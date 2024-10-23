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
    }

    def segmentDetails(Integer id, Boolean edit) {
        boolean isUpdate = edit
        def safe = safeService.getSafeById(id)
        def safeTypes = SafeType.values()
        render(view: "_safeDetails", model: [safe: safe, safeTypes: safeTypes, isUpdate: isUpdate])
    }

    def saveSafe() {
        Safe existingSafe = null
        boolean isUpdate = false
        try {
            isUpdate = params?.isUpdate ? Boolean.parseBoolean(params.isUpdate) : false
            Integer safeId = params?.id ? Integer.parseInt(params.id) : null
            String safeDescription = params?.description
            String safeType = params?.type as SafeType
            boolean shiftStatus = params?.active?.toLowerCase() == 'true'
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
                redirect(action: "searchSafe", params: [successMessage: "Safe ${isUpdate ? 'updated' : 'created'} successfully"])
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
            render(view: "_safeDetails", model: [safe: existingSafe, safeTypes: SafeType.values(), isUpdate: isUpdate])
        }
    }

    def updatePrimarySafe(Integer selectedSafeId) {
        try {
            //Check selected safe id is valid id
            if (selectedSafeId == null || (selectedSafeId != null && selectedSafeId < 0)) {
                redirect(action: "index", params: [errorMessage: "Invalid description selected to be primary. Try again."])
                return
            }
            Safe existingSafe = safeService.getSafeById(selectedSafeId)
            if (existingSafe && !existingSafe.active) {  //There is one validation --> check if it is active
                redirect(action: "searchSafe", params: [errorMessage: "Invalid safe to be primary. Safe need to be active. Try again."])
            } else {
                safeService.updatePrimarySafe(selectedSafeId)
                redirect(action: "searchSafe", params: [successMessage: "Successfully updated primary safe."])
            }
        } catch (Exception ex) {
            log.error(String.format("Safe saving failed: error: %s ", ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: "Primary safe update failed.")
        }
    }

    def searchSafe() {
        String searchTerm = null
        boolean activeSafes = false
        boolean inactiveSafes = false
        def successMessage = params?.successMessage
        def errorMessage = params?.errorMessage
        try {
            searchTerm = params.searchTerm ? params.searchTerm : ""
            activeSafes = params.activeSafes ? params.activeSafes.toBoolean() : false
            inactiveSafes = params.inactiveSafes ? params.inactiveSafes.toBoolean() : false
            boolean isDropdownOnly = params.isDropdownOnly ? params.isDropdownOnly.toBoolean() : false
            int max = params.max ? Integer.parseInt(params.max) : 20
            int offset = 0
            String sortColumn = params.sortColumn ?: "dateCreated"
            String sortOrder = params.sortOrder ?: "desc"

            //This will load safes based on search criteria
            List<Safe> safeList = safeService.findSearchSafes(searchTerm, activeSafes, inactiveSafes, max, offset, sortColumn, sortOrder)

            //This will load all available safe list and extract all safe description with primary safe
            List<Safe> allAvailableSafeList = safeService.getSafesByRetailerAndStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            Map<Integer, String> safeDescriptions = allAvailableSafeList?.collectEntries { safe -> [(safe.id): safe.description] }
            String primaryDescription = allAvailableSafeList?.find { it.primary == true }?.description

            render(template: "safeViewerResults", model: [safes             : safeList,
                                                          safeDescriptions  : safeDescriptions,
                                                          primaryDescription: primaryDescription,
                                                          searchTerm        : searchTerm,
                                                          isDropdownOnly    : isDropdownOnly,
                                                          successMessage    : successMessage,
                                                          errorMessage      : errorMessage])

        } catch (Exception ex) {
            log.error(String.format("Safe searching failed: for search term: %s active enable : %s inactive enable: %s error: %s ", searchTerm, activeSafes, inactiveSafes, ex.getMessage()), ex)
            render(status: 400, contentType: 'application/json', message: "Safe search failed")
        }
    }

}
