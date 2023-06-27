package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.LocationsType

class Retailer implements Serializable {

    int id
    String name
    LocationsType locationsType
    boolean headOfficeProductMaintenance
    boolean snappyShopperEnabled
    boolean twoStageSel
    boolean averyEnabled
    boolean scoEnabled
    String rabbitMqUrl
    boolean rabbitMqSslEnabled
    int rabbitMqPort
    String rabbitMqVirtualHost
    String rabbitMqUsername
    String rabbitMqPassword
    String rabbitMqTransactionsExchange
    String rabbitMqDataSyncExchange
    String rabbitMqReceiptsExchange

    static mapping = {
        table "retailers"
        version false

        id column: "retailerId", sqlType: "tinyint"
        name column: "`name`"
        locationsType column: "locationsType"
        headOfficeProductMaintenance column: "headOfficeProductMaintenance"
        snappyShopperEnabled column: "snappyShopperEnabled"
        twoStageSel column: "twoStageSel"
        averyEnabled column: "averyEnabled"
        scoEnabled column: "scoEnabled"
        rabbitMqUrl column: "rabbitMqUrl"
        rabbitMqSslEnabled column: "rabbitMqSslEnabled"
        rabbitMqPort column: "rabbitMqPort"
        rabbitMqVirtualHost column: "rabbitMqVirtualHost"
        rabbitMqUsername column: "rabbitMqUsername"
        rabbitMqPassword column: "rabbitMqPassword"
        rabbitMqTransactionsExchange column: "rabbitMqTransactionsExchange"
        rabbitMqDataSyncExchange column: "rabbitMqDataSyncExchange"
        rabbitMqReceiptsExchange column: "rabbitMqReceiptsExchange"
    }

    static constraints = {
        id nullable: false
        name nullable: false
        locationsType nullable: false
        headOfficeProductMaintenance nullable: false
        snappyShopperEnabled nullable: false
        twoStageSel nullable: false
        averyEnabled nullable: false
        scoEnabled nullable: false
        rabbitMqUrl nullable: false
        rabbitMqSslEnabled nullable: false
        rabbitMqPort nullable: false
        rabbitMqVirtualHost nullable: false
        rabbitMqUsername nullable: false
        rabbitMqPassword nullable: false
        rabbitMqTransactionsExchange nullable: false
        rabbitMqDataSyncExchange nullable: false
        rabbitMqReceiptsExchange nullable: false
    }
}