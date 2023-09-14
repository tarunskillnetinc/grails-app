package uk.co.wonderlane.wlpos

import groovy.time.Duration
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PromotionGroupType
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.supplier.SymbolGroup
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import java.math.RoundingMode

class PromotionController {

    def springSecurityService

    def productService
    def promotionService
    def rabbitService
    def gsonProvider

    def index() {
        List<uk.co.wonderlane.wlpos.enums.PromotionType> promotionTypes = new ArrayList<>()
        promotionTypes.add(PromotionType.BOGOF)
        promotionTypes.add(PromotionType.FIXED_AMOUNT_DISCOUNT)
        promotionTypes.add(PromotionType.PERCENTAGE_DISCOUNT)
        promotionTypes.add(PromotionType.X_FOR_Y)
        promotionTypes.add(PromotionType.FIXED_PRICE)

        def symbolGroups = SymbolGroup.findAll([sort: "name", order: "asc"])

        render (view: "index", model: [
                types: promotionTypes,
                symbolGroups: symbolGroups
        ])
    }

    def maintenance() {
        List<Map> productsRequired = new ArrayList<>()
        List<Map> productsOffer = new ArrayList<>()
        List<Map> categoriesRequired = new ArrayList<>()
        List<Map> categoriesOffer = new ArrayList<>()
        List<Map> tagsRequired = new ArrayList<>()
        List<Map> tagsOffer = new ArrayList<>()

        def promo = Promotion.findById(params.promotionId)

        promo.groups.each {
            if (it.type == PromotionGroupType.REQUIRED) {
                if (it.sku != null) {
                    productsRequired.add([product: productService.getProductVariant(it.sku)?.product, quantity: it.requiredQuantity, value: it.requiredValue])
                } else if (it.categoryId != null) {
                    categoriesRequired.add([category: Category.findById(it.categoryId), quantity: it.requiredQuantity, value: it.requiredValue])
                } else {
                    tagsRequired.add([tag: Tag.findById(it.tagId), quantity: it.requiredQuantity, value: it.requiredValue])
                }
            } else {
                if (it.sku != null) {
                    productsOffer.add([product: productService.getProductVariant(it.sku)?.product, quantity: it.requiredQuantity, value: it.requiredValue])
                } else if (it.categoryId != null) {
                    categoriesOffer.add([category: Category.findById(it.categoryId), quantity: it.requiredQuantity, value: it.requiredValue])
                } else {
                    tagsOffer.add([tag: Tag.findById(it.tagId), quantity: it.requiredQuantity, value: it.requiredValue])
                }
            }
        }

        tagsRequired?.each { tagRequired ->
            tagRequired.get("tag")?.tagProducts?.each { tagProduct ->
                def productVariant = productService.getProductVariant(tagProduct.sku)

                if (productVariant?.product) {
                    tagProduct.productId = productVariant.product.id
                    tagProduct.productDescription = productVariant.product.description
                }
            }
        }

        tagsOffer?.each { tagOffer ->
            tagOffer.get("tag")?.tagProducts?.each { tagProduct ->
                def productVariant = productService.getProductVariant(tagProduct.sku)

                if (productVariant?.product) {
                    tagProduct.productId = productVariant.product.id
                    tagProduct.productDescription = productVariant.product.description
                }
            }
        }

        def productItemType = "product"
        if (categoriesRequired?.size() > 0 || categoriesOffer?.size() > 0) {
            productItemType = "category"
        } else if (tagsRequired?.size() > 0 || tagsOffer?.size() > 0) {
            productItemType = "tag"
        }

        render (view: 'maintenance', model:[promotion: promo,
                                            promoType: promo.type.toString().toLowerCase(),
                                            productsRequired: productsRequired,
                                            productsOffer: productsOffer,
                                            categoriesRequired: categoriesRequired,
                                            categoriesOffer: categoriesOffer,
                                            tagsRequired: tagsRequired,
                                            tagsOffer: tagsOffer,
                                            productItemType: productItemType])
    }

    def maintenanceError() {
        List<Map> productsRequired = new ArrayList<>()
        List<Map> productsOffer = new ArrayList<>()
        List<Map> categoriesRequired = new ArrayList<>()
        List<Map> categoriesOffer = new ArrayList<>()
        List<Map> tagsRequired = new ArrayList<>()
        List<Map> tagsOffer = new ArrayList<>()

        def promo = flash.promotion

        if (promo != null) {
            promo.groups.each {
                if (it.type == PromotionGroupType.REQUIRED) {
                    if (it.sku != null) {
                        productsRequired.add([product: productService.getProductVariant(it.sku)?.product, quantity: it.requiredQuantity, value: it.requiredValue])
                    } else if (it.categoryId != null) {
                        categoriesRequired.add([category: Category.findById(it.categoryId), quantity: it.requiredQuantity, value: it.requiredValue])
                    } else {
                        tagsRequired.add([tag: Tag.findById(it.tagId), quantity: it.requiredQuantity, value: it.requiredValue])
                    }
                } else {
                    if (it.sku != null) {
                        productsOffer.add([product: productService.getProductVariant(it.sku)?.product, quantity: it.requiredQuantity, value: it.requiredValue])
                    } else if (it.categoryId != null) {
                        categoriesOffer.add([category: Category.findById(it.categoryId), quantity: it.requiredQuantity, value: it.requiredValue])
                    } else {
                        tagsOffer.add([tag: Tag.findById(it.tagId), quantity: it.requiredQuantity, value: it.requiredValue])
                    }
                }
            }
        } else {
            promo = new Promotion()
        }

        render (view: 'maintenance', model:[promotion: promo,
                                            promoType: promo.type.toString().toLowerCase(),
                                            productsRequired: productsRequired,
                                            productsOffer: productsOffer,
                                            categoriesRequired: categoriesRequired,
                                            categoriesOffer: categoriesOffer,
                                            tagsRequired: tagsRequired,
                                            tagsOffer: tagsOffer])
    }

    def add() {
        List<Map> productsRequired = new ArrayList<>()
        List<Map> productsOffer = new ArrayList<>()
        List<Map> categoriesRequired = new ArrayList<>()
        List<Map> categoriesOffer = new ArrayList<>()
        List<Map> tagsRequired = new ArrayList<>()
        List<Map> tagsOffer = new ArrayList<>()

        render (view: 'maintenance', model:[promotion: null,
                                            promoType: 'bogof',
                                            productsRequired: productsRequired,
                                            productsOffer: productsOffer,
                                            categoriesRequired: categoriesRequired,
                                            categoriesOffer: categoriesOffer,
                                            tagsRequired: tagsRequired,
                                            tagsOffer: tagsOffer])
    }

    def setupBasePromotion(Promotion promotion, String type) {
        promotion.description = params."${type}-description"
        promotion.receiptDescription = params."${type}-receiptDescription"
        promotion.startDate = DateTimeFormat.forPattern("EEEE dd MMMM yyyy").parseDateTime(params."${type}-startDate")
        promotion.endDate = (params."${type}-doesNotExpire" ? null : DateTimeFormat.forPattern("EEEE dd MMMM yyyy").parseDateTime(params."${type}-endDate"))

        promotion.updateDatetime = new DateTime()
        promotion.active = params."${type}-active" != null
        promotion.retailerPromotionId = params."${type}-retailerPromoId" ? Integer.parseInt(params."${type}-retailerPromoId") : null

        switch (type) {
            case "bogof":
                promotion.amount = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP)
                promotion.type = PromotionType.BOGOF
                break;
            case "xfory":
                promotion.amount = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP)
                promotion.type = PromotionType.X_FOR_Y
                break;
            case "percentage":
                promotion.amount = new BigDecimal(params."percentage-amount").setScale(2, RoundingMode.HALF_UP)
                promotion.type = PromotionType.PERCENTAGE_DISCOUNT
                break;
            case "fixedAmount":
                promotion.amount = new BigDecimal(params."fixedAmount-amount").setScale(2, RoundingMode.HALF_UP)
                promotion.type = PromotionType.FIXED_AMOUNT_DISCOUNT
                break;
            case "fixedPrice":
                promotion.amount = new BigDecimal(params."fixedPrice-amount").setScale(2, RoundingMode.HALF_UP)
                promotion.type = PromotionType.FIXED_PRICE
                break;
        }


        return promotion
    }

    private Promotion failPromotion(Promotion promotion, PromotionType oldType) {
        if (oldType) {
            promotion.type = oldType
        }

        flash.promotion = promotion

        return promotion
    }

    def save() {
        def promotion = Promotion.findByRetailerIdAndId(springSecurityService.principal.retailerId, params.promotionId.equals("") ? 0 : Integer.valueOf(params.promotionId))
        def newPromotion = false
        if (promotion == null) {
            promotion = new Promotion()
            newPromotion = true

            promotion.retailerId = springSecurityService.principal.retailerId
        }

        def oldType = promotion.type

        promotion = setupBasePromotion(promotion, params.promotionType)

        if (!newPromotion) {
            if (!promotion.validate()) {
                // something is wrong already, don't go any further
                promotion = failPromotion(promotion, oldType)
                redirect(controller: "promotion", action: "maintenanceError")
                return
            }
        }

        switch (params.promotionType) {
            case "bogof":
                if (newPromotion || oldType != promotion.type || !params.boolean('bogof-noItemChange')) {
                    def promoRequiredGroup = new PromotionGroup(promotion: promotion, type: PromotionGroupType.REQUIRED, requiredQuantity: 1, sku: params."bogof-product-required-1-sku", categoryId: params."bogof-category-required-1-categoryId", tagId: params."bogof-tag-required-1-tagId", value: null)
                    def promoOfferGroup = new PromotionGroup(promotion: promotion,type: PromotionGroupType.OFFER, requiredQuantity: 1, sku: params."bogof-product-required-1-sku", categoryId: params."bogof-category-required-1-categoryId", tagId: params."bogof-tag-required-1-tagId", value: null)

                    if ((promoRequiredGroup != null && promoRequiredGroup.validate()) && (promoOfferGroup != null && promoOfferGroup.validate())) {
                        promotion.groups*.delete()
                        promotion.groups.clear()

                        promotion.addToGroups(promoRequiredGroup)
                        promotion.addToGroups(promoOfferGroup)
                    } else {
                        // something is wrong in the new groups
                        promotion = failPromotion(promotion, oldType)
                        redirect(controller: "promotion", action: "maintenanceError")
                        return
                    }
                }
                break
            case "xfory":
                if (newPromotion || oldType != promotion.type || !params.boolean('xfory-noItemChange')) {
                    def promoRequiredGroup = new PromotionGroup(promotion: promotion, type: PromotionGroupType.REQUIRED, requiredQuantity: params."xfory-${params.'xfory-promotionItemsType'}-required-1-quantity", sku: params."xfory-product-required-1-sku", categoryId: params."xfory-category-required-1-categoryId", tagId: params."xfory-tag-required-1-tagId", value: null)
                    def promoOfferGroup = new PromotionGroup(promotion: promotion, type: PromotionGroupType.OFFER, requiredQuantity: params."xfory-${params.'xfory-promotionItemsType'}-offer-1-quantity", sku: params."xfory-product-offer-1-sku", categoryId: params."xfory-category-offer-1-categoryId", tagId: params."xfory-tag-offer-1-tagId", value: null)

                    if ((promoRequiredGroup != null && promoRequiredGroup.validate()) && (promoOfferGroup != null && promoOfferGroup.validate())) {
                        promotion.groups*.delete()
                        promotion.groups.clear()

                        promotion.addToGroups(promoRequiredGroup)
                        promotion.addToGroups(promoOfferGroup)
                    } else {
                        // something is wrong in the new groups
                        promotion = failPromotion(promotion, oldType)
                        redirect(controller: "promotion", action: "maintenanceError")
                        return
                    }
                }
                break
            case "percentage":
                if (newPromotion || oldType != promotion.type || !params.boolean('percentage-noItemChange')) {
                    def promoOfferGroup = new PromotionGroup(promotion: promotion, type: PromotionGroupType.OFFER, requiredQuantity: params."percentage-${params.'percentage-promotionItemsType'}-required-1-quantity", sku: params."percentage-product-required-1-sku", categoryId: params."percentage-category-required-1-categoryId", tagId: params."percentage-tag-required-1-tagId", value: null)

                    if ((promoOfferGroup != null && promoOfferGroup.validate())) {
                        promotion.groups*.delete()
                        promotion.groups.clear()

                        promotion.addToGroups(promoOfferGroup)
                    }  else {
                        promotion = failPromotion(promotion, oldType)
                        redirect(controller: "promotion", action: "maintenanceError")
                        return
                    }
                }
                break
            case "fixedAmount":
                if (newPromotion || oldType != promotion.type || !params.boolean('fixedAmount-noItemChange')) {
                    def promoOfferGroup = new PromotionGroup(promotion: promotion, type: PromotionGroupType.OFFER, requiredQuantity: params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-quantity" == "" ? null : params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-quantity", sku: params."fixedAmount-product-required-1-sku", categoryId: params."fixedAmount-category-required-1-categoryId", tagId: params."fixedAmount-tag-required-1-tagId", value: params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-value" == "" ? null : new BigDecimal(params."fixedAmount-${params.'fixedAmount-promotionItemsType'}-required-1-value").setScale(2, RoundingMode.HALF_UP))

                    if ((promoOfferGroup != null && promoOfferGroup.validate())) {
                        promotion.groups*.delete()
                        promotion.groups.clear()

                        promotion.addToGroups(promoOfferGroup)
                    } else {
                        // something is wrong in the new groups
                        promotion = failPromotion(promotion, oldType)
                        redirect(controller: "promotion", action: "maintenanceError")
                        return
                    }
                }
                break
            case "fixedPrice":
                def itemNo = Integer.parseInt(params.'fixedPrice-count-required')

                if (newPromotion || oldType != promotion.type || !params.boolean('fixedPrice-noItemChange')) {
                    def promoOfferGroups = new ArrayList<PromotionGroup>()

                    def count = 1
                    for (int i = 0; i < itemNo; i++) {

                        def param
                        if (params."fixedPrice-promotionItemsType" == "product") {
                            param = $/fixedPrice-${params."fixedPrice-promotionItemsType"}-required-${count}-sku/$
                        } else {
                            param = $/fixedPrice-${params."fixedPrice-promotionItemsType"}-required-${count}-${params."fixedPrice-promotionItemsType"}Id/$
                        }

                        while(!params.containsKey(param.toString())) {
                            count++
                            param = $/fixedPrice-${params."fixedPrice-promotionItemsType"}-required-${count}-${params."fixedPrice-promotionItemsType"}Id/$
                        }
                        promoOfferGroups.add(new PromotionGroup(promotion: promotion, type: PromotionGroupType.OFFER, requiredQuantity: params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-${count}-quantity" == "" ? null : params."fixedPrice-${params.'fixedPrice-promotionItemsType'}-required-${count}-quantity", sku: params."fixedPrice-product-required-${count}-sku", categoryId: params."fixedPrice-category-required-${count}-categoryId", tagId: params."fixedPrice-tag-required-${count}-tagId", value: null))
                        count++
                    }

                    if (!promoOfferGroups.isEmpty()) {
                        def validationError = false
                        promoOfferGroups.each {
                            if(!it.validate()) {
                                validationError = true
                            }
                        }

                        if (!validationError) {
                            promotion.groups*.delete()
                            promotion.groups.clear()

                            promoOfferGroups.each {
                                promotion.addToGroups(it)
                            }
                        } else {
                            promotion = failPromotion(promotion, oldType)
                            redirect(controller: "promotion", action: "maintenanceError")
                            return
                        }
                    } else {
                        promotion = failPromotion(promotion, oldType)
                        redirect(controller: "promotion", action: "maintenanceError")
                        return
                    }
                }
                break
        }

        if (promotion.validate()) {
            // Client formats the Date Time without the Hours, Minutes, or Seconds, we can safely pad the saved date time, every time.
            promotion.setEndDate(promotion.getEndDate().plusHours(23).plusMinutes(59).plusSeconds(59))
            promotionService.savePromotion(promotion)

            redirect(controller: "promotion", action: "sendToTill" , params: [promotionId: promotion.id])
            return
        } else {
            promotion = failPromotion(promotion, oldType)
            redirect(controller: "promotion", action: "maintenanceError")
            return
        }
    }

    def productSearch() {
        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")

        render(template: "/promotion/productSearchResults", model: [products: products.products, storeId: springSecurityService.principal.storeId, searchTerm: params.searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: products.totalCount])
    }
    
    def categorySearch() {
        def categories
        def totalResults

        if (params.searchBy.equals("description")) {
            categories = Category.findAllByRetailerIdAndDescriptionLike(springSecurityService.principal.retailerId, "%" + params.searchTerm + "%", [max: params.max ? Integer.parseInt(params.max) : 50, sort: "description", order: "asc", offset: params.offset ? Integer.parseInt(params.offset) : 0])
            totalResults = Category.countByRetailerIdAndDescriptionLike(springSecurityService.principal.retailerId, "%" + params.searchTerm + "%")
        } else {
            categories = Category.findAllByRetailerIdAndRetailerCategoryCodeLike(springSecurityService.principal.retailerId, "%" + params.searchTerm + "%", [max: params.max ? Integer.parseInt(params.max) : 50, sort: "retailerCategoryCode", order: "asc", offset: params.offset ? Integer.parseInt(params.offset) : 0])
            totalResults = Category.countByRetailerIdAndRetailerCategoryCodeLike(springSecurityService.principal.retailerId, "%" + params.searchTerm + "%")
        }

        render(template: "/promotion/categorySearchResults", model: [categories: categories, storeId: springSecurityService.principal.storeId, searchTerm: params.searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: totalResults])
    }

    def tagSearch() {
        def tags = Tag.findAllByDescriptionLikeAndHidden("%" + params.searchTerm + "%", false, [max: params.max ? Integer.parseInt(params.max) : 50, sort: "description", order: "asc", offset: params.offset ? Integer.parseInt(params.offset) : 0])
        def totalResults = Tag.countByDescriptionLikeAndHidden("%" + params.searchTerm + "%", false)

        render(template: "/promotion/tagSearchResults", model: [tags: tags, storeId: springSecurityService.principal.storeId, searchTerm: params.searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: totalResults])
    }

    def promotionSearch() {
        DateTime validDate
        DateTime updatedSince
        PromotionType type
        Integer supplierId
        Integer max
        Integer offset
        String sortColumn
        String sortOrder

        try {
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

            validDate = params.validDate ? DateTime.parse(params.validDate, dateFormatter).withZoneRetainFields(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC)
            updatedSince = params.updatedSince ? DateTime.parse(params.updatedSince, dateFormatter).withZoneRetainFields(DateTimeZone.UTC) : null
            type = params.type ? PromotionType.valueOf(params.type) : null
            supplierId = params.supplier ? Integer.parseInt(params.supplier) : null
            max = params.max ? Integer.parseInt(params.max) : null
            offset = params.offset ? Integer.parseInt(params.offset) : null
            sortColumn = validateSortColumn(params.sortColumn)
            sortOrder = validateSortOrder(params.sortOrder)
        } catch (Exception e) {
            e.printStackTrace()

            response.status = 400
            return
        }

        session.PROMOTION_SEARCH_TERM = params.searchTerm

        // Having to completely custom do the sorting and pagination when sorting by supplier name.
        if (sortColumn == "supplierName") {
            offset = 0
            max = 10000
        }

        def promotions = promotionService.searchPromotions(validDate, updatedSince, type, params.searchTerm, params.searchBy == "description",
                max, offset, sortColumn, sortOrder, supplierId, params.status)

        int totalCount = promotions.totalCount

        // Having to completely custom do the sorting and pagination when sorting by supplier name.
        if (sortColumn == "supplierName") {
            max = params.max ? Integer.parseInt(params.max) : 50
            offset = params.offset ? Integer.parseInt(params.offset) : 0

            promotions = promotions?.sort { a, b -> ("" + a?.symbolGroupPromotion?.symbolGroup?.name + a?.symbolGroupPromotion?.isLeaflet) <=> ("" + b?.symbolGroupPromotion?.symbolGroup?.name + b?.symbolGroupPromotion?.isLeaflet) }

            if (sortOrder == "asc") {
                promotions = promotions?.reverse()
            }

            promotions = offset < promotions.size() ? promotions.subList(offset, (offset + max < promotions.size() ? offset + max : promotions.size())) : []
        }

        render(template: "/promotion/promotionSearchResults", model: [promotions  : promotions,
                                                                      storeId     : springSecurityService.principal.storeId,
                                                                      validDate   : params.validDate,
                                                                      updatedSince: params.updatedSince,
                                                                      type        : params.type,
                                                                      searchTerm  : params.searchTerm,
                                                                      searchBy    : params.searchBy,
                                                                      max         : params.max,
                                                                      offset      : params.offset,
                                                                      sortOrder   : params.sortOrder,
                                                                      sortColumn  : params.sortColumn,
                                                                      supplier    : params.supplier,
                                                                      status      : params.status,
                                                                      totalResults: totalCount])
    }

    def sendToTill() {
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.PROMOTION, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
        syncMessage.setInsert(true)

        uk.co.wonderlane.wlpos.entities.Promotion tillPromo = Promotion.findById(Integer.parseInt(params.promotionId)).getPromotion()

        List<uk.co.wonderlane.wlpos.entities.PromotionGroup> tagGroups = new ArrayList<>();
        for (uk.co.wonderlane.wlpos.entities.PromotionGroup offerGroup : tillPromo.getPromotionOfferGroups()) {
            if (offerGroup.getTagId() != null) {
                for (TagProduct tagProduct : Tag.findById(offerGroup.tagId).tagProducts) {
                    uk.co.wonderlane.wlpos.entities.PromotionGroup promotionGroup = new uk.co.wonderlane.wlpos.entities.PromotionGroup()
                    promotionGroup.setId(offerGroup.getId())
                    promotionGroup.setPromotionId(offerGroup.getPromotionId())
                    promotionGroup.setRequiredValue(offerGroup.getRequiredValue())
                    promotionGroup.setType(offerGroup.getType())
                    promotionGroup.setRequiredQuantity(offerGroup.getRequiredQuantity())
                    promotionGroup.setExcessQuantity(offerGroup.isExcessQuantity())
                    promotionGroup.setTagId(offerGroup.getTagId())
                    promotionGroup.setSku(tagProduct.sku)

                    tagGroups.add(promotionGroup)
                }
            }
        }

        tillPromo.getPromotionOfferGroups().addAll(tagGroups)
        tagGroups.clear()

        for (uk.co.wonderlane.wlpos.entities.PromotionGroup requiredGroup : tillPromo.getPromotionRequiredGroups()) {
            if (requiredGroup.getTagId() != null) {
                for (TagProduct tagProduct : Tag.findById(requiredGroup.tagId).tagProducts) {
                    uk.co.wonderlane.wlpos.entities.PromotionGroup promotionGroup = new uk.co.wonderlane.wlpos.entities.PromotionGroup()
                    promotionGroup.setId(requiredGroup.getId())
                    promotionGroup.setPromotionId(requiredGroup.getPromotionId())
                    promotionGroup.setRequiredValue(requiredGroup.getRequiredValue())
                    promotionGroup.setType(requiredGroup.getType())
                    promotionGroup.setRequiredQuantity(requiredGroup.getRequiredQuantity())
                    promotionGroup.setExcessQuantity(requiredGroup.isExcessQuantity())
                    promotionGroup.setTagId(requiredGroup.getTagId())
                    promotionGroup.setSku(tagProduct.sku)

                    tagGroups.add(promotionGroup)
                }
            }
        }

        tillPromo.getPromotionRequiredGroups().addAll(tagGroups)

        syncMessage.setPromotion(tillPromo)

        rabbitService.sendMessage(syncMessage)

        flash.message = "Promotion saved successfully"
        redirect(action: "index", params: params)
    }

    def ajaxGetPromotionsForProduct() {
        render (view: "/product/_promotions", model: [promotions: params.productId ? promotionService.getPromotionsForProduct(Integer.parseInt(params.productId)) : []])
    }

    private String validateSortColumn(String sortColumn) {
        def availableColumns = [ "retailerPromotionId", "description", "updateDatetime", "startDate", "endDate", "active", "type", "amount", "supplierName" ]

        if (!sortColumn) {
            return null
        } else if (availableColumns.contains(sortColumn)) {
            return sortColumn
        } else {
            throw new RuntimeException("Bad request")
        }
    }

    private String validateSortOrder(String sortOrder) {
        def availableOrders = [ "asc", "desc" ]

        if (!sortOrder) {
            return null
        } else if (availableOrders.contains(sortOrder.toLowerCase())) {
            return sortOrder
        } else {
            throw new RuntimeException("Bad request")
        }
    }
}