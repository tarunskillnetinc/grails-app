package uk.co.wonderlane.wlpos

class UnitOfMeasure {

    int id
    int retailerId
    String name
    String symbol

    static mapping = {
        table "unitofmeasure"
        version false

        id column: "id"
        retailerId column: "retailerId", sqlType: "tinyint"
        name column: "name"
        symbol column: "symbol"
    }

    static constraints = {
        retailerId nullable: false
        name nullable: false, size: 1..20, blank: false, validator: { val, obj ->
            return UnitOfMeasure.countByRetailerIdAndNameAndIdNotEqual(obj.retailerId, obj.name, obj.id) > 0 ? ["error.product.duplicateItemCode"] : true
        }
        symbol nullable: false, size: 1..10, blank: false
    }
}
