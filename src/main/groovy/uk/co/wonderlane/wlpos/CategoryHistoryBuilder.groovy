package uk.co.wonderlane.wlpos;

import grails.plugin.springsecurity.SpringSecurityService
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.wonderlane.wlpos.PricingClassification
import uk.co.wonderlane.wlpos.enums.CategoryHistoryType
import uk.co.wonderlane.wlpos.enums.StockClassification

class CategoryHistoryBuilder {

    def springSecurityService
    def categoryHistories = new ArrayList<CategoryHistory>()
    def categoryId
    def now = DateTime.now(DateTimeZone.UTC)
    def effectiveDate

    CategoryHistoryBuilder(Integer categoryId, SpringSecurityService springSecurityService) {
        this.categoryId = categoryId
        this.springSecurityService = springSecurityService
        this.effectiveDate = now.withTimeAtStartOfDay()
    }

    def compare(String property, Object left, Object right) {
        compare(property, left, right, CategoryHistoryType.FIELD)
    }

    def compare(String property, Object left, Object right, CategoryHistoryType categoryHistoryType) {
        if (left != right && right != null) {
            def categoryHistory = new CategoryHistory()

            categoryHistory.retailerId = springSecurityService.principal.retailerId
            categoryHistory.categoryId = categoryId
            categoryHistory.storeId = springSecurityService.principal.storeId
            categoryHistory.updateDate = now
            categoryHistory.effectiveDate = effectiveDate
            categoryHistory.type = categoryHistoryType
            categoryHistory.field = property

            if (left instanceof StockClassification) {
                categoryHistory.fromValue = getStockClassification(left)
                categoryHistory.toValue = getStockClassification(right)
            } else if (left instanceof PricingClassification) {
                categoryHistory.fromValue = left.classification
                categoryHistory.toValue = right.classification
            } else {
                categoryHistory.fromValue = left.toString().substring(0, left.toString().length() > 100 ? 99 : left.toString().length())
                categoryHistory.toValue = right.toString().substring(0, right.toString().length() > 100 ? 99 : right.toString().length())
            }
            
            categoryHistory.userId = springSecurityService.principal.id
            categoryHistory.usersName = springSecurityService.principal.usersName

            categoryHistories.add(categoryHistory)
        }
    }

    def add(CategoryHistoryType categoryHistoryType) {
        def categoryHistory = new CategoryHistory()

        categoryHistory.retailerId = springSecurityService.principal.retailerId
        categoryHistory.categoryId = categoryId
        categoryHistory.storeId = springSecurityService.principal.storeId
        categoryHistory.updateDate = now
        categoryHistory.effectiveDate = effectiveDate
        categoryHistory.type = categoryHistoryType
        categoryHistory.userId = springSecurityService.principal.id
        categoryHistory.usersName = springSecurityService.principal.usersName

        categoryHistories.add(categoryHistory)
    }
    
    String getStockClassification(StockClassification stockClassification) {
        switch(stockClassification) {
            case StockClassification.STANDARD:
                return "Standard"
            case StockClassification.NOSTOCK_NOSALE:
                return "No stock, no sale"
            case StockClassification.NOSTOCK_ALLOWSALE:
                return "No stock, allow sale"
            default:
                return ""
        }
    }
}