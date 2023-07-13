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

    class CategoryWithLevel {
        Category category;
        int categoryLevel;
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchCategories() {
        session.CATEGORIES_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def categories = categoryService.searchCategoriesPaged(params.searchTerm,
                params.max ? Integer.parseInt(params.max) : 50,
                params.offset ? Integer.parseInt(params.offset) : 0,
                "description",
                "asc")

        ArrayList<CategoryWithLevel> extendedCategories = new ArrayList<CategoryWithLevel>()

        categories.forEach {category ->

            var extendedCategory = new CategoryWithLevel()
            extendedCategory.category = category
            extendedCategory.categoryLevel = 0 // How many sub categories deep is this category?

            var currentCategory = category
            while (currentCategory.parentCategory != null) {
                currentCategory = categoryService.getCategory(currentCategory.parentCategory.id)
                if (currentCategory != null) {
                    extendedCategory.categoryLevel++
                } else {
                    break
                }
            }

            extendedCategories.add(extendedCategory)
        }

        render(template: "categorySearchResults", model: [categories    : extendedCategories,
                                                         storeId     : springSecurityService.principal.storeId,
                                                         userColumns : categoryService.getColumns(),
                                                         searchTerm  : params.searchTerm,
                                                         max         : params.max ?: 50,
                                                         offset      : params.offset,
                                                         totalResults: categoryService.countCategories(params.searchTerm)])
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
        render(view: "maintenance", model: [category: blankCategory, addCategory: true, topLevelCategories: getTopLevelCategories()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def save() {
        def addingCategory = false
        def category = categoryService.getCategory(Integer.parseInt(params.get("id").toString()))
        if (category == null) {
            category = new Category()
            category.retailerId = springSecurityService.principal.retailerId
            category.restrictions = new Restrictions()
            addingCategory = true
        }

        bindData(category, params)

        //Check for a Parent Category being selected.
        def parentCategory = params.get("category.id")
        if (parentCategory != null) {
            category.parentCategory = categoryService.getCategory(Integer.parseInt(parentCategory))
        }

        if (!addingCategory) {
            def restriction = Restrictions.findById(category.restrictions.id)
            categoryService.saveRestriction(restriction)
        } else {
            categoryService.saveRestriction(category.restrictions)
        }

        categoryService.saveCategory(category)

        if(!category.hasErrors()) {
            // Send the category to Rabbit to be inserted / updated in the tills
            sendMessageToRabbit(true, category)

            flash.message = "Category saved successfully"
            redirect("controller": "category", action:"index")
        } else {
            redirect(controller: "category", action:"show", id: category.id)
        }
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxDeleteCategory(int categoryId) {
        def category = categoryService.getCategory(categoryId)

        // Search for any products that use this category ID
        def products = Product.findAllByCategory(category)
        if (products.size() != 0) {
            render(template: "maintenanceForm", model: [category: category, hasProducts: true, topLevelCategories: getTopLevelCategories()])
            return
        }

        // Check whether this category has any children
        if (category.childCategories.size() > 0) {
            render(template: "maintenanceForm", model: [category: category, hasChildren: true, topLevelCategories: getTopLevelCategories()])
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

        render(view: "maintenance", model: [category: category, addCategory: false, topLevelCategories: getTopLevelCategories()])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    private List<Category> getTopLevelCategories() {
        def topLevelCategories = categoryService.getTopLevelCategories()
        topLevelCategories.add(0, new Category(description: "NONE"))
        return topLevelCategories;
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
}
