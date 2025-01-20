package uk.co.wonderlane.wlpos

class PartnerCategoryManagementController {

    def springSecurityService
    def categoryService

    def index() {}

    def ajaxGetPartnerCategories(){
        List<EcomSupplier> ecomSupplierList = EcomSupplier.findAll()
    }

    def addPartnerCategory(){
        int retailerId = springSecurityService.principal.retailerId
        int supplierCategoryId = -1
        def isUpdate = false
        def partnerCategoryList = []
        List<EcomSupplier> ecomSupplierList = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
        EcomSupplierCategory ecomSupplierCategory = EcomSupplierCategory.findById(supplierCategoryId)
        def categoryValues  = categoryService.getTopLevelCategories()
        def selectedCategoryId = null

        render(view: "_addPartnerCategory", model: [partnerCategoryList: partnerCategoryList,
                                                    ecomSupplierList: ecomSupplierList,
                                                    ecomSupplierCategory : ecomSupplierCategory,
                                                    categoryValues :categoryValues,
                                                    selectedCategoryId : selectedCategoryId,
                                                    isUpdate : isUpdate])
    }
}
