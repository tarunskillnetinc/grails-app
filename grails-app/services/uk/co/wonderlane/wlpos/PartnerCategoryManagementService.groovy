package uk.co.wonderlane.wlpos

import org.hibernate.Session
import org.hibernate.Transaction
import grails.gorm.transactions.Transactional

@Transactional
class PartnerCategoryManagementService {
    def springSecurityService
    def categoryService
    def sessionFactory

    EcomSupplierCategory createNewEcomSupplierCategory(EcomSupplier ecomSupplier, int retailerId, String partnerCategoryName){
       EcomSupplierCategory ecomSupplierCategory = new EcomSupplierCategory()
       ecomSupplierCategory.setEcomSupplier(ecomSupplier)
       ecomSupplierCategory.setRetailerId(retailerId)
       ecomSupplierCategory.setDescription(partnerCategoryName)
       return ecomSupplierCategory
    }


    List<Category> updatedCategoryList(List<Integer> selectedCategoryList){
        List<Category> categories = new ArrayList<>()
        selectedCategoryList?.each { category ->
            Category selectedCategory = categoryService.getCategory(category)
            categories.add(selectedCategory) as List<Category>
        }
        return categories
    }

    List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings(Collection<Category> newCategories, EcomSupplierCategory ecomSupplierCategory) {
        // Fetch the existing mappings
        def existingMappings = ecomSupplierCategory.ecomSupplierCategoryMappings

        // Collect IDs of the new categories
        def newCategoryIds = newCategories*.id

        // Find and remove mappings that are no longer in the new category list
        List<EcomSupplierCategoryMapping> categoriesToRemove = existingMappings.findAll { mapping ->
            !newCategoryIds.contains(mapping?.categoryId)
        }

        return categoriesToRemove
    }


    List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings(EcomSupplier ecomSupplier, Collection<Category> newCategories, EcomSupplierCategory ecomSupplierCategory) {

        List<EcomSupplierCategoryMapping> newMappings = []

        // Fetch the existing mappings
        def existingMappings = ecomSupplierCategory.ecomSupplierCategoryMappings

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

    @Transactional
    void saveEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory, List<EcomSupplierCategoryMapping> removedEcomSupplierCategoryMappings, List<EcomSupplierCategoryMapping> addedEcomSupplierCategoryMappings){

        if (removedEcomSupplierCategoryMappings != null && removedEcomSupplierCategoryMappings.size() > 0) {
            removedEcomSupplierCategoryMappings.each { mapping ->
//            EcomSupplierCategoryMapping.executeUpdate(
//                    "DELETE FROM EcomSupplierCategoryMapping " +
//                            "WHERE ecomSupplierCategoryId = :ecomSupplierCategoryId and ecomSupplierId = :ecomSupplierId and categoryId = :categoryId",
//                    [ecomSupplierCategoryId: mapping.ecomSupplierCategoryId, ecomSupplierId: mapping.ecomSupplierId, categoryId: mapping.categoryId]
//            )
                ecomSupplierCategory.removeFromEcomSupplierCategoryMappings(mapping)
                println "After remove: ${mapping.ecomSupplierCategory}"
            }
        }

        if (addedEcomSupplierCategoryMappings != null && addedEcomSupplierCategoryMappings.size() > 0) {
            addedEcomSupplierCategoryMappings.each { mapping ->
                ecomSupplierCategory.addToEcomSupplierCategoryMappings(mapping)
            }
        }

        ecomSupplierCategory.save(flush: true)
    }


}
