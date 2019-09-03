package uk.co.wonderlane.wlpos

class ProductData implements Serializable {

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

        product column: "id", insertable: false, updateable: false

        id composite:['id', 'storeId', 'effectiveDate']
        storeId column: "storeId"
        retailPrice column: "price"
        costPrice column: "costPrice"
        balanceOnHand column: "balanceOnHand"
        balanceOnOrder column: "balanceOnOrder"
        effectiveDate column: "effectiveDate"
    }

    @Override
    boolean equals(Object obj) {
        if (this == obj) {
            return true
        }

        ProductData that = (ProductData)obj

        return this.id == that.id && this.storeId == that.storeId && this.effectiveDate.compareTo(that.effectiveDate) == 0
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1

        result = prime * result + id
        result = prime * result + storeId
        result = prime * result + effectiveDate.hashCode()

        return result
    }
}