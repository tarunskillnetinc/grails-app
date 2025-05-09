package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured

class PartnerCategoryManagementController extends BaseController{

    def springSecurityService
    def partnerCategoryManagementService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You cannot access this page when logged in as a store."
            redirect(uri: "/")
            return
        }

        int retailerId = springSecurityService.principal.retailerId
        List<EcomSupplier> ecomSuppliers = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)

        session.PARTNER = null
        // Remove any session variables used for filtering so that the page is clean when loaded from the menu.
        session.PARTNER_CATEGORY = null

        [ecomSuppliers: ecomSuppliers]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetPartnerCategories() {
        EcomSupplier ecomSupplier = null
        int retailerId = springSecurityService.principal.retailerId
        Integer supplierId = params.partnerSupplierIdFilter ? Integer.valueOf(params.partnerSupplierIdFilter) : null
        String partnerCategoryNameFilter = params.partnerCategoryNameFilter ? params.partnerCategoryNameFilter : null
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 10
        session.PARTNER = supplierId
        session.PARTNER_CATEGORY = partnerCategoryNameFilter
        if (supplierId != null) {
            ecomSupplier = EcomSupplier.findByIdAndDeletedAndRetailerId(supplierId, false, retailerId)
        }
        List<EcomSupplierCategory> ecomSupplierCategories = partnerCategoryManagementService.getFilterPartnerCategories(retailerId, ecomSupplier, partnerCategoryNameFilter)
        // Order by `id` (ascending)
        ecomSupplierCategories = ecomSupplierCategories?.sort { it.id } // Ascending order
        int totalResults = ecomSupplierCategories?.size()
        ecomSupplierCategories = ecomSupplierCategories?.drop(offset)?.take(max)
        render(view: "_partnerCategoryResults", model: [ecomSupplierCategories: ecomSupplierCategories,
                                                        offset: offset,
                                                        max: max,
                                                        totalResults: totalResults,
                                                        partnerSupplierIdFilter: supplierId == null ? "" : supplierId,
                                                        partnerCategoryNameFilter: partnerCategoryNameFilter == null ? "" : partnerCategoryNameFilter])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def addPartnerCategory(){
        try {
            int retailerId = springSecurityService.principal.retailerId
            EcomSupplierCategory ecomSupplierCategory = null
            Optional<Integer> supplierCategoryId = tryParseInt(params.supplierCategoryId)
            def isUpdate = false
            if (supplierCategoryId.present  && supplierCategoryId.get() > -1) {
                int selectedSupplierCategoryId = supplierCategoryId.get()
                ecomSupplierCategory = EcomSupplierCategory.findByIdAndDeleted(selectedSupplierCategoryId, false)
                if (!ecomSupplierCategory) {
                    flash.error = "Selected partner category not available for edit."
                    redirect(action: "index")
                    return
                }
                isUpdate = true
            }
            renderAddPartnerCategory(ecomSupplierCategory, retailerId, isUpdate)
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to save partner category"
            redirect(action: "index")
        }

    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def savePartnerCategory(){
        int selectedPartnerCategoryId = -1
        EcomSupplierCategory ecomSupplierCategory = null
        int retailerId = springSecurityService.principal.retailerId
        boolean isUpdate
        try {
            int selectedPartnerSupplierId = -1
            String partnerCategoryName = params.partnerCategoryName
            String selectedCategoryIds = params.get("category.id[]")
            Optional<Integer> partnerSupplierId = tryParseInt(params.partnerSupplierId)
            Optional<Integer> partnerCategoryId = tryParseInt(params.partnerCategoryId)
            if (partnerCategoryId.present) {selectedPartnerCategoryId = partnerCategoryId.get()}
            if (partnerSupplierId.present) {selectedPartnerSupplierId = partnerSupplierId.get()}

            EcomSupplier ecomSupplier = EcomSupplier.findByRetailerIdAndIdAndDeleted(retailerId, selectedPartnerSupplierId, false)
            if (ecomSupplier == null) { //Validate partner supplier exists
                flash.categoryError = "Selected supplier not found"
                redirect(action: "addPartnerCategory", params : [supplierCategoryId: selectedPartnerCategoryId])
                return
            }

            if (selectedPartnerCategoryId > -1) { //Check and validate partner category if it exists
                ecomSupplierCategory = EcomSupplierCategory.findById(selectedPartnerCategoryId)
                if (ecomSupplierCategory && ecomSupplierCategory.deleted) {
                    flash.categoryError = "Selected partner category ${ecomSupplierCategory?.description} already deleted. So Can not complete edit action"
                    redirect(action: "addPartnerCategory", params : [supplierCategoryId: selectedPartnerCategoryId])
                    return
                } else if (!ecomSupplierCategory) {
                    flash.categoryError = "Selected partner category ${partnerCategoryName} not exists. So Can not complete edit action"
                    redirect(action: "addPartnerCategory", params : [supplierCategoryId: selectedPartnerCategoryId])
                    return
                }
            } else {
                ecomSupplierCategory = partnerCategoryManagementService.createNewEcomSupplierCategory(ecomSupplier, retailerId, partnerCategoryName)
            }

            //here input received as selected category as this "[1, 2, 3, 4]"
            //So initially removed [ ] and parse into list of integers
            List<Category> updatedCategoryList = partnerCategoryManagementService.updatedCategoryList(selectedCategoryIds)
            if (!updatedCategoryList || updatedCategoryList.isEmpty()) { //Validate at least single category is created
                flash.categoryError = "Category is required. Please select at least one category."
                redirect(action: "addPartnerCategory", params : [supplierCategoryId: selectedPartnerCategoryId])
                return
            }

            //Update partner category name
            //If updated partner category supplier
            //If updated all mappping item's supplier
            partnerCategoryManagementService.updateEcomSupplierCategory(ecomSupplierCategory, partnerCategoryName)

            //Load category mappings which should be removed
            List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings =
                    partnerCategoryManagementService.removedEcomSupplierCategoryMappings(updatedCategoryList, ecomSupplierCategory)

            //Load category mappings which should be added
            List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings =
                    partnerCategoryManagementService.addedEcomSupplierCategoryMappings(ecomSupplier, updatedCategoryList, ecomSupplierCategory)

            //Add or remove category mappings from supplier category
            partnerCategoryManagementService.updateEcomSupplierCategoryMapping(ecomSupplierCategory, removedEcomSupplierCategoryMappings, addedEcomSupplierCategoryMappings)

            ecomSupplierCategory.validate() //Validate partner category
            if (!ecomSupplierCategory.hasErrors()) { //Proceed if no error found
                partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory)
                flash.message = "Successfully save partner category"
                redirect(action: "index")
            } else {
                isUpdate = selectedPartnerCategoryId > -1
                renderAddPartnerCategory(ecomSupplierCategory, retailerId, isUpdate)
            }
        } catch (Exception ex) {
            log.error("Unknown error saving partner categories, Exception " + ex.getMessage(), ex)
            flash.categoryError = "Unknown error saving partner categories"
            isUpdate = selectedPartnerCategoryId > -1
            renderAddPartnerCategory(ecomSupplierCategory, retailerId, isUpdate)
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def deletePartnerCategory(){
        try {
            int selectedSupplierCategoryId = -1
            Optional<Integer> supplierCategoryId = tryParseInt(params.supplierCategoryId)
            if (supplierCategoryId.present) {
                selectedSupplierCategoryId = supplierCategoryId.get()
            }
            EcomSupplierCategory ecomSupplierCategory = EcomSupplierCategory.findById(selectedSupplierCategoryId)
            if (!ecomSupplierCategory) {
                flash.error = "Action failed. Selected partner category not found"
                redirect(action: "index")
            } else if (ecomSupplierCategory && ecomSupplierCategory.deleted) {
                flash.error = "Action failed. Selected partner category already deleted"
                redirect(action: "index")
            } else {
                List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings = partnerCategoryManagementService.removedEcomSupplierCategoryMappings(null, ecomSupplierCategory)
                ecomSupplierCategory.setDeleted(true)
                partnerCategoryManagementService.updateEcomSupplierCategoryMapping(ecomSupplierCategory, removedEcomSupplierCategoryMappings, null)
                ecomSupplierCategory.validate()
                if (!ecomSupplierCategory.hasErrors()) {
                    partnerCategoryManagementService.saveEcomSupplierCategory(ecomSupplierCategory)
                    flash.message = "Successfully delete partner category ${ecomSupplierCategory?.description}"
                    redirect(action: "index")
                } else {
                    flash.error = "Failed to delete partner category ${ecomSupplierCategory?.description}"
                    redirect(action: "index")
                }
            }
        } catch (Exception ex) {
            log.error("Error deleting partner categories, Exception " + ex.getMessage(), ex)
            flash.error = "Failed to delete partner category"
            redirect(action: "index")
        }
    }

    def ajaxFilterValidCategories(){
        List<Category> categories
        ArrayList<EcomSupplierCategoryMapping>  partnerCategoryList= new ArrayList<>()
        ArrayList<Integer> selectedCategoryIds = new ArrayList<>()
        try {
            Integer supplierId = params.supplierId ? Integer.valueOf(params.supplierId) : null
            Integer supplierCategoryId = params.supplierCategoryId ? Integer.valueOf(params.supplierCategoryId) : null
            int retailerId = springSecurityService.principal.retailerId
            EcomSupplier ecomSupplier = EcomSupplier.findByIdAndDeletedAndRetailerId(supplierId, false, retailerId)
            if (ecomSupplier) {
                EcomSupplierCategory ecomSupplierCategory = null
                if (supplierCategoryId > 0) {
                    ArrayList<EcomSupplierCategory> ecomSupplierCategories = ecomSupplier.ecomSupplierCategories ?: []
                    ecomSupplierCategory = ecomSupplierCategories.find { it.id == supplierCategoryId }
                    if (ecomSupplierCategory) {
                        partnerCategoryList = ecomSupplierCategory.mappedCategories ?: []
                        selectedCategoryIds = ecomSupplierCategory?.ecomSupplierCategoryMappings?.collect { it?.category?.id }?.findAll { it != null } ?: []
                    }
                }
                categories = partnerCategoryManagementService.getAvailableTopLevelCategories(ecomSupplier, ecomSupplierCategory)
            } else {
                categories = categoryService.getTopLevelCategories()
            }
            render(template: "/multiSelectCategory/categorySelectInputs", model: [categories: categories,
                                                                                  level: 1,
                                                                                  productCategoryList: partnerCategoryList,
                                                                                  selectedCategoryIds: selectedCategoryIds,
                                                                                  triggerOnCategoryChange: true,
                                                                                  isSearch: false])
        } catch (Exception ex) {
            log.error("Error filtering partner eligible categories, Exception " + ex.getMessage(), ex)
            flash.error = "Unknown error when selecting eligible categories for partner."
            redirect(action: "index")
        }
    }


    def ajaxSearchCategories(String searchTerm, boolean triggerOnCategoryChange, int level, int specialId) {
        def searchResults = baseSearchCategories(searchTerm)
        def selectedCategoryIds = params?.list('selectedCategoryId[]')?.collect { it.toInteger() } ?: []
        boolean isSearch = searchTerm?.length() > 0
        EcomSupplierCategory ecomSupplierCategory = EcomSupplierCategory.findByIdAndDeleted(specialId, false)
        List<Category> searchedResults = searchResults?.aValue?.unique() ?: []
        List<Category> allAvailableCategories = partnerCategoryManagementService.getAvailableTopLevelCategories(ecomSupplierCategory?.ecomSupplier, ecomSupplierCategory) ?: []
        List<Category> filteredResults = searchedResults?.findAll { it in allAvailableCategories } ?: []
        List<Category> remainingResults = allAvailableCategories?.findAll { !(it in filteredResults) } ?: []
        // Combine filtered results at the top and the remaining items in the tail
        List<Category> combinedResults = filteredResults + remainingResults
        def partnerCategoryList = []
        for (Integer categoryId : (selectedCategoryIds as List<Integer>)) {
            def category = categoryService.getCategory(categoryId)
            while (category) {
                if (!partnerCategoryList.contains(category.id)) {// Add the current category ID to the list if not already added
                    partnerCategoryList << category.id
                }
                category = category.parentCategory // Move to the parent category
            }
        }
        render(template: "/multiSelectCategory/categorySelectInputs", model: [categories: combinedResults,
                                                                              level: isSearch ? level : 1,
                                                                              productCategoryList: partnerCategoryList,
                                                                              selectedCategoryIds: selectedCategoryIds,
                                                                              triggerOnCategoryChange: triggerOnCategoryChange,
                                                                              isSearch: isSearch])
    }

    def ajaxGetChildCategories(int categoryId, int level, boolean triggerOnCategoryChange) {
        def category = categoryService.getCategory(categoryId)
        def selectedCategoryIds = params?.list('selectedCategoryId[]')?.collect { it.toInteger() } ?: []
        render(template: "/multiSelectCategory/categorySelectInputs", model: [categories: category?.childCategories,
                                                                              level: level,
                                                                              selectedCategoryIds: selectedCategoryIds,
                                                                              triggerOnCategoryChange: triggerOnCategoryChange])
    }

    def renderAddPartnerCategory(EcomSupplierCategory ecomSupplierCategory, int retailerId, boolean isUpdate){
        List<EcomSupplier> ecomSupplierList = EcomSupplier.findAllByRetailerIdAndDeleted(retailerId, false)
        def selectedCategoryIds = ecomSupplierCategory?.ecomSupplierCategoryMappings?.collect { it?.category?.id }?.findAll { it != null } ?: []
        def selectedPartnerId = ecomSupplierCategory?.ecomSupplier?.id
        def partnerCategoryList = ecomSupplierCategory?.mappedCategories ?: []
        def categories = partnerCategoryManagementService.getAvailableTopLevelCategories(ecomSupplierCategory?.ecomSupplier, ecomSupplierCategory)
        render(view: "_addPartnerCategory", model: [productCategoryList: partnerCategoryList,
                                                    ecomSupplierList: ecomSupplierList,
                                                    ecomSupplierCategory : ecomSupplierCategory,
                                                    categories :categories,
                                                    selectedCategoryIds : selectedCategoryIds,
                                                    isUpdate : isUpdate,
                                                    selectedPartnerId: selectedPartnerId])
    }

    private static Optional<Integer> tryParseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str))
        } catch (Exception ignored) {
            return Optional.empty()
        }
    }

    @Override
    def getColumns() {
        return null
    }

}
