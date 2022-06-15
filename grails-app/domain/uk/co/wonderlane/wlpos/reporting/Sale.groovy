package uk.co.wonderlane.wlpos.reporting

import org.joda.time.DateTime

import java.math.RoundingMode

class Sale {

    int id
    int retailerId
    int storeId
    int tillId
    int quantity
    BigDecimal costPrice
    BigDecimal retailPrice
    BigDecimal vatAmount
    BigDecimal margin
    int productId
    String productItemCode
    String productDescription
    String productUnitSize
    int vatCodeId
    BigDecimal vatCodeRate
    String vatCodeDescription
    int userId
    String usersName
    DateTime dateCreated

    BigDecimal avgCostPrice // Calculated value, marked transient below.
    BigDecimal avgRetailPrice // Calculated value, marked transient below.
    BigDecimal avgMargin // Calculated value, marked transient below.

    SortedSet<SaleCategory> salesCategories

    static hasMany = [ salesCategories: SaleCategory ]

    static fetchMode = [ salesCategories: 'eager' ]

    static transients = [ 'avgCostPrice', 'avgRetailPrice', 'avgMargin' ]

    static mapping = {
        datasources (["reporting"])

        table "sales"
        version false

        quantity column: "quantity"
        retailerId column: "retailerId", sqlType: "tinyint"
        storeId column: "storeId", sqlType: "smallint"
        tillId column: "tillId"
        costPrice column: "costPrice"
        retailPrice column: "retailPrice"
        vatAmount column: "vatAmount"
        margin column: "margin"
        productId column: "productId"
        productItemCode column: "productItemCode"
        productDescription column: "productDescription"
        productUnitSize column: "productUnitSize"
        vatCodeId column: "vatCodeId"
        vatCodeRate column: "vatCodeRate"
        vatCodeDescription column: "vatCodeDescription"
        userId column: "userId"
        usersName column: "usersName"
        dateCreated column: "dateCreated"
        salesCategories lazy: false
    }

    static constraints = {

    }

    BigDecimal getAvgCostPrice() {
        return quantity == 0 ? costPrice.divide(BigDecimal.valueOf(1), 2, RoundingMode.HALF_UP) : costPrice.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP)
    }

    BigDecimal getAvgRetailPrice() {
        return quantity == 0 ? retailPrice.divide(BigDecimal.valueOf(1), 2, RoundingMode.HALF_UP) : retailPrice.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP)
    }

    BigDecimal getAvgMargin() {
        return quantity == 0 ? margin.divide(BigDecimal.valueOf(1), 2, RoundingMode.HALF_UP) : margin.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP)
    }
}