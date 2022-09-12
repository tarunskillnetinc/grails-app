package uk.co.wonderlane.wlpos

import org.joda.time.DateTime

class Barcode {

    int id
    long sku
    int retailerId
    String barcode
    DateTime effectiveDate
    char recordStatus

    boolean delete
    DateTime effectiveDeleteDate

    static transients = ['delete', 'effectiveDeleteDate']

    static mapping = {
        table "barcode"
        version false

        sku column: "sku"
        retailerId column: "retailerId", sqlType: "tinyint"
        barcode column: "barcode"
        effectiveDate column: "effectiveDate"
        recordStatus column: "recordStatus"
    }

    static constraints = {
        sku nullable: false
        retailerId nullable: false
        barcode size: 1..20, blank: false, nullable: false, validator: { val, obj ->

            //Initially set barcode value is available for use
            boolean isBarcodeActive = false

            //Load all barcode values which do not belonging to sku trying to create
            def barcodes = Barcode.findAllByRetailerIdAndBarcodeAndSkuNotEqualAndEffectiveDateLessThanEquals(obj.retailerId, obj.barcode, obj.sku, obj.effectiveDate, [sort: "effectiveDate", order: "desc"])

            //Group sku list int map of barcode
            def barcodeSkuMap = barcodes?.groupBy {it.sku}

            //Then loop over map of barcode to find out if barcode is available to use
            for (Map.Entry<Long, List<Barcode>> skuListEntry : barcodeSkuMap?.entrySet()) {

                int deletedCount = 0
                int activeCount = 0

                //For barcode belonging to particular sku check occurrence of active and deleted
                skuListEntry.value?.forEach({ barcode ->
                    if (barcode.recordStatus == ('D' as char)) {
                        deletedCount++
                    } else {
                        activeCount++
                    }
                })

                //If active barcode count (Status = 'C') greater than of barcode count for deleted (Status = 'D') then we can assume that barcode is active and can not be use by other sku
                if (activeCount > deletedCount) {
                    isBarcodeActive = true
                    break
                }

            }

            return isBarcodeActive ? ["error.product.duplicateBarcode"] : true
        }
        effectiveDate nullable: false
        recordStatus nullable: false
    }
}