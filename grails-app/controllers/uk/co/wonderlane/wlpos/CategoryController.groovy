package uk.co.wonderlane.wlpos

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class CategoryController extends BaseController {

    def springSecurityService

    def index() { }

    def ajaxSaveColumns() {
        super.ajaxSaveColumns()
    }

    def ajaxSearchCategories() {
        session.CATEGORIES_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def categories = categoryService.searchCategoriesPaged(params.searchTerm,
                params.max ? Integer.parseInt(params.max) : 50,
                params.offset ? Integer.parseInt(params.offset) : 0,
                "description",
                "asc")

        render(template: "categorySearchResults", model: [categories    : categories,
                                                         storeId     : springSecurityService.principal.storeId,
                                                         userColumns : categoryService.getColumns(),
                                                         searchTerm  : params.searchTerm,
                                                         max         : params.max ?: 50,
                                                         offset      : params.offset,
                                                         totalResults: categoryService.countCategories(params.searchTerm)])
    }

    def ajaxGetRestrictions(int selectedCategoryId) {
        def parentCategory = categoryService.getCategory(selectedCategoryId)
        render(template:"restrictions", model: [category: parentCategory])
    }

    def getColumns() {
        return categoryService.getColumns()
    }

    def add() {
        def blankCategory = new Category()
        blankCategory.setRestrictions(new Restrictions())
        render(view: "maintenance", model: [category: blankCategory, topLevelCategories: getTopLevelCategories()])
    }

    def save() {
        def category = categoryService.getCategory(Integer.parseInt(params.get("id").toString()))
        if (category == null) {
            category = new Category()
            category.retailerId = springSecurityService.principal.retailerId
        }

        bindData(category, params)
        category.save()

        if(!category.hasErrors()) {
            flash.message = "Category saved successfully"
            redirect("controller": "category", action:"index")
        } else {
            redirect(controller: "category", action:"show", id: category.id)
        }
    }

    def show(int id) {
        def category = categoryService.getCategory(id)

        render(view: "maintenance", model: [category: category, topLevelCategories: getTopLevelCategories()])
    }

    private List<Category> getTopLevelCategories() {
        def topLevelCategories = categoryService.getTopLevelCategories()
        topLevelCategories.add(0, new Category(description: "NONE"))
        return topLevelCategories;
    }
}
