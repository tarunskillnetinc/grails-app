package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.SafeType

class SafeController {

    def safeService
    def springSecurityService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
        }
        boolean inactiveSafes = params?.inactiveSafes ? Boolean.parseBoolean(params.inactiveSafes) : false
        def successMessage = params?.successMessage
        [showInactiveSafes: inactiveSafes, successMessage: successMessage]
    }

    def addSafe(Integer id, Boolean edit) {
        boolean isUpdate = edit
        boolean inactiveSafes = params.boolean('inactiveSafes')
        def safe = safeService.getSafeById(id)
        render(view: "_addSafe", model: [safe: safe, isUpdate: isUpdate, inactiveSafes: inactiveSafes])
    }

    def closeShiftAdd(){
        boolean inactiveSafes = params.boolean('inactiveSafes')
        redirect(action: "index", params: [inactiveSafes: inactiveSafes])
    }

    def saveSafe() {
        Safe existingSafe = null
        boolean isUpdate = false
        boolean inactiveSafes
        try {
            isUpdate = params?.isUpdate ? Boolean.parseBoolean(params.isUpdate) : false
            inactiveSafes = params?.inactiveSafes ? Boolean.parseBoolean(params.inactiveSafes) : false
            Integer safeId = params?.id ? Integer.parseInt(params.id) : null
            String safeDescription = params?.description
            String safeType = params?.type as SafeType
            boolean shiftStatus = params?.active ? Boolean.parseBoolean(params.active) : false
            existingSafe = safeService.getSafeById(safeId)
            if (isUpdate && existingSafe && existingSafe.primary && !shiftStatus) {
                //Check if it try to inactive primary safe (not allowed)
                flash.error = String.format("Primary shift can not be disable.")
                throw new RuntimeException("Primary shift can not be disable.")
            }
            Safe safe = safeService.populateSafe(existingSafe, isUpdate, safeDescription, safeType, shiftStatus)
            safe.validate() //call validation to check ant domain class validation errors
            if (!safe.hasErrors()) {
                safeService.saveSafe(safe) //Save created/updated safe into db
                safeService.pushSafeIntoRabbitMQ(safe) //once save make sure to publish this into rabbitMq
                flash.message = "Safe ${isUpdate ? 'updated' : 'created'} successfully"
                redirect(action: "index", params: [inactiveSafes: inactiveSafes, successMessage: flash.message])
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
            render(view: "_addSafe", model: [safe: existingSafe, inactiveSafes: inactiveSafes , isUpdate: isUpdate])
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
                    safeService.pushAllUpdatedSafesIntoRabbitMQ() //once update done send all available safes into rabbitMq
                    flash.success = String.format("Successfully updated primary safe to %s.", existingSafe.description)
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
