package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.security.access.annotation.Secured
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

class CategoryController extends BaseController {

    def springSecurityService
    def rabbitService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() { }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSaveColumns() {
        super.ajaxSaveColumns()
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchCategories() {
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        int max = params.max ? Integer.parseInt(params.max) : 50
        String searchTerm = params.searchTerm

        session.CATEGORY_SEARCH_TERM = searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def searchResults = baseSearchCategories(searchTerm)
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
    def ajaxSearchMaintenanceCategories(String searchTerm, boolean triggerOnCategoryChange, int level) {
        def searchResults = baseSearchCategories(searchTerm)
        boolean isSearch = searchTerm?.length() > 0
        render(template: "/product/categorySelectInputs", model: [categories: searchResults.aValue.unique(), level: isSearch ? level : 1, productCategoryList: searchResults.bValue, selectedCategoryId: null, triggerOnCategoryChange: triggerOnCategoryChange, isSearch: isSearch])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxGetRestrictions(int selectedCategoryId) {
        def parentCategory = categoryService.getCategory(selectedCategoryId)
        render(template:"restrictions", model: [category: parentCategory])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def getColumns() {
        return categoryService.getColumns()
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def add() {
        def blankCategory = new Category()
        blankCategory.setRestrictions(new Restrictions())

        // default values for new category:
        blankCategory.restrictions.refundAllowed = true
        blankCategory.restrictions.markdownAllowed = true
        blankCategory.restrictions.discountAllowed = true
        blankCategory.restrictions.creditPaymentAllowed = true
        blankCategory.restrictions.quantityChangeAllowed = true

        render(view: "maintenance", model: [category: blankCategory, addCategory: true, topLevelCategories: categoryService.getTopLevelCategories()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def save() {
        def addingCategory = false
        def category = null
        def categoryId = tryParseInt(params.get("id").toString())

        if (categoryId.isPresent()) {
            category = categoryService.getCategory(categoryId.get())
        }

        if (category == null) {
            category = new Category()
            category.retailerId = springSecurityService.principal.retailerId
            category.restrictions = new Restrictions()
            addingCategory = true
        }

        bindData(category, params)

        //Check for a Parent Category being selected.
        def parentId = tryParseInt(params.get("category.id"))
        if (parentId.isPresent()) {
            def parentCategorySearch = categoryService.getCategory(parentId.get())
            // Make sure we're not saving the same ID otherwise we'll spin forever
            if (parentCategorySearch != null) {
                if (parentCategorySearch.id == category.id) {
                    category.errors.reject('category.parentCategory.notUnique')
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
            render(view: "maintenance", model: [category: category, addCategory: false, topLevelCategories: categoryService.getTopLevelCategories()])
            return
        }

        if (!addingCategory) {
            def restriction = Restrictions.findById(category.restrictions.id)
            categoryService.saveRestriction(restriction)

            if (restriction.hasErrors()) {
                render(view: "maintenance", model: [category: category, restrictions: restriction, addCategory: false, topLevelCategories: categoryService.getTopLevelCategories()])
                return
            }
        } else {
            categoryService.saveRestriction(category.restrictions)

            if (category.restrictions.hasErrors()) {
                render(view: "maintenance", model: [category: category, restrictions: category.restrictions, addCategory: false, topLevelCategories: categoryService.getTopLevelCategories()])
                return
            }
        }

        categoryService.saveCategory(category)

        if (!category.hasErrors()) {
            // Send the category to Rabbit to be inserted / updated in the tills
            sendMessageToRabbit(true, category)

            flash.message = "Category saved successfully"
            redirect("controller": "category", action:"index")
        } else {
            def categoryList = []
            def tempCategory = category

            while (tempCategory) {
                categoryList.add(tempCategory.id)

                tempCategory = tempCategory.parentCategory
            }

            render(view: "maintenance", model: [category: category, restrictions: category.restrictions, addCategory: false, categoryList: categoryList, topLevelCategories: categoryService.getTopLevelCategories()])
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

        // Send the category to Rabbit to be deleted from the tills
        sendMessageToRabbit(false, category)
        categoryService.deleteCategory(category)
        render("OK")
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def show(int id) {
        def category = categoryService.getCategory(id)

        if (!category) {
            flash.error = "Category not found"
            redirect(action: "index")
            return
        }

        def categoryList = []
        def tempCategory = category

        while (tempCategory) {
            categoryList.add(tempCategory.id)

            tempCategory = tempCategory.parentCategory
        }

        render(view: "maintenance", model: [category: category, addCategory: false, categoryList: categoryList, topLevelCategories: categoryService.getTopLevelCategories()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    private void sendMessageToRabbit(boolean insert, Category category) {
        List<uk.co.wonderlane.wlpos.entities.Category> categoryList = new ArrayList<>()
        uk.co.wonderlane.wlpos.entities.Category syncMessageCategory = new uk.co.wonderlane.wlpos.entities.Category()
        syncMessageCategory.retailerId = category.retailerId
        syncMessageCategory.id = category.id
        syncMessageCategory.description = category.description
        syncMessageCategory.shortDescription = category.shortDescription
        syncMessageCategory.retailerCategoryCode = category.retailerCategoryCode

        if (category.parentCategory != null) {
            syncMessageCategory.parentId = category.parentCategory.id
        }

        if (category.restrictions != null) {
            syncMessageCategory.restrictions = new uk.co.wonderlane.wlpos.entities.Restrictions()
            syncMessageCategory.restrictions.id = category.restrictions.id
        }
        categoryList.add(syncMessageCategory)

        // Otherwise, delete the category and update Rabbit
        SyncMessage syncMessage = new SyncMessage(SyncMessageType.CATEGORY, springSecurityService.principal.retailerId, 0,0, 0)
        if (insert) {
            syncMessage.setInsert(true)
        } else {
            syncMessage.setDelete(true)
        }
        syncMessage.setCategories(categoryList)
        rabbitService.sendMessage(syncMessage)
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
}
