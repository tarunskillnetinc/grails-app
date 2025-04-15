package uk.co.wonderlane.wlpos

import com.google.gson.reflect.TypeToken
import uk.co.wonderlane.wlpos.entities.StoreAdditionalDetail
import uk.co.wonderlane.wlpos.entities.OpeningHours
import uk.co.wonderlane.wlpos.entities.StoreConfig
import uk.co.wonderlane.wlpos.entities.StoreLicencing
import uk.co.wonderlane.wlpos.entities.StoreRestrictedHours

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
    String openingHours
    String licencing
    String storeRestrictions

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
        additionalDetails column: "additionalDetails", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        openingHours column: "openingHours", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        licencing column: "licencing", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
        storeRestrictions column: "storeRestrictions", type: "uk.co.wonderlane.wlpos.usertypes.JsonType", sqlType: "json"
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
        openingHours nullable: true
        licencing nullable: true
        storeRestrictions nullable: true
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

    OpeningHours getOpeningHours() {
        if (this.openingHours != null) {
            return gsonProvider.gson.fromJson(this.openingHours, OpeningHours.class)
        }
        return null
    }

    void setOpeningHours(OpeningHours openingHours) {
        if (openingHours != null) {
            this.openingHours = gsonProvider.gson.toJson(openingHours)
        }
    }

    String getOpeningHoursString() {
        return openingHours
    }

    StoreLicencing getLicencing() {
        if (this.licencing != null) {
            return gsonProvider.gson.fromJson(this.licencing, StoreLicencing.class)
        }
        return null
    }

    void setLicencing(StoreLicencing licencing) {
        if (licencing != null) {
            this.licencing = gsonProvider.gson.toJson(licencing)
        }
    }

    StoreRestrictedHours getStoreRestrictedHours() {
        if (storeRestrictions != null) {
            return gsonProvider.gson.fromJson(storeRestrictions, new TypeToken<StoreRestrictedHours>(){}.type)
        }
        return null
    }

    void setStoreRestrictions(StoreRestrictedHours storeRestrictedHours) {
        if (storeRestrictedHours != null) {
            this.storeRestrictions = gsonProvider.gson.toJson(storeRestrictedHours)
        }
    }

    String getLicencingString() {
        return this.licencing
    }

    List<StoreAdditionalDetail> getAdditionalDetailsList() {
        if (additionalDetails) {
            return gsonProvider.gson.fromJson(additionalDetails, new TypeToken<List<StoreAdditionalDetail>>(){}.type)
        }
        return []
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