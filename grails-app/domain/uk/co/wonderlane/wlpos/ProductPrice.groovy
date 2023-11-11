package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class ProductPrice {

    static belongsTo = [ priceBand: PriceBand ]

    int id
    long sku
    DateTime effectiveDate
    BigDecimal price

    static mapping = {
        table "productprice"
        version false

        sku column: "sku"
        priceBand column: "priceBandId"
        effectiveDate column: "effectiveDate"
        price column: "price"
    }

    static constraints = {
        sku nullable: false
        effectiveDate nullable: false
        price min: 0.00 as BigDecimal, max: 99999.99 as BigDecimal, nullable: false, scale: 2
    }

    public uk.co.wonderlane.wlpos.entities.ProductPrice getProductPrice() {
        uk.co.wonderlane.wlpos.entities.ProductPrice productPrice = new uk.co.wonderlane.wlpos.entities.ProductPrice()
        productPrice.setId(id)
        productPrice.setSku(sku)
        productPrice.setPriceBandId(priceBand.id)
        productPrice.setEffectiveDate(effectiveDate)
        productPrice.setPrice(price)

        return productPrice
    }
}