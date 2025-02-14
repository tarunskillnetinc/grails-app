package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class CategoryHistoryService {

    def springSecurityService
    
    def getCategoryHistory(int categoryId, DateTime dateTime) {
        def criteria = CategoryHistory.createCriteria()
        return criteria.list {
            eq("categoryId", categoryId)
            eq ("retailerId", springSecurityService.principal.retailerId)
            lte("effectiveDate", dateTime)
        }
    }
    
    def saveCategoryHistories(List<CategoryHistory> history) {
        CategoryHistory.saveAll(history)
    }
}
