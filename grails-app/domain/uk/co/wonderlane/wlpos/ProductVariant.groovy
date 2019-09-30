package uk.co.wonderlane.wlpos

class ProductVariant {

    static belongsTo = [product: Product]

    int id
    String itemCode
    int storeId
    String size
    String colour
    int balanceOnHand
    int balanceOnOrder
    boolean delete

    Collection<Barcode> barcodes = new ArrayList<>()

    static transients = ['delete']

    static hasMany = [barcodes: Barcode]

    static mapping = {
        table "productvariant"
        version false

        itemCode column: "itemCode"
        product column: "productId"
        storeId column: "storeId"
        size column:"size"
        colour column:"colour"
        balanceOnHand column:"balanceOnHand"
        balanceOnOrder column: "balanceOnOrder"
        barcodes cascade: "all-delete-orphan"
    }

    static constraints = {
        itemCode blank: false, nullable: false
        size blank: true, nullable: true
        colour blank: true, nullable: true
        barcodes minSize: 1
        delete bindable: true
    }

    public uk.co.wonderlane.wlpos.entities.ProductVariant getProductVariant() {
        uk.co.wonderlane.wlpos.entities.ProductVariant productVariant = new uk.co.wonderlane.wlpos.entities.ProductVariant()
        productVariant.setId(id)
        productVariant.setStoreId(storeId)
        productVariant.setItemCode(itemCode)
        productVariant.setProductId(product.id)
        productVariant.setSize(size)
        productVariant.setColour(colour)
        productVariant.setBalanceOnHand(balanceOnHand)
        productVariant.setBalanceOnOrder(balanceOnOrder)
        barcodes.each {
            productVariant.getBarcodes().add(it.barcode)
        }
        return productVariant
    }
}
