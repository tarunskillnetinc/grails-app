package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.cash.SafeSession

class SafeManagementController {

    def springSecurityService
    def safeManagementService
    def safeService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        String startDate = (DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().minusDays(7)).toString(formatter)
        String endDate = (DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()).toString(formatter)

        [startDate: params.startDate ?: startDate, endDate: params.endDate ?: endDate, safeId: params.safeId]
    }

    // This will load all available safe sessions and parsing to view to display
    def ajaxGetSafeSessions() {
        try {
            //Load existing active safe sessions -> At the moment since there is no ant filters pass null as safeId
            List<SafeSession> safeSessions = safeManagementService.getActiveSafeSession(null)

            //Load all safes -> Which requires to check safe type and primary status
            List<Safe> safeList = safeService.getStoreSafes()

            //Check any financial week available for shifts
            boolean isFinancialWeekExists = safeSessions.any { shift -> shift.financialWeek != null }

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

            render(template: "safeSessionViewerResults", model: [safeSessions : safeSessions, safeList: safeList, isFinancialWeekExists: isFinancialWeekExists, lastRefreshDate: new DateTime()])

        } catch (Exception ex) {
            var errorMessage = "Error loading safe session list"
            log.error("Safe session loading error error: ${ex.getMessage()}", ex)
            if (errorMessage == null || errorMessage == '') {
                errorMessage = "Unexpected error loading shifts"
            }
            render(template: "safeSessionViewerResults", model: [errorMessage: errorMessage])
        }
    }
}
