package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType

@Transactional
class CategoryService {

    def springSecurityService

    def getCategory(int categoryId) {
        return Category.findByIdAndRetailerId(categoryId, springSecurityService.principal.retailerId)
    }

    def searchCategories(String searchTerm = null, String searchBy = "description", int offset = 0, int max = 50, String sort = "description", String order = "ASC") {
        return Category.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)

            if (searchTerm) {
                if (searchBy == "description") {
                    like("description", "%$searchTerm%")
                } else if (searchBy == "categoryCode") {
                    like("retailerCategoryCode", "%$searchTerm%")
                }
            }
        }
    }

    def searchForCategories(String description, String categoryCode, int offset = 0, int max = 50, String sort = "description", String order = "ASC") {
        return Category.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            
            if (description != null && description != "") {
                like("description", "%$description%")
            } 
            
            if (categoryCode != null && categoryCode != "") {
                like("retailerCategoryCode", "%$categoryCode%")
            }
        }
    }

    def getTopLevelCategories() {
        return Category.findAllByRetailerIdAndParentCategoryIsNull(springSecurityService.principal.retailerId, [sort: 'description', order: 'asc'])
    }

    @Transactional("reporting")
    def saveColumns(ReportColumns reportColumns) {
        reportColumns.save()
    }

    @Transactional("reporting")
    def getColumns() {
        return ReportColumns.findByUserIdAndReportType(springSecurityService.principal.id, ReportType.CATEGORY_SEARCH)
    }

    def searchCategoriesPaged(String searchTerm, int maxResults, int startIndex, String sortColumn, String sortOrder) {
        return Category.findAllByRetailerIdAndDescriptionLike(springSecurityService.principal.retailerId, "%$searchTerm%",
                [max: maxResults, sort: sortColumn, order: sortOrder, offset: startIndex])
    }

    def countCategories(String searchTerm) {
        return Category.countByRetailerIdAndDescriptionLike(springSecurityService.principal.retailerId, "%$searchTerm%")
    }

    def saveCategory(Category category) {
        category.save()
    }

    def saveRestriction(Restrictions restrictions) {
        restrictions.save()
    }

    def deleteCategory(Category category) {
        category.delete()
    }

}