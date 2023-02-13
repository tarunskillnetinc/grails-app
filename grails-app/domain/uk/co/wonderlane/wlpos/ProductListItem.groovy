package uk.co.wonderlane.wlpos

import java.math.RoundingMode

class ProductListItem {

    int id
    ProductVariant productVariant
    int productQuantityInStock
    Integer quantity
    int fillQuantity = 0
    Integer parentQuantity

    static belongsTo = [ productList: ProductList, productListItemGroup: ProductListItemGroup ]

    static hasMany = [ packLines: PackLine ]

    static transients = [ 'totalValue', 'totalCost' ]

    static mapping = {
        table "productlistitem"
        version false

        productVariant column: "productVariantId", cascade: "save-update"
        productQuantityInStock column: "productQuantityInStock"
        quantity column: "quantity"
        fillQuantity column: "fillQuantity"
        parentQuantity column: "parentQuantity"

        productList column: "productListId"
        productListItemGroup column: "productListItemGroupId"
    }

    static constraints = {
        productVariant nullable: false
        productQuantityInStock nullable: true
        quantity nullable: true
        fillQuantity nullable: false
        parentQuantity nullable: true
        productListItemGroup nullable: true
    }

    def getTotalValue() {
        if (fillQuantity == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        if (productVariant == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        return packLines?.sum {
            it?.getTotalValue()
        }
    }

    def getTotalCost() {
        int totalCost = 0

        if (quantity) {
            int totalSinglesCost = 0
            int totalPackQuantity = packLines?.sum {it -> getPackQuantity(it)} ?: 0
            int singlesQuantity = quantity - totalPackQuantity

            // Get total pack cost.
            int totalPackCost = packLines?.sum {
                it?.quantity?.intValue().multiply(getCostPriceByVariant(it))
            } ?: 0

            // Get total singles cost.
            if (singlesQuantity > 0) {
                totalSinglesCost = singlesQuantity.multiply(productVariant?.costPrice ?: 0)
            }

            // If quantity is not null then estimated delivery cost = total pack cost + singles cost.
            totalCost = totalPackCost + totalSinglesCost
        } else {
            // If quantity is null then estimates delivery cost = fill quantity * product variant cost price.
            totalCost = fillQuantity.multiply(productVariant?.costPrice ?: 0)
        }

        return totalCost
    }

    private int getCostPriceByVariant(PackLine pk){
        try {
            if (pk?.pack?.price) {
                return pk.pack.price.intValue()
            }
        }catch(Exception ex){
            //This exception can be thrown when corresponding packs missing for pack line object
            return 0
        }
        return productVariant?.costPrice ?: 0
    }

    private int getPackQuantity(PackLine pk){
        try {
            if (pk?.pack?.quantity){
                int packQuantity = pk.pack.quantity;
                return packQuantity.multiply(pk.quantity.intValue())
            }
        }catch(Exception ex){
            return pk.quantity.intValue()
        }
    }
}