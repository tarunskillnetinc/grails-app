package uk.co.wonderlane.wlpos

class PartnerCategoryManagementController {

    def springSecurityService
    def categoryService

    def index() {
        List<EcomSupplier> ecomSupplierList = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
        [ecomSupplierList: ecomSupplierList]
    }

    def ajaxGetPartnerCategories(){
        int retailerId = springSecurityService.principal.retailerId
        List<EcomSupplier> ecomSupplierList = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
        render(view: "_partnerCategoryResults", model: [ecomSupplierList: ecomSupplierList])
    }

    def addPartnerCategory(){
        int retailerId = springSecurityService.principal.retailerId
        int supplierCategoryId = -1
        def isUpdate = false
        def partnerCategoryList = []
        List<EcomSupplier> ecomSupplierList = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
        EcomSupplierCategory ecomSupplierCategory = EcomSupplierCategory.findByIdAndDeleted(supplierCategoryId, false)
        def categoryValues  = categoryService.getTopLevelCategories()
        def selectedCategoryId = null

        render(view: "_addPartnerCategory", model: [partnerCategoryList: partnerCategoryList,
                                                    ecomSupplierList: ecomSupplierList,
                                                    ecomSupplierCategory : ecomSupplierCategory,
                                                    categoryValues :categoryValues,
                                                    selectedCategoryId : selectedCategoryId,
                                                    isUpdate : isUpdate])
    }

    def savePartnerCategory(){
        try {

        } catch (Exception ex) {

        }
    }

    def removePartnerCategory(){
        try {

        } catch (Exception ex) {

        }
    }

}
