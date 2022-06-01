package uk.co.wonderlane.wlpos.supplier

import org.joda.time.DateTime

class SupplierPriceUpdate {

    int id
    int packId
    Integer storeId
    int priceBandId
    DateTime updateDatetime

    static mapping = {
        table "supplierpriceupdate"
        version false

        id column: "id"
        packId column: "packId"
        storeId column: "storeId", sqlType: "smallint"
        priceBandId column: "priceBandId"
        updateDatetime column: "updateDatetime"
    }

    static constraints = {
        storeId nullable: true
    }
}