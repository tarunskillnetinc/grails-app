package uk.co.wonderlane.wlpos.reporting

class SaleCategory implements Comparable {

    int id
    int categoryId
    String categoryDescription
    int categoryLevel

    static belongsTo = [ sales: Sale ]

    static mapping = {
        datasources (["reporting", "reportingReadOnly"])
        table "salescategories"
        version false

        categoryId column: "categoryId"
        categoryDescription column: "categoryDescription"
        categoryLevel column: "categoryLevel"
        sales column: "salesId"
    }

    static constraints = {

    }

    int compareTo(obj) {
        return categoryLevel.compareTo(obj.categoryLevel)
    }
}