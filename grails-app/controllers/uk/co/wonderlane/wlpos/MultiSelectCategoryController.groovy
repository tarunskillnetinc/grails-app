package uk.co.wonderlane.wlpos

class MultiSelectCategoryController extends BaseController {

    def ajaxSearchCategories(String searchTerm, boolean triggerOnCategoryChange, int level, int selectedCategoryId) {
        def searchResults = baseSearchCategories(searchTerm)
        boolean isSearch = searchTerm?.length() > 0
        render(template: "/multiSelectCategory/categorySelectInputs", model: [categories: searchResults.aValue.unique(), level: isSearch ? level : 1, productCategoryList: searchResults.bValue, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange, isSearch: isSearch])
    }

    def ajaxGetChildCategories(int categoryId, int level, int selectedCategoryId, boolean triggerOnCategoryChange) {
        def category = categoryService.getCategory(categoryId)

        render(template: "categorySelectInputs", model: [categories: category?.childCategories, level: level, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange])
    }

    @Override
    def getColumns() {
        return null
    }
}
