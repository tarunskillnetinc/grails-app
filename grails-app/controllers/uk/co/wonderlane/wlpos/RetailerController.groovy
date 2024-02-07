package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.entities.RetailerConfig
import uk.co.wonderlane.wlpos.entities.RetailerFunctionConfig
import uk.co.wonderlane.wlpos.entities.RetailerTerminologyConfig
import uk.co.wonderlane.wlpos.entities.RetailerTerminologyLocationsTableConfig
import uk.co.wonderlane.wlpos.enums.LocationsType
import uk.co.wonderlane.wlpos.enums.Visibility

class RetailerController {

    def springSecurityService
    def brandAssetsService
    def retailerConfigService

    String camelToReadable(String camelCaseString) {
        // Use a regular expression to split the string at capital letters
        def words = camelCaseString.split(/(?=[A-Z])/)
        // Capitalize the first letter of each word and join with spaces
        def readableString = words.collect { it.capitalize() }.join(' ')
        return readableString
    }


    @Secured(['ROLE_ENGINEER'])
    def index() {
        def retailer = Retailer.get(springSecurityService.principal.retailerId)
        [retailer: retailer]
    }

    @Secured(['ROLE_ENGINEER'])
    def save(RetailerCommand retailerCommand) {
        if (retailerCommand.brandLogo?.filename != "" && retailerCommand.brandLogo?.filename != null) {
            brandAssetsService.saveBrandLogo(retailerCommand.brandLogo.bytes)
        }
        RetailerConfig retailerConfig = new RetailerConfig()
        RetailerTerminologyConfig terminologyConfig = new RetailerTerminologyConfig()
        RetailerTerminologyLocationsTableConfig locationsTableConfig = new RetailerTerminologyLocationsTableConfig()
        RetailerFunctionConfig functionConfig = new RetailerFunctionConfig()

        if (retailerCommand?.retailerTerminologyConfig == null) {
            retailerCommand.retailerTerminologyConfig = new RetailerTerminologyCommand()
        }

        if (retailerCommand?.retailerTerminologyConfig?.productTerm == "" || retailerCommand?.retailerTerminologyConfig?.productTerm == null) {
            retailerCommand.retailerTerminologyConfig.productTerm = "Product"
        }
        if (retailerCommand?.retailerTerminologyConfig?.packTerm == "" || retailerCommand?.retailerTerminologyConfig?.packTerm == null) {
            retailerCommand.retailerTerminologyConfig.packTerm = "Pack";
        }
        if (retailerCommand?.retailerTerminologyConfig?.quantityInStockTerm == "" || retailerCommand?.retailerTerminologyConfig?.quantityInStockTerm == null) {
            retailerCommand.retailerTerminologyConfig.quantityInStockTerm = "Quantity In Stock";
        }
        if (retailerCommand?.retailerTerminologyConfig?.quantityOnOrderTerm == "" || retailerCommand?.retailerTerminologyConfig?.quantityOnOrderTerm == null) {
            retailerCommand.retailerTerminologyConfig.quantityOnOrderTerm = "Quantity On Order";
        }
        if (retailerCommand?.retailerTerminologyConfig?.userTerm == "" || retailerCommand?.retailerTerminologyConfig?.userTerm == null) {
            retailerCommand.retailerTerminologyConfig.userTerm = "User";
        }
        if (retailerCommand?.retailerTerminologyConfig?.storeTerm == "" || retailerCommand?.retailerTerminologyConfig?.storeTerm == null) {
            retailerCommand.retailerTerminologyConfig.storeTerm = "Store";
        }
        if (retailerCommand?.retailerTerminologyConfig?.itemCodeTerm == "" || retailerCommand?.retailerTerminologyConfig?.itemCodeTerm == null) {
            retailerCommand.retailerTerminologyConfig.itemCodeTerm = "Item Code"
        }
        if (retailerCommand?.retailerTerminologyConfig?.storeHoldingsTerm == "" || retailerCommand?.retailerTerminologyConfig?.storeHoldingsTerm == null) {
            retailerCommand.retailerTerminologyConfig.storeHoldingsTerm = "Store Holdings"
        }
        if (retailerCommand?.retailerTerminologyConfig?.inStockTerm == "" || retailerCommand?.retailerTerminologyConfig?.inStockTerm == null) {
            retailerCommand.retailerTerminologyConfig.inStockTerm = "In Stock"
        }
        if (retailerCommand?.retailerTerminologyConfig?.deliveredTerm == "" || retailerCommand?.retailerTerminologyConfig?.deliveredTerm == null) {
            retailerCommand.retailerTerminologyConfig.deliveredTerm = "Delivered"
        }

        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig = new RetailerTerminologyLocationsTableConfigCommand()
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.stockLocationsTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.stockLocationsTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.stockLocationsTerm = "Stock Locations"
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.descriptionTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.descriptionTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.descriptionTerm = "Description"
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.bayTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.bayTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.bayTerm = "Bay"
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfTerm = "Shelf"
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.positionTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.positionTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.positionTerm = "Position"
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.aisleTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.aisleTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.aisleTerm = "Aisle"
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfCapacityTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfCapacityTerm == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfCapacityTerm = "Shelf Capacity"
        }

        if (retailerCommand.retailerFunctionConfig.shelfEdgeVisibility == null){
            retailerCommand.retailerFunctionConfig.shelfEdgeVisibility =  Visibility.ENABLED

        }
        if (retailerCommand.retailerFunctionConfig.vatRatesVisibility == null) {
            retailerCommand.retailerFunctionConfig.vatRatesVisibility = Visibility.ENABLED
        }
        if (retailerCommand.retailerFunctionConfig.styleVisibility == null) {
            retailerCommand.retailerFunctionConfig.styleVisibility =  Visibility.ENABLED
        }
        if (retailerCommand.retailerFunctionConfig.categoryVisibility == null) {
            retailerCommand.retailerFunctionConfig.categoryVisibility =  Visibility.ENABLED
        }

        retailerCommand.retailerFunctionConfig.functionMenuItems.each {key, value ->
            if (value.name == "") {
                value.name = camelToReadable(key)
            }
            if (!value.menuItemVisibility) {
                value.menuItemVisibility =  Visibility.ENABLED
            }
        }

        bindData(locationsTableConfig, retailerCommand.retailerTerminologyConfig.locationsTableConfig)
        bindData(terminologyConfig, retailerCommand.retailerTerminologyConfig)
        bindData(functionConfig, retailerCommand.retailerFunctionConfig)
        bindData(retailerConfig, retailerCommand)

        // Set those objects to the retailer config object
        terminologyConfig.locationsTableConfig = locationsTableConfig
        retailerConfig.retailerTerminologyConfig = terminologyConfig
        retailerConfig.retailerFunctionConfig = functionConfig

        retailerConfigService.saveRetailerConfig(retailerConfig)

        flash.message = ["Retailer saved successfully."]

        redirect (action: "index")
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxGetBrandLogo() {
        def brandLogo = brandAssetsService.getBrandLogo()

        if (brandLogo) {
            def brandLogoBase64 = Base64.getEncoder().encode(brandLogo)

            response.setHeader 'Content-Type', 'image/png'
            response.outputStream.withStream { it << brandLogoBase64 }
        } else {
            render ""
        }
    }

    @Secured(['ROLE_ENGINEER'])
    def ajaxResetBrandLogo() {
        brandAssetsService.resetBrandLogo()

        render "OK"
    }
}

class RetailerCommand implements Validateable {

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

    MultipartFile brandLogo

    RetailerFunctionCommand retailerFunctionConfig

    RetailerTerminologyCommand retailerTerminologyConfig

}

class RetailerTerminologyCommand {
    String productTerm
    String packTerm
    String quantityInStockTerm
    String quantityOnOrderTerm
    String userTerm
    String storeTerm
    String itemCodeTerm
    String storeHoldingsTerm
    String inStockTerm
    String deliveredTerm
    RetailerTerminologyLocationsTableConfigCommand locationsTableConfig
}

class RetailerTerminologyLocationsTableConfigCommand {
    String stockLocationsTerm
    String descriptionTerm
    String bayTerm
    String shelfTerm
    String positionTerm
    String aisleTerm
    String shelfCapacityTerm
}

class RetailerFunctionCommand {
    Visibility shelfEdgeVisibility
    Visibility vatRatesVisibility
    Visibility styleVisibility
    Visibility categoryVisibility
    Map<String, FunctionMenuItemCommand> functionMenuItems

}

class FunctionMenuItemCommand {
    String name
    Visibility menuItemVisibility
}
