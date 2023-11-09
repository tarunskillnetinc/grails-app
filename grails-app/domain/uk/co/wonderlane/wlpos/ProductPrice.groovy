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
        price nullable: false ,  scale: 2, validator: {
            if (BigDecimal.ZERO == it) return ['productPrice.price.zero']
            if (it >= 99999.99) return ['productPrice.price.max']
        }
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