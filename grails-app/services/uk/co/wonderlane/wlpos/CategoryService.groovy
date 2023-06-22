package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class CategoryService {

    def springSecurityService

    def getCategory(int categoryId) {
        return Category.findByIdAndRetailerId(categoryId, springSecurityService.principal.retailerId)
    }

    def searchCategories(String searchTerm) {
        return Category.findAllByRetailerIdAndDescriptionLike(springSecurityService.principal.retailerId, "%$searchTerm%")
    }

    def getTopLevelCategories() {
        return Category.findAllByRetailerIdAndParentCategoryIsNull(springSecurityService.principal.retailerId, [sort: 'description', order: 'asc'])
    }
}