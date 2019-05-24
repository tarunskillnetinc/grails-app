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
}