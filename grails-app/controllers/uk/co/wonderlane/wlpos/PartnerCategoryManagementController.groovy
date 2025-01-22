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
            def selectedCategoryIds = ecomSupplierCategory?.ecomSupplierCategoryMappings*.category*.id

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

            Integer partnerSupplierId = params.partnerName ? Integer.valueOf(params.partnerName) ? null
//            Optional<Integer> partnerSupplierId = tryParseInt(params.partnerName)
            Integer supplierCategoryId = params.supplierCategoryId ? Integer.valueOf(params.supplierCategoryId) ? null
            String selectedCategoryIds = params.get("category.id")
            //here input recieved as selected category as this "[1, 2, 3, 4]"
            //So initially removed [ ] and parse into list of integers
            List<Integer> selectedCategoryList = selectedCategoryIds?.replaceAll("[\\[\\]]", "")?.split(",")?.collect { it.trim() as Integer } ?: []
            List<Category> updatedCategoryList = partnerCategoryManagementService.updatedCategoryList(selectedCategoryList)

            if (partnerSupplierId.present) {selectedPartnerSupplierId = partnerSupplierId.get()}
            EcomSupplier ecomSupplier = EcomSupplier.findByRetailerIdAndId(retailerId, selectedPartnerSupplierId)
            if (ecomSupplier == null) {
                flash.error = "Selected supplier not found"
                redirect(action: "index")
                return
            }

            if (supplierCategoryId.present) {
                int selectedPartnerCategoryId = supplierCategoryId.get()
                isUpdate = true
                ecomSupplierCategory = EcomSupplierCategory.findById(selectedPartnerCategoryId)
            }
            if (isUpdate && ecomSupplierCategory && ecomSupplierCategory.deleted) {
                flash.error = "Selected partner category ${ecomSupplierCategory?.description} already deleted. So Can not complete edit action"
                redirect(action: "index")
                return
            } else if (isUpdate && !ecomSupplierCategory) {
                flash.error = "Selected partner category ${partnerCategoryName} not exists. So Can not complete edit action"
                redirect(action: "index")
                return
            }

            if (!isUpdate){
                ecomSupplierCategory = partnerCategoryManagementService.createNewEcomSupplierCategory(ecomSupplier, retailerId, partnerCategoryName)
            }

            List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings = partnerCategoryManagementService.removedEcomSupplierCategoryMappings(updatedCategoryList, ecomSupplierCategory)
            List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings = partnerCategoryManagementService.addedEcomSupplierCategoryMappings(ecomSupplier, updatedCategoryList, ecomSupplierCategory)
            ecomSupplierCategory.validate()
            if (!ecomSupplierCategory.hasErrors()) {
                partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory, removedEcomSupplierCategoryMappings, addedEcomSupplierCategoryMappings)
                flash.message = "Successfully save partner category"
                redirect(action: "index")
            } else {
                flash.error = "Failed to save partner category"
                redirect(action: "index")
            }
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to save partner category"
            redirect(action: "index")
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
