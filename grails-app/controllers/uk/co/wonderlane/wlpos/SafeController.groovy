package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.SafeType

class SafeController {

    def safeService
    def locationService
    def springSecurityService
    def safeManagementService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
        }
        boolean inactiveSafes = params?.inactiveSafes ? Boolean.parseBoolean(params.inactiveSafes) : false
        [showInactiveSafes: inactiveSafes]
    }

    def addSafe(Integer id, Boolean edit) {
        boolean isUpdate = edit
        boolean inactiveSafes = params.boolean('inactiveSafes')
        def safe = safeService.getSafeById(id)
        render(view: "_addSafe", model: [safe: safe, isUpdate: isUpdate, inactiveSafes: inactiveSafes])
    }

    def closeSafeAdd(){
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
            boolean safeStatus = params?.active ? Boolean.parseBoolean(params.active) : false
            existingSafe = safeService.getSafeById(safeId)
            boolean currentlyActive = existingSafe?.active ?: false
            if (isUpdate && existingSafe && existingSafe.primary && !safeStatus) {
                //Check if it try to inactive primary safe (not allowed)
                flash.error = String.format("Primary safe can not be disabled.")
                throw new RuntimeException("Primary safe can not be disabled.")
            }
            Safe safe = safeService.populateSafe(existingSafe, isUpdate, safeDescription, safeType, safeStatus)
            safe.validate() //call validation to check and if domain class validation errors
            if (!safe.hasErrors()) {
                safeService.saveSafe(safe) //Save created/updated safe into db
                if (isUpdate) { // If this is update then update location description
                    safeService.updateLocationDescriptionBySafeId(safe.id, safe.description)
                    //If safe saved and has been set active create a a safe session
                    if(!currentlyActive && safeStatus){
                        safeManagementService.createNewSafeSession(safe.retailerId, safe.storeId, safe.id, false);
                    }
                } else {
                    locationService.createSafeLocation(safe.id, safe.description)
                    safeManagementService.createNewSafeSession(safe.retailerId, safe.storeId, safe.id, false)
                }
                safeService.pushSafeIntoRabbitMQ(safe) //once save make sure to publish this into rabbitMq
                flash.message = "Safe ${isUpdate ? 'updated' : 'created'} successfully"
                redirect(action: "index", params: [inactiveSafes: inactiveSafes])
            } else {
                List<String> errors = safeService.extractErrorMessages(safe.errors)
                String finalErrors = errors.join('\n')
                flash.error = finalErrors
                throw new RuntimeException("Safe saving error.")
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
                    // This will
                    // 1. Remove primary flag from all available safes
                    // 2. Add new flag to requested safe
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
            List<Safe> safeList = safeService.getStoreSafes()
            if (!inactiveSafes) { //This will load all available safe list and extract with active
                safeList.retainAll { it.active }
            }
            render(template: "safeViewerResults", model: [safes             : safeList,
                                                          successMessage    : successMessage,
                                                          errorMessage      : errorMessage])

        } catch (Exception ex) {
            log.error(String.format("Safe searching failed: for inactive enable: %s error: %s ", inactiveSafes, ex.getMessage()), ex)
            render(status: 400, text: "Safe search failed")
        }
    }

}
