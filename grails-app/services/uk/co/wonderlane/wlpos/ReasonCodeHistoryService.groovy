package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class ReasonCodeHistoryService {

    // placeholder!

    def springSecurityService

    def getReasonCodeHistory() {
        /*
        def criteria = ReasonCodeHistory.createCriteria()
        return criteria.list {
            eq ("retailerId", springSecurityService.principal.retailerId)
        }
         */
    }

//    def saveCategoryHistories(List<CategoryHistory> history) {
//        CategoryHistory.saveAll(history)
//    }
}
