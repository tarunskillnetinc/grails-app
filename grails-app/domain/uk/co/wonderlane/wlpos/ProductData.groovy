package uk.co.wonderlane.wlpos

class ProductData implements Serializable {

    static belongsTo = [ product: Product ]

    int id
    int storeId
    BigDecimal retailPrice
    BigDecimal costPrice
    Date effectiveDate
    Date createdDatetime
    Integer createdUserId
    Date updateDatetime
    Integer updatedUserId

    static mapping = {
        table "productdata"
        version false

        product column: "id", insertable: false, updateable: false

        id composite:['id', 'storeId', 'effectiveDate']
        storeId column: "storeId"
        retailPrice column: "price"
        costPrice column: "costPrice"
        effectiveDate column: "effectiveDate"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updateDatetime column: "updateDatetime"
        updatedUserId column: "updatedUserId"
    }

    static constraints = {
        retailPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, blank: false, nullable: false, scale: 2
        costPrice min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, blank: false, nullable: false, scale: 2
        effectiveDate nullable: false
        createdUserId nullable: true
        updatedUserId nullable: true
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