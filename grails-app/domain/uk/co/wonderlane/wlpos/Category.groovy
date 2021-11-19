package uk.co.wonderlane.wlpos

class Category {

    int id
    int retailerId
    String description
    String shortDescription
    String retailerCategoryCode
    Restrictions restrictions

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
        parentCategory column: "parentId"

        childCategories sort: 'description', order: 'asc'
    }

    static constraints = {
        retailerCategoryCode nullable: true
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

        return category
    }
}