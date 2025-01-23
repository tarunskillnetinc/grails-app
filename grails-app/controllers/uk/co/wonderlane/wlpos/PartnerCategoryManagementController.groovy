package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured

class PartnerCategoryManagementController {

    def springSecurityService
    def categoryService
    def partnerCategoryManagementService

    def index() {
        int retailerId = springSecurityService.principal.retailerId
        List<EcomSupplier> ecomSuppliers = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
        [ecomSuppliers: ecomSuppliers]
    }

    def ajaxGetPartnerCategories(){
        EcomSupplier ecomSupplier = null
        int retailerId = springSecurityService.principal.retailerId
        Integer supplierId = params.partnerSupplierIdFilter ? Integer.valueOf(params.partnerSupplierIdFilter) : null
        String partnerCategoryNameFilter = params.partnerCategoryNameFilter ? params.partnerCategoryNameFilter : null
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        session.PARTNER = supplierId
        session.PARTNER_CATEGORY = partnerCategoryNameFilter
        if (supplierId != null) {
            ecomSupplier = EcomSupplier.findByIdAndDeletedAndRetailerId(supplierId, false, retailerId)
        }
        List<EcomSupplierCategory> ecomSupplierCategories = partnerCategoryManagementService.getFilterPartnerCategories(retailerId, ecomSupplier, partnerCategoryNameFilter)
        int totalResults = ecomSupplierCategories.size()
        ecomSupplierCategories = ecomSupplierCategories.drop(offset).take(max)
        render(view: "_partnerCategoryResults", model: [ecomSupplierCategories: ecomSupplierCategories, offset: offset, max: max, totalResults: totalResults])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def addPartnerCategory(){
        try {
            int retailerId = springSecurityService.principal.retailerId
            int selectedSupplierCategoryId = -1
            Optional<Integer> supplierCategoryId = tryParseInt(params.supplierCategoryId)
            if (supplierCategoryId.present) {
                selectedSupplierCategoryId = supplierCategoryId.get()
            }
            def isUpdate = false
            def partnerCategoryList = []
            List<EcomSupplier> ecomSupplierList = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
            EcomSupplierCategory ecomSupplierCategory = EcomSupplierCategory.findByIdAndDeleted(selectedSupplierCategoryId, false)
            def categoryValues  = categoryService.getTopLevelCategories()
            def selectedCategoryIds = ecomSupplierCategory?.ecomSupplierCategoryMappings?.collect { it?.category?.id }?.findAll { it != null } ?: []

            render(view: "_addPartnerCategory", model: [partnerCategoryList: partnerCategoryList,
                                                        ecomSupplierList: ecomSupplierList,
                                                        ecomSupplierCategory : ecomSupplierCategory,
                                                        categoryValues :categoryValues,
                                                        selectedCategoryIds : selectedCategoryIds,
                                                        isUpdate : isUpdate])
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to save partner category"
            redirect(action: "index")
        }

    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def savePartnerCategory(){
        try {
            int selectedPartnerSupplierId = -1
            boolean isUpdate = false
            EcomSupplierCategory ecomSupplierCategory = null
            int retailerId = springSecurityService.principal.retailerId
            String partnerCategoryName = params.partnerCategoryName

            //here input recieved as selected category as this "[1, 2, 3, 4]"
            //So initially removed [ ] and parse into list of integers
            String selectedCategoryIds = params.get("category.id")
            List<Integer> selectedCategoryList = selectedCategoryIds?.replaceAll("[\\[\\]]", "")?.split(",")?.collect { it.trim() as Integer } ?: []
            List<Category> updatedCategoryList = partnerCategoryManagementService.updatedCategoryList(selectedCategoryList)
            if (!updatedCategoryList || updatedCategoryList.isEmpty()) { //Validate at least single category is created
                flash.error = "Please select at least one category"
                redirect(action: "addPartnerCategory")
                return
            }

            Optional<Integer> partnerSupplierId = tryParseInt(params.partnerSupplierId)
            if (partnerSupplierId.present) {selectedPartnerSupplierId = partnerSupplierId.get()}
            EcomSupplier ecomSupplier = EcomSupplier.findByRetailerIdAndIdAndDeleted(retailerId, selectedPartnerSupplierId, false)
            if (ecomSupplier == null) { //Validate partner supplier exists
                flash.error = "Selected supplier not found"
                redirect(action: "addPartnerCategory")
                return
            }

            Optional<Integer> partnerCategoryId = tryParseInt(params.partnerCategoryId)
            if (partnerCategoryId.present) { //Check and validate partner category if it exists
                int selectedPartnerCategoryId = partnerCategoryId.get()
                isUpdate = true
                ecomSupplierCategory = EcomSupplierCategory.findById(selectedPartnerCategoryId)
                if (ecomSupplierCategory && ecomSupplierCategory.deleted) {
                    flash.error = "Selected partner category ${ecomSupplierCategory?.description} already deleted. So Can not complete edit action"
                    redirect(action: "addPartnerCategory")
                    return
                } else if (!ecomSupplierCategory) {
                    flash.error = "Selected partner category ${partnerCategoryName} not exists. So Can not complete edit action"
                    redirect(action: "addPartnerCategory")
                    return
                }
            }


            if (!isUpdate){ //If this is not update action then create new supplier category
                ecomSupplierCategory = partnerCategoryManagementService.createNewEcomSupplierCategory(ecomSupplier, retailerId, partnerCategoryName)
            }

            //Load category mappings which should be removed
            List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings =
                    partnerCategoryManagementService.removedEcomSupplierCategoryMappings(updatedCategoryList, ecomSupplierCategory)

            //Load category mappings which should be added
            List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings =
                    partnerCategoryManagementService.addedEcomSupplierCategoryMappings(ecomSupplier, updatedCategoryList, ecomSupplierCategory)

            ecomSupplierCategory.validate() //Validate partner category
            if (!ecomSupplierCategory.hasErrors()) { //Proceed if no error found
                partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory, removedEcomSupplierCategoryMappings, addedEcomSupplierCategoryMappings)
                flash.message = "Successfully save partner category"
                redirect(action: "index")
            } else {
                flash.error = "Failed to save partner category"
                redirect(action: "addPartnerCategory")
            }
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to save partner category"
            redirect(action: "addPartnerCategory")
        }
    }

    def deletePartnerCategory(){
        try {
            int selectedSupplierCategoryId = -1
            Optional<Integer> supplierCategoryId = tryParseInt(params.supplierCategoryId)
            if (supplierCategoryId.present) {
                selectedSupplierCategoryId = supplierCategoryId.get()
            }
            EcomSupplierCategory ecomSupplierCategory = EcomSupplierCategory.findById(selectedSupplierCategoryId)
            List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings = partnerCategoryManagementService.removedEcomSupplierCategoryMappings(null, ecomSupplierCategory)
            if (ecomSupplierCategory) {
                ecomSupplierCategory.setDeleted(true)
                partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory, removedEcomSupplierCategoryMappings, null)
                flash.message = "Successfully delete partner category ${ecomSupplierCategory?.description}"
                redirect(action: "index")
            } else {
                flash.error = "Failed to delete partner category"
                redirect(action: "index")
            }
        } catch (Exception ex) {
            log.error("Error deleting partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to delete partner category"
            redirect(action: "index")
        }
    }

    private static Optional<Integer> tryParseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str))
        } catch (Exception ignored) {
            return Optional.empty()
        }
    }

}
