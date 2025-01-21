package uk.co.wonderlane.wlpos

class PartnerCategoryManagementController {

    def springSecurityService
    def categoryService
    def partnerCategoryManagementService

    def index() {
        int retailerId = springSecurityService.principal.retailerId
        List<EcomSupplierCategory> ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeleted(retailerId, false)
        [ecomSupplierCategories: ecomSupplierCategories]
    }

    def ajaxGetPartnerCategories(){
        int retailerId = springSecurityService.principal.retailerId
        List<EcomSupplierCategory> ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeleted(retailerId, false)
        render(view: "_partnerCategoryResults", model: [ecomSupplierCategories: ecomSupplierCategories])
    }

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
            def selectedCategoryId = ecomSupplierCategory?.ecomSupplierCategoryMappings?.get(0)?.category?.id

            render(view: "_addPartnerCategory", model: [partnerCategoryList: partnerCategoryList,
                                                        ecomSupplierList: ecomSupplierList,
                                                        ecomSupplierCategory : ecomSupplierCategory,
                                                        categoryValues :categoryValues,
                                                        selectedCategoryId : selectedCategoryId,
                                                        isUpdate : isUpdate])
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to save partner category"
            redirect(action: "index")
        }

    }

    def savePartnerCategory(){
        try {
            int selectedCategoryId = -1
            int selectedPartnerSupplierId = -1
            boolean isUpdate = false
            EcomSupplierCategory ecomSupplierCategory = null
            int retailerId = springSecurityService.principal.retailerId
            String partnerCategoryName = params.partnerCategoryName
            Optional<Integer> partnerSupplierId = tryParseInt(params.partnerName)
            Optional<Integer> supplierCategoryId = tryParseInt(params.supplierCategoryId)
            Optional<Integer> categoryId = tryParseInt(params.get("category.id"))
            if (categoryId.present) {selectedCategoryId = categoryId.get()}
            if (partnerSupplierId.present) {selectedPartnerSupplierId = partnerSupplierId.get()}
            if (supplierCategoryId.present) {
                int selectedPartnerCategoryId = supplierCategoryId.get()
                isUpdate = true
                ecomSupplierCategory = EcomSupplierCategory.findById(selectedPartnerCategoryId)
            }

            EcomSupplier ecomSupplier = EcomSupplier.findByRetailerIdAndId(retailerId, selectedPartnerSupplierId)
            List<Category> updatedCategoryList = partnerCategoryManagementService.updatedCategoryList(selectedCategoryId)
            if (isUpdate && ecomSupplierCategory && ecomSupplierCategory.deleted) {
                flash.error = "Selected partner category ${ecomSupplierCategory?.description} already deleted. So Can not complete edit action"
            } else if (isUpdate && !ecomSupplierCategory) {
                flash.error = "Selected partner category ${partnerCategoryName} not exists. So Can not complete edit action"
            } else if (!isUpdate){
                ecomSupplierCategory = partnerCategoryManagementService.createNewEcomSupplierCategory(ecomSupplier, retailerId, partnerCategoryName)
            }


            partnerCategoryManagementService.updateEcomSupplierCategoryMappings(ecomSupplier, updatedCategoryList, ecomSupplierCategory)
            ecomSupplierCategory.validate()
            if (ecomSupplierCategory.hasErrors()) {
                partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory)
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
            if (ecomSupplierCategory) {
                ecomSupplierCategory.setDeleted(true)
                partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory)
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
