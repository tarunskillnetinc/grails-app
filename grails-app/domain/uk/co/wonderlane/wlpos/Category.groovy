package uk.co.wonderlane.wlpos

class Category {

    int id
    int retailerId
    Integer parentId
    String description
    String shortDescription
    String retailerCategoryCode
    Restrictions restrictions

    static mapping = {
        table "category"
        version false

        retailerId column: "retailerId"
        parentId column: "parentId"
        description column: "`description`"
        shortDescription column: "shortDescription"
        retailerCategoryCode column: "retailerCategoryCode"
        restrictions column: "restrictionsId"
    }

    static constraints = {
        parentId nullable: true
    }

    public uk.co.wonderlane.wlpos.entities.Category getCategory() {
        uk.co.wonderlane.wlpos.entities.Category category = new uk.co.wonderlane.wlpos.entities.Category()

        category.setId(id)
        category.setRetailerId(retailerId)
        category.setParentId(parentId)
        category.setDescription(description)
        category.setShortDescription(shortDescription)
        category.setRetailerCategoryCode(retailerCategoryCode)
        category.setRestrictions(restrictions.getRestrictions())

        return category
    }
}