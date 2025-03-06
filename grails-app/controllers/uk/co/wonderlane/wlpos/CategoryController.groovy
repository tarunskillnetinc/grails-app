package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.CategoryHistoryType
import uk.co.wonderlane.wlpos.enums.StockClassification
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import java.math.RoundingMode

class CategoryController extends BaseController {

    def springSecurityService
    def rabbitService
    def categoryHistoryService
    def pricingClassificationService
    def productService
    def storeService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }

        if (session.CATEGORY_PRODUCT_LIST != null) {
            session.setAttribute('CATEGORY_PRODUCT_LIST', null)
        }

        [userColumns: categoryService.getColumns()]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveColumns() {
        super.ajaxSaveColumns()
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchCategories() {
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        String searchTerm = params.searchTerm
        String categoryCode = params.categoryCode

        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def searchResults = baseSearchForCategories(searchTerm, categoryCode)
        def topLevelCats = searchResults.getaValue().drop(offset).take(max)
        def matchingCats = searchResults.getbValue()

        render(template: "categorySearchResults", model: [topLevelCategories : topLevelCats.unique(),
                                                          matchedCategories: searchTerm.isEmpty() ? null : matchingCats,
                                                          storeId     : springSecurityService.principal.storeId,
                                                          userColumns : categoryService.getColumns(),
                                                          searchTerm  : searchTerm,
                                                          max         : max,
                                                          offset      : offset,
                                                          totalResults: searchResults.getaValue().size()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchMaintenanceCategories(String searchTerm, boolean triggerOnCategoryChange, int level, int selectedCategoryId) {
        def searchResults = baseSearchCategories(searchTerm)
        boolean isSearch = searchTerm?.length() > 0
        render(template: "/product/categorySelectInputs", model: [categories: searchResults.aValue.unique(), level: isSearch ? level : 1, productCategoryList: searchResults.bValue, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange, isSearch: isSearch])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetInheritance(int selectedCategoryId) {
        def parentCategory = categoryService.getCategory(selectedCategoryId)
        render(template:"restrictions", model: [category: parentCategory])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def getColumns() {
        return categoryService.getColumns()
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def add() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }
        
        def blankCategory = new Category()
        blankCategory.setRestrictions(new Restrictions())
        def pricingClassification = pricingClassificationService.getPricingClassificationsForRetailer()

        // default values for new category:
        blankCategory.varianceQuantity = 10
        blankCategory.varianceValue = new BigDecimal(100).setScale(2, RoundingMode.HALF_UP)
        blankCategory.restrictions.saleAllowed = true
        blankCategory.restrictions.buyerAgeRestriction = 0
        blankCategory.restrictions.buyerChallengeAge = 0
        blankCategory.restrictions.sellerAgeRestriction = 0
        blankCategory.restrictions.allowPriceChange = true
        blankCategory.restrictions.refundAllowed = true
        blankCategory.restrictions.markdownAllowed = true
        blankCategory.restrictions.discountAllowed = true
        blankCategory.restrictions.maximumMarkdownPercentage = new BigDecimal(90).setScale(2, RoundingMode.HALF_UP)
        blankCategory.restrictions.quantityChangeAllowed = true
        blankCategory.restrictions.quantityChangeRestriction = 5
        blankCategory.restrictions.promptedDaysFrom = 7
        blankCategory.restrictions.stockClassification = StockClassification.STANDARD
        def loyaltyEnabled = springSecurityService.principal.retailer.config?.loyaltyRetailerConfig?.isLoyaltyEnabled ? true : false

        render(view: "maintenance", model: [category: blankCategory, addCategory: true, topLevelCategories: categoryService.getTopLevelCategories(), loyaltyEnabled: loyaltyEnabled, pricingClassifications: pricingClassification])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def save(CategoryCommand editedCategory) {
        def builder = null
        def addingCategory = false
        def category = null
        def categoryId = tryParseInt(params.get("id").toString())
        def pricingClassification = pricingClassificationService.getPricingClassificationsForRetailer()

        if (categoryId.isPresent()) {
            category = categoryService.getCategory(categoryId.get())
        }

        if (category == null) {
            category = new Category()
            category.retailerId = springSecurityService.principal.retailerId
            category.restrictions = new Restrictions()
            addingCategory = true
        } else {
            builder = new CategoryHistoryBuilder(category.id, springSecurityService)
            doComparison(builder, category, editedCategory)
        }

        bindData(category, params)

        /* Backward logic means bind data will not have the correct value */
        category.restrictions.saleAllowed = !params.boolean('restrictions.saleAllowed')

        //Check for a Parent Category being selected.
        def parentId = tryParseInt(params.get("category.id"))
        if (parentId.isPresent()) {
            def parentCategorySearch = categoryService.getCategory(parentId.get())
            // Make sure we're not saving the same ID otherwise we'll spin forever
            if (parentCategorySearch != null) {
                if (parentCategorySearch.id == category.id) {
                    category.errors.reject('category.parentCategory.notUnique')
                } else if (parentCategorySearch.retailerCategoryCode == null) {
                    category.errors.reject('category.parentCategory.categorycode')
                } else if (parentIsSubCategory(category.id, parentId.get())) {
                    category.errors.reject('category.parentCategory.subcategory.error')
                } else {
                    category.parentCategory = parentCategorySearch
                }
            } else {
                category.parentCategory = null
            }
        } else {
            category.parentCategory = null
        }

        if (category.hasErrors()) {
            render(view: "maintenance", model: [category: category, addCategory: false, topLevelCategories: categoryService.getTopLevelCategories(), pricingClassifications: pricingClassification])
            return
        }

        if (!addingCategory) {
            def restriction = Restrictions.findById(category.restrictions.id)
            categoryService.saveRestriction(restriction)

            if (restriction.hasErrors()) {
                render(view: "maintenance", model: [category: category, restrictions: restriction, addCategory: false, topLevelCategories: categoryService.getTopLevelCategories(), pricingClassifications: pricingClassification])
                return
            }
        } else {
            categoryService.saveRestriction(category.restrictions)

            if (category.restrictions.hasErrors()) {
                render(view: "maintenance", model: [category: category, restrictions: category.restrictions, addCategory: false, topLevelCategories: categoryService.getTopLevelCategories(), pricingClassifications: pricingClassification])
                return
            }
        }

        categoryService.saveCategory(category)

        if (!category.hasErrors()) {
            // Send the category to Rabbit to be inserted / updated in the tills
            sendMessageToRabbit(true, category)
            
            if (addingCategory) {
                builder = new CategoryHistoryBuilder(category.id, springSecurityService)
                builder.add(CategoryHistoryType.NEW_CATEGORY)
            }

            if (builder && builder.categoryHistories) {
                categoryHistoryService.saveCategoryHistories(builder.categoryHistories)
            }

            flash.message = "Category ${category.description} saved successfully"
            redirect("controller": "category", action:"index")
        } else {
            def categoryList = []
            def tempCategory = category

            while (tempCategory) {
                categoryList.add(tempCategory.id)

                tempCategory = tempCategory.parentCategory
            }

            render(view: "maintenance", model: [category: category, restrictions: category.restrictions, addCategory: false, categoryList: categoryList, topLevelCategories: categoryService.getTopLevelCategories(), pricingClassifications: pricingClassification])
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxDeleteCategory(int categoryId) {
        def category = categoryService.getCategory(categoryId)

        // Search for any products that use this category ID
        def products = Product.findAllByCategory(category)
        if (products.size() != 0) {
            render(template: "maintenanceForm", model: [category: category, hasProducts: true, topLevelCategories: categoryService.getTopLevelCategories()])
            return
        }

        // Check whether this category has any children
        if (category.childCategories.size() > 0) {
            render(template: "maintenanceForm", model: [category: category, hasChildren: true, topLevelCategories: categoryService.getTopLevelCategories()])
            return
        }
        
        def builder = new CategoryHistoryBuilder(category.id, springSecurityService)
        builder.add(CategoryHistoryType.DELETE)

        categoryHistoryService.saveCategoryHistories(builder.categoryHistories)

        // Send the category to Rabbit to be deleted from the tills
        sendMessageToRabbit(false, category)
        categoryService.deleteCategory(category)

        flash.message = "Category ${category.description} deleted successfully"
        render("OK")
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetCategoryHistory(int categoryId) {
        def categoryHistoryMap = [:]
        def category = categoryService.getCategory(categoryId)

        if (categoryId > 0) {
            def effectiveDate = DateTime.now(DateTimeZone.UTC)

            if (session != null && session.effectiveDate != null && session.effectiveDate[1] != null) {
                effectiveDate = session.effectiveDate[1]
            }

            def categoryHistoryList = categoryHistoryService.getCategoryHistory(categoryId, effectiveDate)

            categoryHistoryList = categoryHistoryList?.sort {
                it?.effectiveDate
            }

            categoryHistoryList = categoryHistoryList?.reverse()

            String nullString = "null"
            categoryHistoryList?.each { item ->
                if (item?.fromValue == null || item?.fromValue == nullString) {
                    item?.fromValue = "unset"
                }

                if (item?.toValue == null || item?.toValue == nullString) {
                    item?.toValue = "unset"
                }
            }

            categoryHistoryMap = categoryHistoryList?.groupBy {
                it?.effectiveDate?.toDate()?.format('dd/MM/yyyy')
            }
        }

        render(template: "categoryHistory", model: [category: category, categoryHistoryMap: categoryHistoryMap])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def show(int id) {
        if (springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }
        
        def category = categoryService.getCategory(id)
        def pricingClassifications = pricingClassificationService.getPricingClassificationsForRetailer()

        if (!category) {
            flash.error = "Category not found"
            redirect(action: "index")
            return
        }

        def categoryList = []
        def tempCategory = category

        while (tempCategory) {
            categoryList.add(tempCategory.id)

            if (!isValidParentCategory(tempCategory.id, tempCategory.parentCategory)) {
                return
            }

            tempCategory = tempCategory.parentCategory
        }

        def loyaltyEnabled = springSecurityService.principal.retailer.config?.loyaltyRetailerConfig?.isLoyaltyEnabled ?true : false

        render(view: "maintenance", model: [category: category, addCategory: false, categoryList: categoryList, topLevelCategories: categoryService.getTopLevelCategories(), loyaltyEnabled: loyaltyEnabled, pricingClassifications: pricingClassifications])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxCategoryProductSearch() {
        session.CATEGORY_PRODUCT_SEARCH_TERM = params.searchTerm
        def categoryId = params.categoryId as int

        def products = productService.searchProductsHql(params.searchTerm, params.searchBy, 
                params.max ? Integer.parseInt(params.max) : 50, params.offset ? Integer.parseInt(params.offset) : 0, "id", "asc")
        
        render(template: "categoryProductSearchResults", 
            model: [products : products.products,
                    category: categoryService.getCategory(categoryId),
                    searchTerm : params.searchTerm,
                    searchBy : params.searchBy,
                    max : params.max ?: 50,
                    offset : params.offset,
                    totalResults : products.totalCount])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def categoryProductMapping() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }

        def categoryId = params.id as int

        [category: categoryService.getCategory(categoryId)]
    }

    def ajaxCategoryProductMapping() {
        def category
        def products
        def totalResults
        def categoryId = params.id as int
        
        category = categoryService.getCategory(categoryId)

        if (session.CATEGORY_PRODUCT_LIST) {
            products = Product.findAllByRetailerIdAndIdInList(springSecurityService.principal.retailerId, session.CATEGORY_PRODUCT_LIST,
                    [max: params.max ? Integer.parseInt(params.max) : 50, sort: "itemCode", order: "asc", offset: params.offset ? Integer.parseInt(params.offset) : 0])
            totalResults = Product.findAllByRetailerIdAndIdInList(springSecurityService.principal.retailerId, session.CATEGORY_PRODUCT_LIST).size()
        } else {
            products = Product.findAllByRetailerIdAndCategory(springSecurityService.principal.retailerId, category,
                    [max: params.max ? Integer.parseInt(params.max) : 50, sort: "itemCode", order: "asc", offset: params.offset ? Integer.parseInt(params.offset) : 0])
            totalResults = Product.findAllByRetailerIdAndCategory(springSecurityService.principal.retailerId, category).size()
        }
        
        render(template: "categoryProductMappingResults",
            model: [category : category,
                    products : products,
                    max : params.max ?: 50,
                    offset : params.offset,
                    totalResults : totalResults])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def addCategoryProduct() {
        if (springSecurityService.principal.storeId) {
            flash.error = "You do not have access to this page."
            redirect(uri: "/")
            return
        }
        
        def categoryId = params.id as int
        def category = categoryService.getCategory(categoryId)
        
        if (session.CATEGORY_PRODUCT_LIST == null) {
            session.CATEGORY_PRODUCT_LIST = Product.findAllByRetailerIdAndCategory(springSecurityService.principal.retailerId, category).findAll{ it.category?.id == categoryId }*.id
        }

        [category: category]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxToggleProductId() {
        def productId = params.productId as Integer
        
        if (session.CATEGORY_PRODUCT_LIST?.contains(productId)) {
            def index = session.CATEGORY_PRODUCT_LIST.indexOf(productId)
            session.CATEGORY_PRODUCT_LIST.remove(index)
        } else {
            if (!session.CATEGORY_PRODUCT_LIST) {
                session.CATEGORY_PRODUCT_LIST = []
            }

            session.CATEGORY_PRODUCT_LIST.add(productId)
        }
    
        render(status: 200, text: "")
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveCategoryProducts() {
        def categoryId = Integer.parseInt(params.id)
        def updatedCategoryProducts = session.CATEGORY_PRODUCT_LIST

        /* Get the category and the current products associated with it */
        def category = categoryService.getCategory(categoryId)
        def currentCategoryProducts = Product.findAllByRetailerIdAndCategory(springSecurityService.principal.retailerId, category).findAll{ it.category?.id == categoryId }*.id

        /* Determine the new product ids associated with the category */
        def addedProductIds = updatedCategoryProducts - currentCategoryProducts
        def products = Product.findAllByRetailerIdAndIdInList(springSecurityService.principal.retailerId, addedProductIds)

        try {
            /* Update the selected products to this category */
            for (Product product : products) {
                product.setCategory(category)
                productService.saveProduct(product)
            }
        } catch (Exception e) {
            flash.error = "An error occured when attempting to save the updated products"
            redirect(action: "index")
            return
        }

        /* Get the updated products and sync them to the ranged POS */
        products = Product.findAllByRetailerIdAndIdInList(springSecurityService.principal.retailerId, addedProductIds)

        try {
            for (Product product : products) {
                def rangeProducts = RangeProduct.findAllByProductId(product.id)
    
                rangeProducts?.each { rangeProduct ->
                    productService.sendProductUpdate([product], storeService.getStoresByRange(springSecurityService.principal.retailerId, rangeProduct.range))
                }
            }
        } catch (Exception e) {
            flash.error = "An error occured when attempting to sync the updated products"
            redirect(action: "index")
            return
        }

        flash.message = "Products added to category successfully"
        redirect(action: "index")
    }

    private boolean isValidParentCategory(int childId, Category parentCategory) {
        if (parentCategory != null && childId == parentCategory.id) {
            flash.error = "Error loading category"
            redirect(action: "index")
            return false
        }

        return true
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    private void sendMessageToRabbit(boolean insert, Category category) {
        List<uk.co.wonderlane.wlpos.entities.Category> categoryList = new ArrayList<>()
        List<uk.co.wonderlane.wlpos.entities.PricingClassification> pricingClassificationList = new ArrayList<>()
        uk.co.wonderlane.wlpos.entities.Category syncMessageCategory = new uk.co.wonderlane.wlpos.entities.Category()
        syncMessageCategory.retailerId = category.retailerId
        syncMessageCategory.id = category.id
        syncMessageCategory.description = category.description
        syncMessageCategory.shortDescription = category.shortDescription
        syncMessageCategory.retailerCategoryCode = category.retailerCategoryCode
        syncMessageCategory.varianceQuantity = category.varianceQuantity
        syncMessageCategory.varianceValue = category.varianceValue

        if (category.parentCategory != null) {
            syncMessageCategory.parentId = category.parentCategory.id
        }

        if (category.restrictions != null) {
            syncMessageCategory.restrictions = category.restrictions.getRestrictions()
        }
        categoryList.add(syncMessageCategory)

        // Otherwise, delete the category and update Rabbit
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.CATEGORY, springSecurityService.principal.retailerId, 0, 0, 0)
        if (insert) {
            syncMessage.setInsert(true)
        } else {
            syncMessage.setDelete(true)
        }
        syncMessage.setCategories(categoryList)
        rabbitService.sendMessage(syncMessage)

        if (category.restrictions.getPricingClassification() != null) {
            def pricing = category.restrictions.getPricingClassification()
            pricingClassificationList.add(pricing.getPricingClassification())
            
            SyncMessage pricingSyncMessage = new SyncMessage(SyncMessageType.PRICING_CLASSIFICATION, springSecurityService.principal.retailerId, 0, 0, 0)
            
            if (insert) {
                pricingSyncMessage.setInsert(true)
            } else {
                pricingSyncMessage.setDelete(true)
            }
            
            pricingSyncMessage.setPricingClassifications(pricingClassificationList)
            rabbitService.sendMessage(pricingSyncMessage)
        }
    }

    private static Optional<Integer> tryParseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str))
        } catch (Exception ignored) {
            return Optional.empty()
        }
    }

    private boolean parentIsSubCategory(int categoryId, int selectedParentId) {
        Category parent = categoryService.getCategory(selectedParentId)
        while (parent != null) {
            if (parent.id == categoryId) {
                return true
            }
            parent = parent.parentCategory
        }
        return false
    }

    private void doComparison(CategoryHistoryBuilder builder, Category category, CategoryCommand editedCategory) {
        builder.compare("description", category.description, editedCategory.description)
        builder.compare("shortDescription", category.shortDescription, editedCategory.shortDescription)
        builder.compare("retailerCategoryCode", category.retailerCategoryCode, editedCategory.retailerCategoryCode)
        builder.compare("varianceQuantity", category.varianceQuantity, editedCategory.varianceQuantity)
        builder.compare("varianceValue", category.varianceValue, editedCategory.varianceValue)
        builder.compare("restrictions.minOpenPrice", category.restrictions.minOpenPrice, editedCategory.restrictions.minOpenPrice)
        builder.compare("restrictions.maxOpenPrice", category.restrictions.maxOpenPrice, editedCategory.restrictions.maxOpenPrice)
        builder.compare("restrictions.buyerIdRequired", category.restrictions.buyerIdRequired, editedCategory.restrictions.buyerIdRequired)
        builder.compare("restrictions.buyerIdForced", category.restrictions.buyerIdForced, editedCategory.restrictions.buyerIdForced)
        builder.compare("restrictions.buyerAgeRestriction", category.restrictions.buyerAgeRestriction, editedCategory.restrictions.buyerAgeRestriction)
        builder.compare("restrictions.buyerChallengeAge", category.restrictions.buyerChallengeAge, editedCategory.restrictions.buyerChallengeAge)
        builder.compare("restrictions.sellerAgeRestriction", category.restrictions.sellerAgeRestriction, editedCategory.restrictions.sellerAgeRestriction)
        builder.compare("restrictions.refundAllowed", category.restrictions.refundAllowed, editedCategory.restrictions.refundAllowed)
        builder.compare("restrictions.markdownAllowed", category.restrictions.markdownAllowed, editedCategory.restrictions.markdownAllowed)
        builder.compare("restrictions.discountAllowed", category.restrictions.discountAllowed, editedCategory.restrictions.discountAllowed)
        builder.compare("restrictions.creditPaymentAllowed", category.restrictions.creditPaymentAllowed, editedCategory.restrictions.creditPaymentAllowed)
        builder.compare("restrictions.quantityChangeAllowed", category.restrictions.quantityChangeAllowed, editedCategory.restrictions.quantityChangeAllowed)
        builder.compare("restrictions.quantityChangeForced", category.restrictions.quantityChangeForced, editedCategory.restrictions.quantityChangeForced)
        builder.compare("restrictions.receiptPrintForced", category.restrictions.receiptPrintForced, editedCategory.restrictions.receiptPrintForced)
        builder.compare("restrictions.allowsLoyaltyPointsCollection", category.restrictions.allowsLoyaltyPointsCollection, editedCategory.restrictions.allowsLoyaltyPointsCollection)
        builder.compare("restrictions.alwaysOpenCashDrawer", category.restrictions.alwaysOpenCashDrawer, editedCategory.restrictions.alwaysOpenCashDrawer)
        builder.compare("restrictions.excludedFromPromotion", category.restrictions.excludedFromPromotion, editedCategory.restrictions.excludedFromPromotion)
        builder.compare("restrictions.saleAllowed", !category.restrictions.saleAllowed, editedCategory.restrictions.saleAllowed)
        builder.compare("restrictions.priceEntryRequired", category.restrictions.priceEntryRequired, editedCategory.restrictions.priceEntryRequired)
        builder.compare("restrictions.allowPriceChange", category.restrictions.allowPriceChange, editedCategory.restrictions.allowPriceChange)
        builder.compare("restrictions.maximumMarkdownPercentage", category.restrictions.maximumMarkdownPercentage, editedCategory.restrictions.maximumMarkdownPercentage)
        builder.compare("restrictions.quantityChangeRestriction", category.restrictions.quantityChangeRestriction, editedCategory.restrictions.quantityChangeRestriction)
        builder.compare("restrictions.promptForMarkdown", category.restrictions.promptForMarkdown, editedCategory.restrictions.promptForMarkdown)
        builder.compare("restrictions.stockClassification", category.restrictions.stockClassification, editedCategory.restrictions.stockClassification)
        builder.compare("restrictions.promptedDaysFrom", category.restrictions.promptedDaysFrom, editedCategory.restrictions.promptedDaysFrom)
        builder.compare("restrictions.pricingClassification", category.restrictions.pricingClassification, editedCategory.restrictions.pricingClassification)
    }
}

class CategoryCommand {
    int id
    String description
    String shortDescription
    String retailerCategoryCode
    Restrictions restrictions
    Integer varianceQuantity
    BigDecimal varianceValue
}