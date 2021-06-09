package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class CategoryService {

    def springSecurityService

    def getCategory(int categoryId) {
        return Category.findByIdAndRetailerId(categoryId, springSecurityService.principal.retailerId)
    }

    /**
     * Returns the category hierarchy down to the provided category ID, with department as the first entry.
     *
     * @param categoryId
     * @return
     */
    def getCategoryHierarchy(int categoryId) {
        def categories = []

        def category = getCategory(categoryId)

        categories.add(category)

//        while (category?.parentId) {
//            category = getCategory(category.parentId)
//
//            categories.add(0, category)
//        }

        return categories
    }

    def getFullCategoryHierarchy() {
        return Category.findAllByRetailerIdAndParentCategory(springSecurityService.principal.retailerId, null, [sort: 'description', order: 'asc'])
    }
}