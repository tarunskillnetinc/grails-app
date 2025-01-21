package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class PartnerCategoryManagementService {
    def springSecurityService

   EcomSupplierCategory createNewEcomSupplierCategory(EcomSupplier ecomSupplier, int retailerId, String partnerCategoryName){
       EcomSupplierCategory ecomSupplierCategory = new EcomSupplierCategory()
       ecomSupplierCategory.setEcomSupplier(ecomSupplier)
       ecomSupplierCategory.setRetailerId(retailerId)
       ecomSupplierCategory.setDescription(partnerCategoryName)
       return ecomSupplierCategory
   }

    EcomSupplierCategoryMapping createNewEcomSupplierCategoryMapping(EcomSupplier ecomSupplier, Category category, EcomSupplierCategory ecomSupplierCategory ){
        EcomSupplierCategoryMapping ecomSupplierCategoryMapping = new EcomSupplierCategoryMapping()
        ecomSupplierCategoryMapping.setEcomSupplier(ecomSupplier)
        ecomSupplierCategoryMapping.setCategory(category)
        ecomSupplierCategoryMapping.setEcomSupplierCategory(ecomSupplierCategory)
        return ecomSupplierCategoryMapping
    }

    @Transactional
    void saveEcomSupplierCategory(EcomSupplierCategory ecomSupplierCategory){
        ecomSupplierCategory.save(flush: true)
    }


}
