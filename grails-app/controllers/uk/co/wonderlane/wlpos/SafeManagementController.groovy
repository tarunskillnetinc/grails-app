package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.entities.cash.ReconciliationTotal
import uk.co.wonderlane.wlpos.entities.cash.SafeSession
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.ReasonCodeType
import uk.co.wonderlane.wlpos.enums.SafeSessionStatus
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.exception.SafeSessionUpdateException

class SafeManagementController {

    def springSecurityService
    def safeManagementService
    def safeService
    def reasonCodeService
    def cashManagementService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
        }
    }

    // This will load all available safe sessions and parsing to view to display
    def ajaxGetSafeSessions() {
        def successMessage = params.successMessage
        def errorMessage = params.errorMessage
        try {
            //Load existing active safe sessions -> At the moment since there is no ant filters pass null as safeId
            List<SafeSession> safeSessions = safeManagementService.getActiveSafeSessions(null)

            //Load all safes -> Which requires to check safe type and primary status
            List<Safe> safeList = safeService.getStoreSafes()

            //Check any financial week available for safe session
            boolean isFinancialWeekExists = safeSessions.any { session -> session.financialWeek != null }

            // Create a map of safes by their ID for quick lookup
            Map<Long, Safe> safeMap = safeList.collectEntries { [(it.id): it] }

            // Sort the safeSessions list
            safeSessions = safeSessions.sort { session ->
                Safe correspondingSafe = safeMap[session.safeId]  // Find the corresponding safe using the safeId
                [
                        correspondingSafe?.primary ? 0 : 1,  // Primary safes first (0) and others (1)
                        session.safeId                      // Then sort by safe ID in ascending order
                ]
            }

            int configuredRecountLimit = safeManagementService.getConfiguredRecountAttempts()

            render(template: "safeSessionViewerResults", model: [safeSessions : safeSessions, safeList: safeList, isFinancialWeekExists: isFinancialWeekExists,
                                                                 configuredRecountLimit: configuredRecountLimit,  successMessage: successMessage, errorMessage: errorMessage])

        } catch (Exception ex) {
            if (errorMessage == null || errorMessage == '') {
                errorMessage = "Error loading safe session list"
            }
            log.error("Safe session loading error error: ${ex.getMessage()}", ex)
            if (errorMessage == null || errorMessage == '') {
                errorMessage = "Unexpected error loading safe sessions"
            }
            render(template: "safeSessionViewerResults", model: [errorMessage: errorMessage])
        }
    }

    // This is method to load either cash up model or cash summary based on requested action
    // If action is either reconcile or recount --> then popup cash up mode
    // If action is finalise --> then pop up cash summary mode
    def ajaxGetSafeSessionCashUpModal(int sessionId, boolean isRecount, boolean isFinalise, String safeDescription) {
        try {
            def safeSession = safeManagementService.getSafeSession(sessionId)
            if (safeSession == null) {
                render(status: 400, contentType: 'application/json', message: "Safe session not found for ${safeDescription}.")
                return
            }
            // If it is RECONCILE request -> Session status must be OPEN
            // If it is RECOUNT or FINALISED request -> Session status must be RECONCILED
            def isReconcile = !isRecount && !isFinalise
            def validStatus = (safeSession.getSessionStatus() == SafeSessionStatus.OPEN && isReconcile)
                    || (safeSession.getSessionStatus() == SafeSessionStatus.RECONCILED && !isReconcile);

            if (validStatus) {
                if (isFinalise) { // If the safe request is finalise show summary modal
                    def varianceReasons = reasonCodeService.getReasonCodesByType(safeSession.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_SAFE_VARIANCE)
                    boolean isSafeFinalisingWarningRequired = safeManagementService.isSafeFinalisingWarningRequired(safeSession)
                    render(template: "cashUpSummaryModal", model: [safeSession: safeSession, isSafeSessionFinalizeMode: true, varianceReasons:varianceReasons,
                                                                   safeDescription: safeDescription, isSafeFinalisingWarningRequired: isSafeFinalisingWarningRequired])
                } else {
                    render(template: "cashUpModal", model: [safeSession: safeSession, safeDescription: safeDescription])
                }
            } else {
                String errorMsg = "Safe ${safeDescription} is in an invalid status to "
                if (isFinalise) {
                    errorMsg += "finalise."
                } else if (isRecount) {
                    errorMsg += "recount."
                } else {
                    errorMsg += "reconcile."
                }
                render(status: 400, contentType: 'application/json', message: errorMsg)
            }
        } catch (Exception ex) {
            log.error("Safe session cash detail loading error for safe session id: ${sessionId} error: ${ex.getMessage()}",  ex)
            render(status: 400, contentType: 'application/json', message: "Action failed for safe ${safeDescription}.")
        }
    }

    // This will store values added in cash up model into temporary variable `pending` cash and voucher total's in session object
    // Secondary this will check any available locations available if not added default `Safe 1` location
    def ajaxUpdateSafeSessionReconcileData(SafeSessionCashUpCommand safeSessionCashUpCommand) {
        try {
            def safeSession = safeManagementService.getSafeSession(safeSessionCashUpCommand.safeSessionId)
            //This is called either in reconcile or recount flow in this case safe session status can be either OPEN or RECONCILED
            if (safeSession != null && (safeSession.getSessionStatus() == SafeSessionStatus.OPEN || safeSession.getSessionStatus() == SafeSessionStatus.RECONCILED)) {
                def varianceReasons = reasonCodeService.getReasonCodesByType(safeSession.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_SAFE_VARIANCE)
                safeManagementService.processInterimReconciliationSave(safeSessionCashUpCommand, safeSession)
                def cashManagementConfig = cashManagementService.getCashManagementConfig(safeSession.getRetailerId(), safeSession.getStoreId())
                def tillSafeSessionVarianceLimit = cashManagementConfig?new BigDecimal(cashManagementConfig.getSafeVarianceLimit()).movePointLeft(2):0.00
                boolean isSafeFinalisingWarningRequired = safeManagementService.isSafeFinalisingWarningRequired(safeSession)
                response.status = 200
                //Here this will load cash up summary with on hold data because that hasn't save into safe session's reconciliationTotals values
                render(template: "cashUpSummaryModal", model: [safeSession: safeSession, varianceReasons: varianceReasons, isSafeSessionFinalizeMode: false,
                                                               tillSafeSessionVarianceLimit : tillSafeSessionVarianceLimit,
                                                               safeDescription: safeSessionCashUpCommand.safeDescription,
                                                               isSafeFinalisingWarningRequired : isSafeFinalisingWarningRequired ])
            }
        } catch (SafeSessionUpdateException ex) {
            log.info("Safe sessions reconciliation save error for safe session id: ${safeSessionCashUpCommand.safeSessionId} error: ${ex.getMessage()}", ex)
            render(status: 400, contentType: 'application/json', message: ex.getMessage())
        } catch (Exception ex) {
            log.error("Safe sessions reconciliation save error for safe session id: ${safeSessionCashUpCommand.safeSessionId} error: ${ex.getMessage()}", ex)
            render(status: 400, contentType: 'application/json', message: "Action failed for safe ${safeSessionCashUpCommand.safeDescription}.")
        }
    }

    // If the request is reconcile, recount or finalise then this is to
    //    1. save session to temporary save variable `pending` into actual cash and voucher total's in session object
    //    2. Add audit entry
    def ajaxSaveSafeSessionCashData(SafeSessionSaveCommand safeSessionSaveCommand) {
        try {
            def safeSession = safeManagementService.getSafeSession(safeSessionSaveCommand.safeSessionId)
            if (safeSession != null && ((!safeSessionSaveCommand.isRecount && !safeSessionSaveCommand.isFinalise && safeSession.getSessionStatus() == SafeSessionStatus.OPEN) ||
                    ((safeSessionSaveCommand.isRecount || safeSessionSaveCommand.isFinalise) && safeSession.getSessionStatus() == SafeSessionStatus.RECONCILED))) {
                safeManagementService.processDataSave(safeSessionSaveCommand, safeSession)
                if (safeSessionSaveCommand.isFinalise) { //Only update this if it is finalized
                    //Safe session finalise logic
                    //If safe is active then create new safe and move all reconcile amounts into tender totals
                    //If safe is in active but have cash in it then also create new safe and move all reconcile amounts into tender totals
                    Safe safe = safeService.getSafeById(safeSession.safeId)
                    List<TenderTotal> tenderTotals = safeSession.getCombinedReconciledAndPendingTotals()
                    //If safe is active or if save is inactive but have cash to move then create new safe session and assign counted values to new session
                    if (safe.active || !tenderTotals.isEmpty()){
                        safeManagementService.createNewSafeSessionWithTenderTotals(
                                safe.retailerId, safe.storeId, safe.id,
                                tenderTotals, false)
                    }

                    //Redirect to ajaxGetSafeSessions to reload safe session view
                    redirect(action: "ajaxGetSafeSessions", params: [successMessage: "Successfully finalised safe ${safeSessionSaveCommand.safeDescription}."])
                    return
                }
                boolean isSafeFinalisingWarningRequired = safeManagementService.isSafeFinalisingWarningRequired(safeSession)
                def varianceReasons = reasonCodeService.getReasonCodesByType(safeSession.getRetailerId(), ReasonCodeType.TENDER_RECONCILIATION_SAFE_VARIANCE)
                //Here this will load cash up summary with actual session's reconciliationTotals values because that is now confirmed
                render(template: "cashUpSummaryModal", model: [safeSession: safeSession, isSafeSessionFinalizeMode: true, varianceReasons:varianceReasons,
                                                               safeDescription: safeSessionSaveCommand.safeDescription,
                                                               isSafeFinalisingWarningRequired: isSafeFinalisingWarningRequired])
            } else if (safeSession != null && !safeSessionSaveCommand.isRecount && !safeSessionSaveCommand.isFinalise && safeSession.getSessionStatus() != SafeSessionStatus.OPEN) {
                // Request is for reconcile but already reconciled
                render(status: 400, contentType: 'application/json', message: "Failed to reconcile safe ${safeSessionSaveCommand.safeDescription}. Already reconciled.")
            } else if (safeSession != null && safeSessionSaveCommand.isRecount && safeSession.getSessionStatus() != SafeSessionStatus.RECONCILED) {
                // Request is for recount but already recounted
                render(status: 400, contentType: 'application/json', message: "Failed to recount safe ${safeSessionSaveCommand.safeDescription}. Already recounted.")
            } else if (safeSession != null && safeSessionSaveCommand.isFinalise && safeSession.getSessionStatus() != SafeSessionStatus.RECONCILED) {
                // Request is for finalise but already finalised
                render(status: 400, contentType: 'application/json', message: "Failed to finalise safe ${safeSessionSaveCommand.safeDescription}. Already finalised.")
            } else {
                render(status: 400, contentType: 'application/json', message: "Action failed for safe ${safeSessionSaveCommand.safeDescription}.")
            }
        } catch (SafeSessionUpdateException ex) {
            log.info("Safe session reconciliation error for session id: ${safeSessionSaveCommand.safeSessionId} error: ${ex.getMessage()}", ex)
            render(status: 400, contentType: 'application/json', message: ex.getMessage())
        } catch (Exception ex) {
            log.error("Safe session reconciliation error for session id: ${safeSessionSaveCommand.safeSessionId} error: ${ex.getMessage()}", ex)
            render(status: 400, contentType: 'application/json', message: "Action failed for safe ${safeSessionSaveCommand.safeDescription}.")
        }
    }

    // This is method to spot check this will popup dialog box which have values each tender types
    def ajaxSpotCheck(){
        Integer safeSessionId = params.safeSessionId ? Integer.parseInt(params.safeSessionId) : -1
        try {
            def safeSession = safeManagementService.getSafeSession(safeSessionId)
            if (safeSession != null) { // If safe not exists then process the action
                safeManagementService.addSpotCheckAudit(safeSession) // Add audit for spot check
                safeSession.transferPendingTotals() // combine totals for spot check, but not the audit
                safeSession.setVersionId("LOCAL CHANGES") // prevent accidental saving
                render(template: "spotCheck", model: [safeSession: safeSession, fetchTime: new DateTime()]) //Load spot check template
            } else {
                render(status: 400, contentType: 'application/json', message: String.format("Spot check action failed. Safe Session not available for safe session id %d", safeSessionId))
            }
        } catch (Exception ex) {
            log.error(String.format("Spot check error for Safe Session id: %d error: %s", safeSessionId, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: String.format("Action failed for spot check for safe session id: %d ", safeSessionId))
        }
    }

}

class SafeSessionCashUpCommand {

    int safeSessionId
    boolean isRecount
    String safeDescription
    String type // Type being navigated TO.
    String cashUpBy // Type being navigated FROM.
    BigDecimal fiftyPounds = BigDecimal.ZERO
    BigDecimal twentyPounds = BigDecimal.ZERO
    BigDecimal tenPounds = BigDecimal.ZERO
    BigDecimal fivePounds = BigDecimal.ZERO
    BigDecimal twoPounds = BigDecimal.ZERO
    BigDecimal onePounds = BigDecimal.ZERO
    BigDecimal fiftyPences = BigDecimal.ZERO
    BigDecimal twentyPences = BigDecimal.ZERO
    BigDecimal tenPences = BigDecimal.ZERO
    BigDecimal fivePences = BigDecimal.ZERO
    BigDecimal twoPences = BigDecimal.ZERO
    BigDecimal onePences = BigDecimal.ZERO
    BigDecimal cashTotal = BigDecimal.ZERO
    BigDecimal chequesTotal = BigDecimal.ZERO
    BigDecimal vouchersTotal = BigDecimal.ZERO
}

class SafeSessionSaveCommand {

    int safeSessionId
    boolean isFinalise
    boolean isRecount
    String safeDescription
    String tenderReconciliationVarianceReason
    String tenderReconciliationVarianceReasonText
}
