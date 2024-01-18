package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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
            eq("id", promotionId)
            eq("retailerId", springSecurityService.principal.retailerId)
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
                "in"("sku", allSkus)
            }
        }

        //loop over tags to get all tag ids
        def tagIds = []
        tagIds = allTags?.collect { Tag it -> it.id }

        def promotionCriteria = Promotion.createCriteria()

        def promotions = promotionCriteria.list([sort: "description", order: "ASC"]) {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("active", true)
            lte("startDate", DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay())
            or {
                isNull("endDate")
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
                } else if (promotionGroup.tagId && tagIds?.contains(promotionGroup.tagId)) {
                    relevantPromotions.add(promotion)
                }
            }
        }

        return relevantPromotions.unique()
    }

    def searchPromotions(DateTime validDate, DateTime updatedSince, PromotionType promotionType, String searchTerm, boolean descriptionSearch,
                         Integer max, Integer offset, String sortColumn, String sortOrder, Integer supplierId, String status, boolean loyaltyOnly) {

        max = max ?: 50
        offset = offset ?: 0

        def promotions
        def criteria = Promotion.createCriteria()

        promotions = criteria.list([max: max, offset: offset]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (validDate != null) {
                lte("startDate", validDate)
                or {
                    isNull('endDate')
                    gte("endDate", validDate)
                }

            }

            if (updatedSince != null) {
                gte("updateDatetime", updatedSince)
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

            if (status != null && !status.isBlank()) {
                eq("active", status == "ACTIVE")
            }

            if (loyaltyOnly) {
                eq("loyalty", true)
            }

            if (searchTerm != null && searchTerm != "") {
                if (descriptionSearch) {
                    like("description", "%$searchTerm%")
                } else if (searchTerm.isNumber()) {
                    sqlRestriction "cast( retailerPromotionId AS char( 256 )) like '%${searchTerm}%'";
                } else {
                    // This block is only hit when the user selects to search by promotion reference but then enters a non-numeric entry in the search box.
                    like("description", "%$searchTerm%")
                }
            }

            if (sortColumn != "supplierName") {
                order(sortColumn ?: "description", sortOrder ?: "asc")
            }
        }

        return promotions
    }
}