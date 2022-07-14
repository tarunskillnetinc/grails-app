package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.enums.PromotionType

@Transactional
class PromotionService {

    def springSecurityService

    def savePromotion(Promotion promotion) {
        promotion.save()
    }

    def getPromotion(int promotionId) {
        def promotionCriteria = Promotion.createCriteria()

        return promotionCriteria.get() {
            eq ("id", promotionId)
            eq ("retailerId", springSecurityService.principal.retailerId)
        }
    }

    def getPromotionsForProduct(int productId) {
        Product product = Product.findByIdAndRetailerId(productId, springSecurityService.principal.retailerId)

        if (!product) {
            return null
        }

        def allSkus = product.variants?.collect { it.sku }

        def tagCriteria = Tag.createCriteria()
        def allTags = tagCriteria.list() {
            tagProducts {
                "in" ("sku", allSkus)
            }
        }

        def promotionCriteria = Promotion.createCriteria()

        def promotions = promotionCriteria.list([sort : "description",
                                                 order: "ASC"]) {

            eq("retailerId", springSecurityService.principal.retailerId)
            eq("active", true)
            lte("startDate", DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay())
            or {
                eq("endDate", null)
                gte("endDate", DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().plusDays(1))
            }
        }

        def relevantPromotions = []

        promotions.each { promotion ->
            promotion.groups.each { promotionGroup ->
                if (promotionGroup.sku && allSkus?.contains(promotionGroup.sku)) {
                    relevantPromotions.add(promotion)
                } else if (promotionGroup.categoryId && promotionGroup.categoryId == product.category.id) {
                    relevantPromotions.add(promotion)
                } else if (promotionGroup.tagId && allTags?.contains(promotionGroup.tagId)) {
                    relevantPromotions.add(promotion)
                }
            }
        }

        return relevantPromotions.unique()
    }

    def searchPromotions(int retailerId, String startDateString, String endDateString, String updatedSinceString,
                         String typeString, String searchTerm, boolean descriptionSearch, String max, String offset, Integer supplierId) {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

        DateTime startDate
        if (startDateString != null && startDateString != "") {
            startDate = DateTime.parse(startDateString, dateFormatter).withZoneRetainFields(DateTimeZone.UTC)
        }

        DateTime endDate
        if (endDateString != null && endDateString != "") {
            endDate = DateTime.parse(endDateString, dateFormatter).withZoneRetainFields(DateTimeZone.UTC)
        }

        DateTime updatedDate
        if (updatedSinceString != null && updatedSinceString != "") {
            updatedDate = DateTime.parse(updatedSinceString, dateFormatter).withZoneRetainFields(DateTimeZone.UTC)
        }

        PromotionType promotionType
        if (typeString != null && typeString != "") {
            promotionType = determinePromotionTypeFromString(typeString)
        }

        def promotions
        def criteria = Promotion.createCriteria()

        promotions = criteria.list([max: max ? Integer.parseInt(max) : 50, offset: offset ? Integer.parseInt(offset) : 0, sort: "description", order: "ASC"]) {
            eq("retailerId", retailerId)

            if (startDate != null) {
                gte("startDate", startDate)
            }

            if (endDate != null) {
                lte("endDate", endDate)
            }

            if (updatedDate != null) {
                gte("updateDatetime", updatedDate)
            }

            if (promotionType != null) {
                eq("type", promotionType)
            }

            if (supplierId != null && supplierId > 0) {
                symbolGroupPromotion {
                    symbolGroup {
                        eq("id", supplierId)
                    }
                }
            }

            if (searchTerm != null && searchTerm != "") {
                if (descriptionSearch) {
                    like("description", searchTerm)
                } else {
                    like("retailerPromotionId", searchTerm)
                }
            }
        }
        return promotions
    }

    def getSymbolGroupPromotion(Object val) {
        def symbolGroupPromotion = SymbolGroupPromotion.createCriteria()

        promotions = criteria.list([max: max ? Integer.parseInt(max) : 50, offset: offset ? Integer.parseInt(offset) : 0, sort: "description", order: "ASC"]) {
            eq("retailerId", retailerId)

            if (startDate != null) {
                gte("startDate", startDate)
            }

            if (endDate != null) {
                lte("endDate", endDate)
            }

            if (updatedDate != null) {
                gte("updateDatetime", updatedDate)
            }

            if (promotionType != null) {
                eq("type", promotionType)
            }

            if (searchTerm != null && searchTerm != "") {
                if (descriptionSearch) {
                    like("description", searchTerm)
                } else {
                    like("retailerPromotionId", searchTerm)
                }
            }
        }

        return promotions
    }

    def determinePromotionTypeFromString(String type) {
        switch (type) {
            case "BOGOF":
                return PromotionType.BOGOF
            case "X_FOR_Y":
                return PromotionType.X_FOR_Y
            case "PERCENTAGE_DISCOUNT":
                return PromotionType.PERCENTAGE_DISCOUNT
            case "FIXED_AMOUNT_DISCOUNT":
                return PromotionType.FIXED_AMOUNT_DISCOUNT
            case "FIXED_PRICE":
                return PromotionType.FIXED_PRICE
        }
    }

    def determineSupplierTypeFromString(String supplier) {
        switch (supplier) {
            case "Nisa":
                return SupplierType.NISA
            case "Costcutter":
                return SupplierType.COSTCUTTER
            case "Booker":
                return SupplierType.BOOKER
        }
    }
}