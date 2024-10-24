package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.SafeType

class SafeController {

    def safeService
    def springSecurityService
    def rabbitService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
        }
        boolean showInactiveSafes = params?.showInactiveSafes ? Boolean.parseBoolean(params.showInactiveSafes) : false
        [showInactiveSafes: showInactiveSafes]
    }

    def addSafe(Integer id, Boolean edit) {
        boolean isUpdate = edit
        boolean showInactiveSafes = params.boolean('inactiveSafes')
        def safe = safeService.getSafeById(id)
        render(view: "_addSafe", model: [safe: safe, isUpdate: isUpdate, showInactiveSafes: showInactiveSafes])
    }

    def saveSafe() {
        Safe existingSafe = null
        boolean isUpdate = false
        boolean showInactiveSafes
        try {
            isUpdate = params?.isUpdate ? Boolean.parseBoolean(params.isUpdate) : false
            Integer safeId = params?.id ? Integer.parseInt(params.id) : null
            String safeDescription = params?.description
            String safeType = params?.type as SafeType
            boolean shiftStatus = params?.active?.toLowerCase() == 'true'
            showInactiveSafes = params?.showInactiveSafes ? Boolean.parseBoolean(params.showInactiveSafes) : false
            existingSafe = safeService.getSafeById(safeId)
            if (isUpdate && existingSafe && existingSafe.primary && !shiftStatus) {
                //Check if it try to inactive primary safe (not allowed)
                flash.error = String.format("Primary shift can not be disable.")
                throw new RuntimeException("Primary shift can not be disable.")
            }
            Safe safe = safeService.populateSafe(existingSafe, isUpdate, safeDescription, safeType, shiftStatus)
            safe.validate()
            if (!safe.hasErrors()) {
                safeService.saveSafe(safe)
                //once save make sure to publish this into rabbitMq
                rabbitService.sendOfferAllocationMessage("DataSync", loyaltyOfferSyncMessage)
                flash.message = "Safe ${isUpdate ? 'updated' : 'created'} successfully"
                redirect(action: "index", params: [showInactiveSafes: showInactiveSafes])
            } else {
                List<String> errors = safeService.extractErrorMessages(safe.errors)
                String finalErrors = errors.join('\n')
                flash.error = finalErrors
                throw new RuntimeException("Shift save error.")
            }
        } catch (Exception ex) {
            log.error(String.format("Safe saving failed: error: %s ", ex.getMessage()), ex)
            if (!flash.error) {
                flash.error = String.format("Failed to ${isUpdate ? 'update' : 'create'} safe")
            }
            render(view: "_addSafe", model: [safe: existingSafe, showInactiveSafes: showInactiveSafes , isUpdate: isUpdate])
        }
    }

    def updatePrimarySafe(Integer selectedSafeId) {
        String inactiveSafes = params?.inactiveSafes
        try {
            if (selectedSafeId != null && selectedSafeId > 0) { //Check selected safe id is valid id
                Safe existingSafe = safeService.getSafeById(selectedSafeId)
                if (existingSafe && !existingSafe.active) {  //There is one validation --> check if it is active
                    flash.error = "Invalid safe to be primary. Safe need to be active. Try again."
                } else {
                    safeService.updatePrimarySafe(selectedSafeId)
                    flash.success = "Successfully updated primary safe."
                }
            } else {
                flash.error = "Invalid description selected to be primary. Try again."
            }
        } catch (Exception ex) {
            log.error(String.format("Safe saving failed: error: %s ", ex.getMessage()), ex)
            flash.error = "Primary safe update failed."
        }
        redirect(action: "searchSafe", params: [inactiveSafes: inactiveSafes, successMessage: flash.success, errorMessage: flash.error])
    }

    def searchSafe() {
        boolean inactiveSafes = false
        def successMessage = params?.successMessage
        def errorMessage = params?.errorMessage
        try {
            inactiveSafes = params.inactiveSafes ? params.inactiveSafes.toBoolean() : false
            boolean isDropdownOnly = params.isDropdownOnly ? params.isDropdownOnly.toBoolean() : false
            //This will load all available safe list and extract all safe description with primary safe
            List<Safe> safeList = safeService.getSafesByRetailerAndStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            if (!inactiveSafes) {
                safeList.retainAll { it.active }
            }
            render(template: "safeViewerResults", model: [safes             : safeList,
                                                          isDropdownOnly    : isDropdownOnly,
                                                          successMessage    : successMessage,
                                                          errorMessage      : errorMessage])

        } catch (Exception ex) {
            log.error(String.format("Safe searching failed: for inactive enable: %s error: %s ", inactiveSafes, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: "Safe search failed")
        }
    }

}
