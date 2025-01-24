package uk.co.wonderlane.wlpos

import org.hibernate.Session
import org.hibernate.Transaction
import grails.gorm.transactions.Transactional
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Str

@Transactional
class PartnerCategoryManagementService {
    def springSecurityService
    def categoryService
    def sessionFactory

    //Loading partner categories based on filter criteria
    List<EcomSupplierCategory> getFilterPartnerCategories(Integer retailerId, EcomSupplier ecomSupplier, String partnerCategoryNameFilter){
        List<EcomSupplierCategory> ecomSupplierCategories = []

        if (ecomSupplier && partnerCategoryNameFilter) {
            // Filter by both ecomSupplier and partnerCategoryNameFilter
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeletedAndEcomSupplierAndDescriptionLike(retailerId, false, ecomSupplier, "%${partnerCategoryNameFilter}%")
        } else if (ecomSupplier) {
            // Filter only by ecomSupplier
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeletedAndEcomSupplier(retailerId, false, ecomSupplier)
        } else if (partnerCategoryNameFilter) {
            // Filter only by partnerCategoryNameFilter
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeletedAndDescriptionLike(retailerId, false, "%${partnerCategoryNameFilter}%")
        } else {
            // No filters, fetch all
            ecomSupplierCategories = EcomSupplierCategory.findAllByRetailerIdAndDeleted(retailerId, false)
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

        // Collect IDs of the new categories
        def newCategoryIds = newCategories*.id

        // Find and remove mappings that are no longer in the new category list
        List<EcomSupplierCategoryMapping> categoriesToRemove = existingMappings.findAll { mapping ->
            !newCategoryIds.contains(mapping?.categoryId)
        }

        return categoriesToRemove
    }


    //Filter out and returned all newly added categories
    List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings(EcomSupplier ecomSupplier, Collection<Category> newCategories, EcomSupplierCategory ecomSupplierCategory) {

        List<EcomSupplierCategoryMapping> newMappings = []

        // Fetch the existing mappings
        def existingMappings = ecomSupplierCategory?.ecomSupplierCategoryMappings ?: []

        // Collect IDs of the existing categories
        def existingCategoryIds = existingMappings*.category*.id

        // Find and add new mappings for categories that are not already mapped
        newCategories.each { category ->
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

    void updateEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory, EcomSupplier ecomSupplier, String partnerCategoryName){
        ecomSupplierCategory.setEcomSupplier(ecomSupplier)
        ecomSupplierCategory.setDescription(partnerCategoryName)
        ecomSupplierCategory?.ecomSupplierCategoryMappings?.each { mapping ->
            mapping.ecomSupplier = ecomSupplier
        }
    }

    @Transactional
    void saveEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory, List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings, List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings){
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

            ecomSupplierCategory.save(flush: true)
        } catch (Exception ex) {
            log.error("Error saving partner categories, Exception " + ex.getMessage(), ex)
            throw new RuntimeException("Error saving ecom supplier categories, exception " + ex)
        }
    }




}
