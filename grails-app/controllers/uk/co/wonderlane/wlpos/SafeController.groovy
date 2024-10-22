package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.SafeType

class SafeController {

    def safeService
    def springSecurityService

    def index() {
        if (!springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }
        List<Safe> safeList = safeService.getSafesByRetailerAndStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        Map<Integer, String> safeDescriptions = safeList?.collectEntries { safe -> [(safe.id): safe.description]}
        String primaryDescription = safeList?.find { it.primary == true }?.description
        flash.message = params?.successMessage
        flash.error = params?.errorMessage
        [safes: safeList, safeDescriptions: safeDescriptions, primaryDescription: primaryDescription]
    }

    def segmentDetails(Integer id, Boolean edit) {
        boolean isUpdate = edit
        def safe = safeService.getSafeById(id)
        def safeTypes = SafeType.values()
        render (view: "_safeDetails", model: [safe: safe, safeTypes : safeTypes, isUpdate: isUpdate])
    }

    def saveSafe(){
        Safe existingSafe =  null
        boolean isUpdate = false
        try {
            isUpdate = params?.isUpdate ? Boolean.parseBoolean(params.isUpdate) : false
            Integer shiftId = params?.id ? Integer.parseInt(params.id) : null
            String safeDescription = params?.description
            String safeType = params?.type as SafeType
            boolean shiftStatus = params?.active?.toLowerCase() == 'true'
            existingSafe = safeService.getSafeById(shiftId)
            if (isUpdate && existingSafe && existingSafe.primary && !shiftStatus){ //Check if it try to inactive primary safe (not allowed)
                flash.error = String.format("Primary shift can not be disable.")
                throw new RuntimeException("Primary shift can not be disable.")
            }
            Safe safe = safeService.populateSafe(existingSafe, isUpdate, safeDescription, safeType, shiftStatus)
            safe.validate()
            if (!safe.hasErrors()) {
                safeService.saveSafe(safe)
                flash.message = "Safe ${isUpdate ? 'updated' : 'created'} successfully"
                redirect(action: "index", params: [successMessage: flash.message])
            } else {
                List<String> errors = safeService.extractErrorMessages(safe.errors)
                flash.error = errors.join('\n')
                throw new RuntimeException("Shift save error.")
            }
        } catch (Exception ex) {
            println ex
            if (!flash.error){
                flash.error = String.format("Failed to ${isUpdate ? 'update' : 'create'} safe")
            }
            render (view: "_safeDetails", model: [safe: existingSafe, safeTypes : SafeType.values(), isUpdate: isUpdate])
        }
    }

    def updatePrimarySafe(Integer selectedSafeId){
        try {
            if (selectedSafeId == null || (selectedSafeId != null && selectedSafeId < 0)) {
                redirect(action: "index", params: [errorMessage: "Invalid description selected to be primary. Try again."])
            }
            safeService.updatePrimarySafe(selectedSafeId)
            render(status: 200, contentType: 'application/json', message: "Successfully updated primary safe.")
        } catch (Exception ex) {
            render(status: 400, contentType: 'application/json', message: "Primary safe update failed.")
        }
    }

    def searchSafe(){
        try {

        } catch (Exception ex) {

        }
    }

}
