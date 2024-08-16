package uk.co.wonderlane.wlpos

import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.PromotionGroupType
import uk.co.wonderlane.wlpos.enums.PromotionType
import uk.co.wonderlane.wlpos.supplier.SymbolGroup
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class PromotionController {

    def springSecurityService

    def productService
    def promotionService
    def storeService
    def categoryService
    def tagService
    def rabbitService
    def gsonProvider

    def index() {
        if (session.addedStores) {
            session.addedStores.clear()
        }

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

    def add() {
        [promotionTypes: PromotionType.values(), canEdit: true]
    }

    def edit(int id) {
        def promotion = promotionService.getPromotion(id)

        if (!promotion) {
            flash.error = "Promotion not found"
            redirect (action: "index")
            return
        }

        def canEdit = true
        // If we're logged in at store level and promotion has stores
        if (promotion?.stores && springSecurityService.principal.storeId) {
            // Check if the user's store ID is in the list of promotion stores
            promotion.stores.each { store ->
                if (springSecurityService.principal.storeId == store.id) {
                    // If the promotion has more than one store, it should be read only
                    if (promotion.stores.size() > 1) {
                        canEdit = false
                        return // Exit the loop early as further checks are unnecessary
                    }
                }
            }
        }


        // In the current implementation, the stores on the form are stored in the session so we need to set these now otherwise we can't manipulate the list on screen correctly.
        session.addedStores = promotion?.stores


        render(view: "add", model: [promotion: promotion, promotionTypes: PromotionType.values(), canEdit: canEdit])
    }

    def ajaxSearchTags(String searchTerm) {
        def tags = tagService.getTags(searchTerm, "everything", params.offset ? Integer.parseInt(params.offset) : 0, params.max ? Integer.parseInt(params.max) : 50)

        render(template: "tagSearchResults", model: [tags: tags, searchTerm: searchTerm, searchBy: params.searchBy, max: params.max ?: 50, offset: params.offset, totalResults: tags.totalCount])
    }

    def ajaxSearchCategories(String searchTerm, String searchBy) {
        def categories = categoryService.searchCategories(searchTerm, searchBy)

        render(template: "categorySearchResults", model: [categories: categories, searchTerm: searchTerm, searchBy: searchBy, max: params.max ?: 50, offset: params.offset, totalResults: categories.totalCount])
    }

    def ajaxGetSku(long id, String promotionType, String promotionGroupType, int groupId) {
        // Note id is actually a sku.
        def productVariant = productService.getProductVariant(id)

        def (showQuantityField, showValueField) = getQuantityAndValueFieldVisibility(PromotionType.valueOf(promotionType))

        PromotionGroupCommand promotionGroup = new PromotionGroupCommand()
        promotionGroup.type = PromotionGroupType.valueOf(promotionGroupType.toUpperCase())
        promotionGroup.sku = id

        render(template: "promotionGroup", model: [promotionGroup: promotionGroup, promoGroupId: groupId, promoGroupName: "${promotionGroupType}PromoGroup-${groupId}", promotionGroupDescription: productVariant?.product?.description, promotionGroupType: promotionGroupType, showQuantityField: showQuantityField, showValueField: showValueField])
    }

    def ajaxGetTag(int id, String promotionType, String promotionGroupType, int groupId) {
        def tag = tagService.getTag(id)

        def (showQuantityField, showValueField) = getQuantityAndValueFieldVisibility(PromotionType.valueOf(promotionType))

        PromotionGroupCommand promotionGroup = new PromotionGroupCommand()
        promotionGroup.type = PromotionGroupType.valueOf(promotionGroupType.toUpperCase())
        promotionGroup.tagId = id

        render(template: "promotionGroup", model: [promotionGroup: promotionGroup, promoGroupId: groupId, promoGroupName: "${promotionGroupType}PromoGroup-${groupId}", promotionGroupDescription: tag.description, promotionGroupType: promotionGroupType, showQuantityField: showQuantityField, showValueField: showValueField])
    }

    def ajaxGetCategory(int id, String promotionType, String promotionGroupType, int groupId) {
        def category = categoryService.getCategory(id)

        def (showQuantityField, showValueField) = getQuantityAndValueFieldVisibility(PromotionType.valueOf(promotionType))

        PromotionGroupCommand promotionGroup = new PromotionGroupCommand()
        promotionGroup.type = PromotionGroupType.valueOf(promotionGroupType.toUpperCase())
        promotionGroup.categoryId = id

        render(template: "promotionGroup", model: [promotionGroup: promotionGroup, promoGroupId: groupId, promoGroupName: "${promotionGroupType}PromoGroup-${groupId}", promotionGroupDescription: category.description, promotionGroupType: promotionGroupType, showQuantityField: showQuantityField, showValueField: showValueField])
    }

    private List<Boolean> getQuantityAndValueFieldVisibility(PromotionType promotionType) {
        switch (promotionType) {
            case PromotionType.BOGOF:
                return [false, false]
            case PromotionType.FIXED_AMOUNT_DISCOUNT:
                return [true, true]
            case PromotionType.PERCENTAGE_DISCOUNT:
                return [true, false]
            case PromotionType.X_FOR_Y:
                return [true, false]
            case PromotionType.FIXED_PRICE:
                return [true, false]
        }

        return [false, false]
    }

    def save(PromotionCommand promotionCommand) {
        // If we're logged in at a store, we want to ensure the promotionCommand contains our store.
        if (springSecurityService.principal.storeId) {
            Store store = storeService.getStore(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            promotionCommand.storez.add(new PromotionStoreCommand(id: store.id, storeName: store.config.storeName, storeNumber: store.config.storeNumber))
        }

        // Deliberate use of & so that the children get validated regardless of the promotion itself being validated.
        if (promotionCommand.validate() & validateChildren(promotionCommand)) {
            Promotion promotion

            if (promotionCommand.id > 0) {
                promotion = promotionService.getPromotion(promotionCommand.id)

                if (!promotion) {
                    flash.error = "Promotion not found"
                    redirect (action: "index")
                    return
                }
            } else {
                promotion = new Promotion()
            }

            bindData(promotion, promotionCommand)

            promotion.updateDatetime = DateTime.now(DateTimeZone.UTC)
            promotion.retailerId = springSecurityService.principal.retailerId

            if (promotion.type == PromotionType.BOGOF || promotion.type == PromotionType.X_FOR_Y) {
                promotion.amount = BigDecimal.ZERO
            }

            if (promotion.type == PromotionType.BOGOF) {
                // For BOGOFs the user only selects a single item/category/tag which creates an OFFER group. We need to also add a matching REQUIRED group.
                promotionCommand.requiredGroups?.clear()

                promotionCommand.offerGroups?.each {
                    PromotionGroupCommand pgc = new PromotionGroupCommand()
                    pgc.type = PromotionGroupType.REQUIRED
                    pgc.sku = it.sku
                    pgc.categoryId = it.categoryId
                    pgc.tagId = it.tagId
                    pgc.requiredQuantity = it.requiredQuantity
                    pgc.requiredValue = it.requiredValue

                    promotionCommand.requiredGroups.add(pgc)
                }
            }

            addRemovePromotionGroups(promotion, promotionCommand, PromotionGroupType.REQUIRED)
            addRemovePromotionGroups(promotion, promotionCommand, PromotionGroupType.OFFER)
            addRemoveStores(promotion, promotionCommand)

            promotionService.savePromotion(promotion)

            try {
                sendToTills(promotion)
            } catch (Exception e) {
                flash.error = "Promotion was unable to be sent to tills."
            }

            flash.message = "Promotion added successfully."

            redirect(action: "index")
        } else {
            render (view: "add", model: [promotion: promotionCommand, promotionTypes: PromotionType.values(), canEdit: true])
        }
    }

    private void addRemovePromotionGroups(Promotion promotion, PromotionCommand promotionCommand, PromotionGroupType promotionGroupType) {
        def promotionGroups = []

        promotionCommand.getGroups(promotionGroupType)?.each {
            PromotionGroup promotionGroup = promotion?.groups?.find { pg -> pg.id == it.id && pg.type == promotionGroupType } ?: new PromotionGroup()

            bindData(promotionGroup, it)

            promotionGroups.add(promotionGroup)
        }

        // Calculate which groups were added to or removed from the promotion.
        def removedGroups = promotion.groups?.findAll { it.type == promotionGroupType && !promotionGroups?.contains(it) }
        def addedGroups = promotionGroups?.findAll { !promotion.groups?.contains(it) }

        removedGroups?.each {
            promotion.removeFromGroups(it)
        }

        addedGroups?.each {
            promotion.addToGroups(it)
        }
    }

    private void addRemoveStores(Promotion promotion, PromotionCommand promotionCommand) {
        def selectedStores = storeService.getStores(promotionCommand.storez?.collect { it.id })

        // Calculate which stores were added to or removed from the promotion.
        def removedStores = promotion.stores?.findAll { !selectedStores?.contains(it) }
        def addedStores = selectedStores?.findAll { !promotion.stores?.contains(it) }

        removedStores?.each {
            promotion.removeFromStores(it)
        }

        addedStores?.each {
            promotion.addToStores(it)
        }
    }

    private void sendToTills(Promotion promotion) {
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.PROMOTION, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, 0)
        syncMessage.setInsert(true)

        // TODO This doesn't appear to take into account the actual stores selected from the list yet?

        uk.co.wonderlane.wlpos.entities.Promotion tillPromo = promotion.getPromotion()

        List<uk.co.wonderlane.wlpos.entities.PromotionGroup> tagGroups = new ArrayList<>();
        for (uk.co.wonderlane.wlpos.entities.PromotionGroup offerGroup : tillPromo.getPromotionOfferGroups()) {
            if (offerGroup.getTagId() != null) {
                for (TagProduct tagProduct : Tag.findByIdAndRetailerId(offerGroup.tagId, springSecurityService.principal.retailerId).tagProducts) {
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
                for (TagProduct tagProduct : Tag.findByIdAndRetailerId(requiredGroup.tagId, springSecurityService.principal.retailerId).tagProducts) {
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
    }

    private boolean validateChildren(PromotionCommand promotionCommand) {
        boolean valid = true

        promotionCommand?.requiredGroups?.eachWithIndex { obj, i ->
            if (!obj.validate()) {
                valid = false
                promotionCommand.errors.reject("promotionCommand.requiredGroups.error", [(i+1)] as Object[], "")
            }

            if (obj.requiredQuantity == null && obj.requiredValue == null) {
                valid = false
                promotionCommand.errors.reject("promotionCommand.requiredGroups.quantityOrValue.error", [(i+1)] as Object[], "")
            }
        }

        promotionCommand?.offerGroups?.eachWithIndex { obj, i ->
            if (!obj.validate()) {
                valid = false
                promotionCommand.errors.reject("promotionCommand.offerGroups.error", [(i+1)] as Object[], "")
            }

            if (obj.requiredQuantity == null && obj.requiredValue == null) {
                valid = false
                promotionCommand.errors.reject("promotionCommand.offerGroups.quantityOrValue.error", [(i+1)] as Object[], "")
            }
        }

        return valid
    }

    def removeStoreFromSession(int storeId) {
        if (session.addedStores) {
            // Remove the store from session.addedStores based on the storeId
            session.addedStores = session.addedStores.findAll { it.id != storeId }
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
        def tags = Tag.findAllByRetailerIdAndDescriptionLikeAndHidden(springSecurityService.principal.retailerId, "%" + params.searchTerm + "%", false, [max: params.max ? Integer.parseInt(params.max) : 50, sort: "description", order: "asc", offset: params.offset ? Integer.parseInt(params.offset) : 0])
        def totalResults = Tag.countByRetailerIdAndDescriptionLikeAndHidden(springSecurityService.principal.retailerId, "%" + params.searchTerm + "%", false)

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

    def ajaxGetPromotionsForProduct() {
        render (view: "/product/_promotions", model: [promotions: params.productId ? promotionService.getPromotionsForProduct(Integer.parseInt(params.productId)) : []])
    }

    def ajaxAddAllStores() {
        session.addedStores = session.addedStores ?: []
        def remainingStores = getRemainingStores()
        session.addedStores.addAll(remainingStores)
        render(template: 'storeList', model: [addedStores: session.addedStores])
    }

    private List<Store> getRemainingStores() {
        def stores = Store.findAllByRetailerIdAndDeleted(springSecurityService.principal.retailerId, false)
        def addedStoreIds = session.addedStores.collect { it.id } as Set
        return stores.findAll { store ->
            !addedStoreIds.contains(store.id)
        }
    }

    def ajaxRemoveAllStores() {
        session.addedStores.clear()
        def stores = [] // Logic to remove all stores from stores
        render(template: 'storeList', model: [addedStores: stores])
    }

    def ajaxGetAllStores() {
        Integer storeNumberFilter
        String storeNameFilter
        def sortParams = [:]

        if (!params.sort) {
            sortParams = [max: 50, offset: 0, sort: "storeNumber", order: "ASC"]
        } else {
            sortParams.max = Integer.parseInt(params.max)
            sortParams.offset = Integer.parseInt(params.offset)
            sortParams.sort = params.sort
            sortParams.order = params.order
        }

        if (params.storeNumberFilter && params.storeNumberFilter.isNumber()) {
            storeNumberFilter = Integer.parseInt(params.storeNumberFilter)
        }
        if (params.storeNameFilter && params.storeNameFilter != "null") {
            storeNameFilter = params.storeNameFilter
        }

        def stores = Store.findAllByRetailerIdAndDeleted(springSecurityService.principal.retailerId, false)

        if (session.addedStores) {
            def addedStores = session.addedStores
            def addedStoreIds = []

            // Loop over addesStores and add the Ids to an array
            addedStores.each { store ->
                addedStoreIds.add(store.id)
            }

            // Remove stores from stores based on addedStoreIds
            stores.removeIf {store ->
                addedStoreIds.contains(store.id)
            }
        }

        // Filter results on storeNameFilter
        if (storeNameFilter) {
            stores.removeIf {store ->
                !store.config.storeName.toLowerCase().contains(storeNameFilter.toLowerCase())
            }
        }

        // Filter results on storeNumberFilter
        if (storeNumberFilter) {
            stores.removeIf {store ->
                !store.config.storeNumber.equals(storeNumberFilter)
            }
        }

        def paginatedStores = stores.subList(0 + sortParams.offset, Math.min(sortParams.max + sortParams.offset, stores.size()))

        render(template: 'storeSelectionList', model: [stores: paginatedStores, totalResults: stores.size(), sortParams: sortParams, storeNameFilter: storeNameFilter ?: "", storeNumberFilter: storeNumberFilter ?: ""])
    }

    def ajaxAddStores() {
        def storeIdStrings = params.list('storeIds[]')
        def storeIds = storeIdStrings.collect { Integer.parseInt(it) }

        def addedStores = storeService.getStores(storeIds) // Retrieve selected stores by IDs

        session.addedStores = session.addedStores ?: []
        session.addedStores.addAll(addedStores)

        render(template: 'storeList', model: [addedStores: session.addedStores])
    }

    def ajaxRemoveStores(int storeId) {
        session.addedStores = session.addedStores?.findAll { it.id != storeId }

        // Render the updated store list
        render(template: 'storeList', model: [addedStores: session.addedStores])
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

class PromotionCommand implements Validateable {

    Integer id
    PromotionType type
    String description
    String receiptDescription
    DateTime startDate
    DateTime endDate
    BigDecimal amount
    boolean active
    Integer retailerPromotionId
    Collection<PromotionGroupCommand> requiredGroups = new ArrayList<>()
    Collection<PromotionGroupCommand> offerGroups = new ArrayList<>()
    Collection<PromotionStoreCommand> storez = new ArrayList<>() // Renamed to avoid data binding command object to real PromotionStore object.
    SymbolGroupPromotion symbolGroupPromotion

    static constraints = {
        id nullable: true
        description nullable: false, size: 1..200
        receiptDescription nullable: false, size: 1..50
        startDate nullable: false
        endDate nullable: true
        type nullable: false
        amount nullable: true, validator: {val, obj ->
            if (obj.type == PromotionType.FIXED_PRICE) {
                BigDecimal maxValue = BigDecimal.valueOf(9999.99)

                if (val <= BigDecimal.ZERO) {
                    return 'promotionCommand.fixedPriceNotSet'
                } else if (val > maxValue) {
                    return 'promotionCommand.fixedPriceExceeded'
                }
            } else if (obj.type == PromotionType.FIXED_AMOUNT_DISCOUNT) {
                BigDecimal maxValue = BigDecimal.valueOf(9999.99)

                if (val <= BigDecimal.ZERO) {
                    return 'promotionCommand.fixedAmountNotSet'
                } else if (val > maxValue) {
                    return 'promotionCommand.fixedAmountExceeded'
                }
            } else if (obj.type == PromotionType.PERCENTAGE_DISCOUNT) {
                BigDecimal maxValue = BigDecimal.valueOf(100.00)

                if (val <= BigDecimal.ZERO) {
                    return 'promotionCommand.percentageDiscountNotSet'
                } else if (val > maxValue) {
                    return 'promotionCommand.percentageDiscountExceeded'
                }
            }
        }
        active nullable: false
        retailerPromotionId nullable: true, range: 0..999999999
        requiredGroups validator: { val, obj ->
            // Only X for Y has a separate required group.
            if (obj.type == PromotionType.X_FOR_Y) {
                if (val.size() == 0) {
                    return ['promotionCommand.requiredGroupsNotSet']
                }
            }

            return true
        }
        offerGroups validator: { val, obj ->
            if (val.size() == 0) {
                return 'promotionCommand.offerGroupsNotSet'
            }

            return true
        }
        storez validator: { val, obj ->
            if (val.size() == 0) {
                return 'promotionCommand.storesNotSet'
            }

            return true
        }
        symbolGroupPromotion nullable: true
    }

    def getGroups(PromotionGroupType promotionGroupType) {
        return promotionGroupType == PromotionGroupType.REQUIRED ? requiredGroups : offerGroups
    }
}

class PromotionGroupCommand implements Validateable {

    int id
    PromotionGroupType type
    Long sku
    Integer categoryId
    Integer tagId
    Integer requiredQuantity
    BigDecimal requiredValue

    static constraints = {
        id nullable: true
        type nullable: false
        sku nullable: true, validator: { val, obj ->
            val != null || !(obj.categoryId == null && obj.tagId == null)
        }
        categoryId nullable: true, validator: { val, obj ->
            val != null || !(obj.sku == null && obj.tagId == null)
        }
        tagId nullable: true, validator: { val, obj ->
            val != null || !(obj.sku == null && obj.categoryId == null)
        }
        requiredQuantity nullable: true, range:1..999999999
        requiredValue nullable: true, min: 0.02, max:9999.99, scale: 2
    }
}

class PromotionStoreCommand implements Validateable {

    int id
    String storeName
    String storeNumber

    // Mocking a config object so that the page doesn't need to differentiate between a real promotion and a promotion command.
    def getConfig() {
        return [storeName: storeName, storeNumber: storeNumber]
    }
}