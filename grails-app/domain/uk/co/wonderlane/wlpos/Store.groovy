package uk.co.wonderlane.wlpos

import com.google.gson.reflect.TypeToken
import uk.co.wonderlane.wlpos.entities.StoreAdditionalDetail
import uk.co.wonderlane.wlpos.entities.StoreConfig

import java.lang.reflect.Type

class Store {

    def springSecurityService
    def gsonProvider

    int id
    int retailerId
    Integer parentStoreId
    PriceBand priceBand
    Range range

    String config

    Date createdDatetime
    Integer createdUserId
    Date updatedDatetime
    Integer updatedUserId
    String retailerStoreId
    boolean deleted
    String additionalDetails

    // This constructor is required or dependency injection (springSecurityService) breaks.
    public Store() {}

    static mapping = {
        autowire true
        table "store"
        version false

        id column: "id", sqlType: "smallint"
        retailerId column: "retailerId", sqlType: "tinyint"
        parentStoreId column: "parentStoreId", sqlType: "smallint"
        priceBand column: "priceBandId"
        range column: "rangeId"
        config column: "`config`", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updatedDatetime column: "updatedDatetime"
        updatedUserId column: "updatedUserId"
        retailerStoreId column: "retailerStoreId"
        deleted column: "deleted"
        additionalDetails column: "additionaldetails", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
    }

    static constraints = {
        id nullable: true
        retailerId nullable: false
        parentStoreId nullable: true
        priceBand nullable: false
        range nullable: false
        config nullable: false

        createdDatetime nullable: true
        createdUserId nullable: true
        updatedDatetime nullable: true
        updatedUserId nullable: true
        retailerStoreId nullable: true
        deleted nullable: false
        additionalDetails nullable: true
    }

    def colorCodeValidator(String colorCode) {
        if (colorCode == null || colorCode.trim().isEmpty()) {
            return true
        }

        if (colorCode.length() != 6) {
            return ['storeSettings.colourCode.length.notmet', colorCode]
        }

        if (colorCode.startsWith('#')) {
            return ['storeSettings.colourCode.format.startsWith.notmet', colorCode]
        }

        if (!isValidHexCode(colorCode)) {
            return ['storeSettings.colourCode.format.notmet', colorCode]
        }
    }

    def beforeInsert() {
        createdDatetime = new Date()
        createdUserId = springSecurityService.principal.id
    }

    def beforeUpdate() {
        updatedDatetime = new Date()
        updatedUserId = springSecurityService.principal.id
    }

    StoreConfig getConfig() {
        return gsonProvider.gson.fromJson(config, StoreConfig.class)
    }

    void setConfig(StoreConfig storeConfig) {
        config = gsonProvider.gson.toJson(storeConfig)
    }

    String getConfigString() {
        return config
    }

    List<StoreAdditionalDetail> getAdditionalDetailsList() {
        Type listType = new TypeToken<List<StoreAdditionalDetail>>(){}.getType();
        return gsonProvider.gson.fromJson(additionalDetails, listType);
    }

    String getAdditionalDetailsString() {
        return additionalDetails
    }

    public uk.co.wonderlane.wlpos.entities.Store getStore() {
        uk.co.wonderlane.wlpos.entities.Store store = new uk.co.wonderlane.wlpos.entities.Store()

        store.setId(id)
        store.setRetailerId(retailerId)
        store.setParentStoreId(parentStoreId)
        store.setRetailerStoreId(retailerStoreId)

        store.setConfig(getConfig())

        return store
    }

    private boolean isValidHexCode(String s) {
        return s.chars()
                .allMatch({ c -> "0123456789ABCDEFabcdef".indexOf(c) >= 0 });
    }
}