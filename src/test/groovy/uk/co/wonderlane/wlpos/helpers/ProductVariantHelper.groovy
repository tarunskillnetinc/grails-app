package uk.co.wonderlane.wlpos.helpers

import uk.co.wonderlane.wlpos.Barcode
import uk.co.wonderlane.wlpos.ProductPrice
import uk.co.wonderlane.wlpos.ProductVariant

class ProductVariantHelper extends ProductVariant {

    List<ProductPrice> productPrice
    List<Barcode> barcodes

    List<ProductPrice> getPrices() {
        return productPrice
    }

    List<ProductPrice> getAllPrices() {
        return productPrice
    }

    List<Barcode> getBarcodes() {
        return barcodes
    }

}
