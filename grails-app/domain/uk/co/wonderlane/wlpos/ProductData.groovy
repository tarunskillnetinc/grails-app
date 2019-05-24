package uk.co.wonderlane.wlpos

class ProductData {

    static belongsTo = [ product: Product ]

    int id
    int storeId
    BigDecimal retailPrice
    BigDecimal costPrice
    int balanceOnHand
    int balanceOnOrder
    Date effectiveDate

    static mapping = {
        table "productdata"
        version false

//        id type: "int", generator: "foreign", params: [ property: 'product' ]
        product column: "id", insertable: false, updateable: false

        storeId column: "storeId"
        retailPrice column: "price"
        costPrice column: "costPrice"
        balanceOnHand column: "balanceOnHand"
        balanceOnOrder column: "balanceOnOrder"
        effectiveDate column: "effectiveDate"
    }
}