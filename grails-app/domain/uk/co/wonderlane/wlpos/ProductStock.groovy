package uk.co.wonderlane.wlpos

class ProductStock implements Serializable {

    int id
    long sku
    int storeId
    int quantityInStock
    int quantityOnOrder
    int quantityDelivered

    // This constructor is required or dependency injection (springSecurityService) breaks. Don't forget "autowire true" in the mappings as well.
    public ProductStock() { }

    static mapping = {
        autowire true
        table "productstock"
        version false

        sku column: "sku"
        storeId column: "storeId", sqlType: "smallint"
        quantityInStock column: "quantityInStock"
        quantityOnOrder column: "quantityOnOrder"
        quantityDelivered column: "quantityDelivered"
    }

    static constraints = {
        storeId nullable: false
        sku nullable: false
        quantityInStock nullable: false
        quantityOnOrder nullable: false
        quantityDelivered nullable: false
    }

    @Override
    boolean equals(Object obj) {
        ProductStock that = (ProductStock)obj

        return this.id == that.id && this.storeId == that.storeId && this.sku == that.sku
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1

        result = prime * result + id
        result = prime * result + (storeId ?: 0)
        result = prime * result + sku

        return result
    }
}