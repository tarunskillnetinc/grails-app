package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.entities.RetailerConfig
import uk.co.wonderlane.wlpos.entities.RetailerFunctionConfig
import uk.co.wonderlane.wlpos.entities.RetailerTerminologyConfig
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

        if (retailerCommand.retailerFunctionConfig.shelfEdgeVisibility == null){
            retailerCommand.retailerFunctionConfig.shelfEdgeVisibility = RetailerFunctionCommand.Visibility.ENABLED
        }

        retailerCommand.retailerFunctionConfig.functionMenuItems.each {key, value ->
            if (value.name == "") {
                value.name = camelToReadable(key)
            }
            if (!value.menuItemVisibility) {
                value.menuItemVisibility = RetailerFunctionCommand.Visibility.ENABLED
            }
        }

        bindData(terminologyConfig, retailerCommand.retailerTerminologyConfig)
        bindData(functionConfig, retailerCommand.retailerFunctionConfig)
        bindData(retailerConfig, retailerCommand)

        // Set those objects to the retailer config object
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
}

class RetailerFunctionCommand {
    Visibility shelfEdgeVisibility
    Map<String, FunctionMenuItemCommand> functionMenuItems

}

class FunctionMenuItemCommand {
    String name
    Visibility menuItemVisibility
}
