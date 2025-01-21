package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.entities.transactionv2.Transaction

@Transactional
class PartnerCategoryManagementService {
    def springSecurityService
    def categoryService

   EcomSupplierCategory createNewEcomSupplierCategory(EcomSupplier ecomSupplier, int retailerId, String partnerCategoryName){
       EcomSupplierCategory ecomSupplierCategory = new EcomSupplierCategory()
       ecomSupplierCategory.setEcomSupplier(ecomSupplier)
       ecomSupplierCategory.setRetailerId(retailerId)
       ecomSupplierCategory.setDescription(partnerCategoryName)
       return ecomSupplierCategory
   }

    List<Category> updatedCategoryList(int categoryId){
        List<Category> categories = new ArrayList<>()
        Category selectedCategory = categoryService.getCategory(categoryId)
        categories.add(selectedCategory) as List<Category>
        return categories
    }

    @Transactional
    void updateEcomSupplierCategoryMappings(EcomSupplier ecomSupplier, Collection<Category> newCategories, EcomSupplierCategory ecomSupplierCategory) {
        // Fetch the existing mappings
        def existingMappings = ecomSupplierCategory.ecomSupplierCategoryMappings

        // Collect IDs of the new categories
        def newCategoryIds = newCategories*.id

        // Find and remove mappings that are no longer in the new category list
        def categoriesToRemove = existingMappings.findAll { mapping ->
            !newCategoryIds.contains(mapping?.categoryId)
        }

        categoriesToRemove.each { mapping ->
//            EcomSupplierCategoryMapping.executeUpdate(
//                    "DELETE FROM EcomSupplierCategoryMapping WHERE ecomSupplierCategory.id = :ecomSupplierCategoryId AND ecomSupplier.id = :ecomSupplierId AND category.id = :categoryId",
//                    [ecomSupplierCategoryId: mapping.ecomSupplierCategory.id, ecomSupplierId: mapping.ecomSupplier.id, categoryId: mapping.category.id]
//            )
            //mapping.delete(flush: true)
            //ecomSupplierCategory.removeFromEcomSupplierCategoryMappings(mapping)
        }

        // Collect IDs of the existing categories
        def existingCategoryIds = existingMappings*.category*.id

        // Find and add new mappings for categories that are not already mapped
        newCategories.each { category ->
            if (!existingCategoryIds.contains(category.id)) {
                EcomSupplierCategoryMapping newMapping = new EcomSupplierCategoryMapping()
                newMapping.setEcomSupplier(ecomSupplier)
                newMapping.setCategory(category)
                newMapping.setEcomSupplierCategory(ecomSupplierCategory)
                ecomSupplierCategory.addToEcomSupplierCategoryMappings(newMapping)
            }
        }

        println ecomSupplierCategory

       // return ecomSupplierCategory
    }

    @Transactional
    void saveEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory){
        ecomSupplierCategory.save(flush: true)
    }


}
