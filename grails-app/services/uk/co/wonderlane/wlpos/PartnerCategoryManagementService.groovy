package uk.co.wonderlane.wlpos


import grails.gorm.transactions.Transactional

@Transactional
class PartnerCategoryManagementService {
    def springSecurityService
    def categoryService
    def sessionFactory

    //Loading partner categories based on filter criteria
    List<EcomSupplierCategory> getFilterPartnerCategories(Integer retailerId, EcomSupplier ecomSupplier, String partnerCategoryNameFilter){
        List<EcomSupplierCategory> ecomSupplierCategories
        if (ecomSupplier && partnerCategoryNameFilter) { // Filter by both ecomSupplier and partnerCategoryNameFilter
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeletedAndEcomSupplierAndDescriptionLike(retailerId, false, ecomSupplier, "%${partnerCategoryNameFilter}%") ?: []
        } else if (ecomSupplier) { // Filter only by ecomSupplier
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeletedAndEcomSupplier(retailerId, false, ecomSupplier) ?: []
        } else if (partnerCategoryNameFilter) { // Filter only by partnerCategoryNameFilter
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeletedAndDescriptionLike(retailerId, false, "%${partnerCategoryNameFilter}%") ?: []
        } else { // No filters, fetch all
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeleted(retailerId, false) ?: []
        }

        return ecomSupplierCategories
    }

    //Create new supplier category
    EcomSupplierCategory createNewEcomSupplierCategory(EcomSupplier ecomSupplier, int retailerId, String partnerCategoryName){
       EcomSupplierCategory ecomSupplierCategory = new EcomSupplierCategory()
       ecomSupplierCategory.setEcomSupplier(ecomSupplier)
       ecomSupplierCategory.setRetailerId(retailerId)
       ecomSupplierCategory.setDescription(partnerCategoryName)
       return ecomSupplierCategory
    }

    //Update selected categories
    List<Category> updatedCategoryList(String selectedCategoryIds){
        List<Integer> selectedCategoryList = selectedCategoryIds?.replaceAll("[\\[\\]]", "")?.split(",")?.collect { it.trim() as Integer } ?: []
        List<Category> categories = new ArrayList<>()
        selectedCategoryList?.each { category ->
            Category selectedCategory = categoryService.getCategory(category)
            categories.add(selectedCategory) as List<Category>
        }
        return categories
    }

    //Filter out and returned removed categories
    List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings(Collection<Category> newCategories, EcomSupplierCategory ecomSupplierCategory) {
        // Fetch the existing mappings
        def existingMappings = ecomSupplierCategory?.ecomSupplierCategoryMappings
        if (!newCategories) {// If newCategories is null, return all existing mappings as they should be removed
            return existingMappings?.toList() ?: []
        }
        def newCategoryIds = newCategories*.id // Collect IDs of the new categories
        List<EcomSupplierCategoryMapping> categoriesToRemove = existingMappings.findAll { mapping ->
            !newCategoryIds.contains(mapping?.categoryId) // Find and remove mappings that are no longer in the new category list
        }
        return categoriesToRemove
    }


    //Filter out and returned all newly added categories
    List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings(EcomSupplier ecomSupplier, Collection<Category> newCategories, EcomSupplierCategory ecomSupplierCategory) {
        List<EcomSupplierCategoryMapping> newMappings = []
        // Fetch the existing mappings
        def existingMappings = ecomSupplierCategory?.ecomSupplierCategoryMappings ?: []
        def existingCategoryIds = existingMappings*.category*.id // Collect IDs of the existing categories
        newCategories.each { category -> // Find and add new mappings for categories that are not already mapped
            if (!existingCategoryIds.contains(category.id)) {
                EcomSupplierCategoryMapping newMapping = new EcomSupplierCategoryMapping()
                newMapping.setEcomSupplier(ecomSupplier)
                newMapping.setCategory(category)
                newMapping.setEcomSupplierCategory(ecomSupplierCategory)
                newMappings.add(newMapping) // Add the mapping to the list
            }
        }
        return newMappings
    }

    void updateEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory, String partnerCategoryName){
        ecomSupplierCategory.setDescription(partnerCategoryName)
    }

    void updateEcomSupplierCategoryMapping(EcomSupplierCategory ecomSupplierCategory, List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings, List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings){
        try {
            //If any category mapping is removed then removed them from supplier category association
            if (removedEcomSupplierCategoryMappings != null && removedEcomSupplierCategoryMappings.size() > 0) {
                removedEcomSupplierCategoryMappings.each { mapping ->
                    ecomSupplierCategory.removeFromEcomSupplierCategoryMappings(mapping)
                }
            }

            //If any category mapping is added then add them from supplier category association
            if (addedEcomSupplierCategoryMappings != null && addedEcomSupplierCategoryMappings.size() > 0) {
                addedEcomSupplierCategoryMappings.each { mapping ->
                    ecomSupplierCategory.addToEcomSupplierCategoryMappings(mapping)
                }
            }

            ecomSupplierCategory.save(flush: true, failOnError: true)
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            throw new RuntimeException("Error saving ecom supplier categories, exception " + ex)
        }
    }

    @Transactional
    void saveEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory){
        try {
            ecomSupplierCategory.save(flush: true, failOnError: true)
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            throw new RuntimeException("Error saving ecom supplier categories, exception " + ex)
        }
    }

    List<Category> getAvailableTopLevelCategories(EcomSupplier ecomSupplier, EcomSupplierCategory ecomSupplierCategory) {
        List<Category> categories = new ArrayList<>()
        try {
            categories = categoryService.getTopLevelCategories()

            // Get the mappings for the given supplier
            ArrayList<EcomSupplierCategoryMapping> ecomSupplierCategoryMappings = ecomSupplier?.ecomSupplierCategoryMappings ?: []
            ArrayList<EcomSupplierCategoryMapping> currentEcomSupplierCategoryMappings = []

            // If supplierCategory is provided, load the corresponding category mappings
            if (ecomSupplierCategory != null) {
                currentEcomSupplierCategoryMappings = ecomSupplierCategory?.ecomSupplierCategoryMappings ?: []
            }

            // Filter out the categories already assigned to this supplier
            List<Long> currentMappingIds = currentEcomSupplierCategoryMappings?.collect { it?.id } ?: []
            List<Category> currentAssignedTopLevelCategories = ecomSupplierCategoryMappings?.findAll { !currentMappingIds.contains(it.id)  }
                    ?.collect { it.category }
                    ?.findAll { it.parentCategory == null }

            // Return the categories that are not already assigned
            List<Category> unassignedTopLevelCategories = categories.findAll { category -> !currentAssignedTopLevelCategories.any { it.id == category.id }}

            return unassignedTopLevelCategories
        } catch (Exception ex) {
            log.error("Error loading available top level categories, Exception " + ex.getMessage(), ex)
            return categories
        }

    }


}
