package uk.co.wonderlane.wlpos

class Category {

    int id
    int retailerId
    String description
    String shortDescription
    String retailerCategoryCode
    Restrictions restrictions
    Integer varianceQuantity
    BigDecimal varianceValue

    Collection<Category> childCategories

    static hasMany = [ childCategories: Category ]
    static belongsTo = [ parentCategory: Category ]

    static mapping = {
        table "category"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        description column: "`description`"
        shortDescription column: "shortDescription"
        retailerCategoryCode column: "retailerCategoryCode"
        restrictions column: "restrictionsId"
        varianceValue column: "varianceValue"
        varianceQuantity column: "varianceQuantity"

        parentCategory column: "parentId"
        childCategories sort: 'description', order: 'asc'
    }

    static constraints = {
        description nullable: false, blank: false, validator: { val, obj ->
            if (val == null) {
                return true
            }

            def existingCategory = Category.findByRetailerIdAndDescription(obj.retailerId, val)
            if (existingCategory && (obj.id == null || obj.id == 0 || obj.id != existingCategory.id)) {
                return ['category.description.unique.error']
            }

            if (!val.matches("\\A\\p{ASCII}*\\z")) {
                return ['category.description.ascii.error']
            }
        }
        shortDescription nullable: true, blank: true, validator: { val, obj ->
            if (val == null) {
                return true
            }

            if (!val.matches("\\A\\p{ASCII}*\\z")) {
                return ['category.shortDescription.ascii.error']
            }
        }
        retailerCategoryCode nullable: false, blank: false, validator: { val, obj ->
            if (val == null) {
                return true
            }

            def existingCategory = Category.findByRetailerIdAndRetailerCategoryCode(obj.retailerId, val)
            if (existingCategory && (obj.id == null || obj.id == 0 || obj.id != existingCategory.id)) {
                return ['category.retailerCategoryCode.unique.error']
            }

            if (!val.matches("\\A\\p{ASCII}*\\z")) {
                return ['category.retailerCategoryCode.ascii.error']
            }
        }
        parentCategory nullable: true
        varianceQuantity nullable: true, min: 1, max: 99
        varianceValue nullable: true, min: BigDecimal.ZERO, max: 99999.99
    }

    public uk.co.wonderlane.wlpos.entities.Category getCategory() {
        uk.co.wonderlane.wlpos.entities.Category category = new uk.co.wonderlane.wlpos.entities.Category()

        category.setId(id)
        category.setRetailerId(retailerId)
        category.setParentId(parentCategory?.id)
        category.setDescription(description)
        category.setShortDescription(shortDescription)
        category.setRetailerCategoryCode(retailerCategoryCode)
        category.setRestrictions(restrictions.getRestrictions())
        category.setVarianceQuantity(varianceQuantity)
        category.setVarianceValue(varianceValue)

        return category
    }
}