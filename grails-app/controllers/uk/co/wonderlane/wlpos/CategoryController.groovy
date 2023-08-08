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
        Category category
        int categoryLevel
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchCategories() {
        session.CATEGORY_SEARCH_TERM = params.searchTerm
        session.effectiveDate = ["Current", DateTime.now(DateTimeZone.UTC)]

        def categories = categoryService.searchCategoriesPaged(params.searchTerm,
                params.max ? Integer.parseInt(params.max) : 50,
                params.offset ? Integer.parseInt(params.offset) : 0,
                "description",
                "desc")

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

        extendedCategories.sort { it.categoryLevel }
        def relationTree = new CategoryTree()
        extendedCategories.forEach { relationTree.insert(it) }

        render(template: "categorySearchResults", model: [categories    : relationTree.toList(),
                                                         storeId     : springSecurityService.principal.storeId,
                                                         userColumns : categoryService.getColumns(),
                                                         searchTerm  : params.searchTerm,
                                                         max         : params.max ?: 50,
                                                         offset      : params.offset,
                                                         totalResults: categoryService.countCategories(params.searchTerm)])
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def ajaxSearchMaintenanceCategories(String searchTerm, boolean triggerOnCategoryChange, int level) {
        baseSearchCategories(searchTerm, triggerOnCategoryChange, level)
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
        render(view: "maintenance", model: [category: blankCategory, addCategory: true, topLevelCategories: getTopLevelCategories()])
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
                if (parentCategorySearch.id != category.id) {
                    category.parentCategory = parentCategorySearch
                } else {
                    category.errors.reject('category.parentCategory.notUnique', [category.parentCategory] as Object[], 'Categories cannot be their own parent, please select a new category or none.')
                }
            } else {
                category.parentCategory = null
            }
        }

        if (category.hasErrors()) {
            render(view: "maintenance", model: [category: category, addCategory: false, topLevelCategories: getTopLevelCategories()])
            return
        }

        if (!addingCategory) {
            def restriction = Restrictions.findById(category.restrictions.id)
            nullOptionalAmountFields(restriction)
            categoryService.saveRestriction(restriction)

            if (restriction.hasErrors()) {
                render(view: "maintenance", model: [category: category, restrictions: restriction, addCategory: false, topLevelCategories: getTopLevelCategories()])
                return
            }
        } else {
            nullOptionalAmountFields(category.restrictions)
            categoryService.saveRestriction(category.restrictions)

            if (category.restrictions.hasErrors()) {
                render(view: "maintenance", model: [category: category, restrictions: category.restrictions, addCategory: false, topLevelCategories: getTopLevelCategories()])
                return
            }
        }

        categoryService.saveCategory(category)

        if(!category.hasErrors()) {
            // Send the category to Rabbit to be inserted / updated in the tills
            sendMessageToRabbit(true, category)

            flash.message = "Category saved successfully"
            redirect("controller": "category", action:"index")
        } else {
            render(view: "maintenance", model: [category: category, restrictions: category.restrictions, addCategory: false, topLevelCategories: getTopLevelCategories()])
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

    private static Optional<Integer> tryParseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str))
        } catch (Exception ignored) {
            return Optional.empty()
        }
    }

    private static void nullOptionalAmountFields(Restrictions restrictions) {
        if (restrictions.minOpenPrice == 0) {
            restrictions.minOpenPrice = null
        }
        if (restrictions.maxOpenPrice == 0) {
            restrictions.maxOpenPrice = null
        }
    }

    private class CategoryTree {
        HashMap<Integer, CategoryWithLevel> mapping
        CategoryNode head;

        CategoryTree() {
            mapping = new HashMap<>()
        }

        private void insert(CategoryWithLevel cat) {
            if (mapping.containsKey(cat.category.parentCategoryId)) {
                def parent = mapping.get(cat.category.parentCategoryId)
                def newNode = new CategoryNode(cat, null, parent.child)
                parent.child = newNode
                mapping.put(cat.category.id, newNode)
                return
            }
            head = new CategoryNode(cat, null, head)
            mapping.put(cat.category.id, head)
        }

        private ArrayList<CategoryWithLevel> toList() {
            return head.toList();
        }

        private class CategoryNode {
            CategoryWithLevel cat;
            CategoryNode child;
            CategoryNode next;

            CategoryNode(CategoryWithLevel cat, CategoryNode child, CategoryNode next) {
                this.cat = cat
                this.child = child
                this.next = next
            }

            private ArrayList<CategoryWithLevel> toList() {
                ArrayList<CategoryWithLevel> result = new ArrayList<>()
                result.add(cat)
                if (child != null) {
                    result.addAll(child.toList())
                }
                if (next != null) {
                    result.addAll(next.toList())
                }
                return result
            }
        }
    }
}
