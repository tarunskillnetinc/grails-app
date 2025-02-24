package uk.co.wonderlane.wlpos


import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.Retailer
import uk.co.wonderlane.wlpos.entities.*
import uk.co.wonderlane.wlpos.entities.loyalty.LoyaltyRetailerConfig
import uk.co.wonderlane.wlpos.enums.LocationsType
import uk.co.wonderlane.wlpos.enums.Visibility

class RetailerController {

    def springSecurityService
    def brandAssetsService
    def retailerConfigService
    def retailerProvider

    final int MAX_LOGO_SIZE = 1048576

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
        def items = retailer.config.retailerFunctionConfig.functionMenuItems["varianceReport"]
        [retailer: retailer]
    }

    @Secured(['ROLE_ENGINEER'])
    def save(RetailerCommand retailerCommand) {
        def errorMessages = []

        if (retailerCommand.brandLogo?.filename != "" && retailerCommand.brandLogo?.filename != null) {
            if (retailerCommand.brandLogo.size <= MAX_LOGO_SIZE) {
                brandAssetsService.saveBrandLogo(retailerCommand.brandLogo.bytes)
            } else {
                errorMessages << message(code: 'retailer.logo.maxsize')
            }
        }

        Integer multiplier = retailerCommand.alcoholMinimumPriceMultiplier
        if (multiplier != null && (multiplier > 1000 || multiplier < 1)) {
            errorMessages << message(code: 'retailer.alcoholMinimumPriceMultiplier.size')
        }

        for(toggle in retailerCommand.menuItemDetails?.functionToggles?.values()){
            var t = new FunctionToggle()
            t.name = toggle.name
            t.enabled = toggle.enabled == true
            t.displayName = toggle.displayName
            retailerCommand.retailerFunctionConfig.functionMenuItems[toggle.parent].functionToggles[toggle.name] = t
        }

        RetailerConfig retailerConfig = new RetailerConfig()
        RetailerTerminologyConfig terminologyConfig = new RetailerTerminologyConfig()
        RetailerTerminologyLocationsTableConfig locationsTableConfig = new RetailerTerminologyLocationsTableConfig()
        RetailerFunctionConfig functionConfig = new RetailerFunctionConfig()
        LoyaltyRetailerConfig loyaltyRetailerConfig = new LoyaltyRetailerConfig()

        if (retailerCommand?.retailerTerminologyConfig == null) {
            retailerCommand.retailerTerminologyConfig = new RetailerTerminologyCommand()
        }

        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig == null) {
            retailerCommand?.retailerTerminologyConfig?.locationsTableConfig = new RetailerTerminologyLocationsTableConfigCommand()
        }

        if (retailerCommand?.retailerTerminologyConfig?.productTerm == "" || retailerCommand?.retailerTerminologyConfig?.productTerm == null) {
            errorMessages << "Product Term is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.packTerm == "" || retailerCommand?.retailerTerminologyConfig?.packTerm == null) {
            errorMessages << "Pack is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.quantityInStockTerm == "" || retailerCommand?.retailerTerminologyConfig?.quantityInStockTerm == null) {
            errorMessages << "Quantity In Stock is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.quantityOnOrderTerm == "" || retailerCommand?.retailerTerminologyConfig?.quantityOnOrderTerm == null) {
            errorMessages << "Quantity On Order is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.userTerm == "" || retailerCommand?.retailerTerminologyConfig?.userTerm == null) {
            errorMessages << "User is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.storeTerm == "" || retailerCommand?.retailerTerminologyConfig?.storeTerm == null) {
            errorMessages << "Store is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.itemCodeTerm == "" || retailerCommand?.retailerTerminologyConfig?.itemCodeTerm == null) {
            errorMessages << "ItemCode is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.storeHoldingsTerm == "" || retailerCommand?.retailerTerminologyConfig?.storeHoldingsTerm == null) {
            errorMessages << "Store Holdings is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.inStockTerm == "" || retailerCommand?.retailerTerminologyConfig?.inStockTerm == null) {
            errorMessages << "In Stock is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.deliveredTerm == "" || retailerCommand?.retailerTerminologyConfig?.deliveredTerm == null) {
            errorMessages << "Delivered is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.accentBarStoreTerm == "" || retailerCommand?.retailerTerminologyConfig?.accentBarStoreTerm == null) {
            errorMessages << "Store (Accent Bar) is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.stockLocationsTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.stockLocationsTerm == null) {
            errorMessages << "Stock Locations is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.descriptionTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.descriptionTerm == null) {
            errorMessages << "Description is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.bayTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.bayTerm == null) {
            errorMessages << "Bay is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfTerm == null) {
            errorMessages << "Shelf is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.positionTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.positionTerm == null) {
            errorMessages << "Position is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.aisleTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.aisleTerm == null) {
            errorMessages << "Aisle is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfCapacityTerm == "" || retailerCommand?.retailerTerminologyConfig?.locationsTableConfig?.shelfCapacityTerm == null) {
            errorMessages << "Shelf Capacity is empty. Should not be null."
        }
        if (retailerCommand?.retailerTerminologyConfig?.stockRoomTerm == "" || retailerCommand?.retailerTerminologyConfig?.stockRoomTerm == null) {
            errorMessages << "Stock Room is empty. Should not be null."
        } else {
            if (retailerCommand?.retailerTerminologyConfig?.stockRoomTerm.length() > 20) {
                errorMessages << "Stock Room cannot be more than 20 characters in length."
            }
        }
        if (retailerCommand?.retailerTerminologyConfig?.stockRoomAbbreviatedTerm == "" || retailerCommand?.retailerTerminologyConfig?.stockRoomAbbreviatedTerm == null) {
            errorMessages << "Stock Room (Abbreviated) is empty. Should not be null."
        } else {
            if (retailerCommand?.retailerTerminologyConfig?.stockRoomAbbreviatedTerm.length() > 3) {
                errorMessages << "Stock Room (Abbreviated) cannot be more than 3 characters in length."
            }
        }

        if (retailerCommand?.retailerTerminologyConfig?.unexpectedCageInDeliveryTerm == "" || retailerCommand?.retailerTerminologyConfig?.unexpectedCageInDeliveryTerm == null) {
            errorMessages << "Unexpected Cage In Delivery Term is empty. Should not be null."
        }

        if (retailerCommand.retailerFunctionConfig.shelfEdgeVisibility == null) {
            retailerCommand.retailerFunctionConfig.shelfEdgeVisibility = Visibility.ENABLED
        }
        if (retailerCommand.retailerFunctionConfig.vatRatesVisibility == null) {
            retailerCommand.retailerFunctionConfig.vatRatesVisibility = Visibility.ENABLED
        }
        if (retailerCommand.retailerFunctionConfig.styleVisibility == null) {
            retailerCommand.retailerFunctionConfig.styleVisibility = Visibility.ENABLED
        }
        if (retailerCommand.retailerFunctionConfig.categoryVisibility == null) {
            retailerCommand.retailerFunctionConfig.categoryVisibility = Visibility.ENABLED
        }


        if (errorMessages != null && !errorMessages.isEmpty()) {
            flash.error = errorMessages
            redirect(action: "index")
        } else {
            retailerCommand.retailerFunctionConfig.functionMenuItems.each { key, value ->
                if (value.name == "") {
                    value.name = camelToReadable(key)
                }
                if (!value.menuItemVisibility) {
                    value.menuItemVisibility = Visibility.ENABLED
                }
            }


            bindData(locationsTableConfig, retailerCommand.retailerTerminologyConfig.locationsTableConfig)
            bindData(terminologyConfig, retailerCommand.retailerTerminologyConfig)
            bindData(functionConfig, retailerCommand.retailerFunctionConfig)
            bindData(retailerConfig, retailerCommand)
            bindData(loyaltyRetailerConfig, retailerCommand.loyaltyConfig)

            // Set those objects to the retailer config object
            terminologyConfig.locationsTableConfig = locationsTableConfig
            retailerConfig.retailerTerminologyConfig = terminologyConfig
            retailerConfig.retailerFunctionConfig = functionConfig
            retailerConfig.loyaltyRetailerConfig = loyaltyRetailerConfig

            retailerConfigService.saveRetailerConfig(retailerConfig)
            springSecurityService.principal.retailer = retailerProvider.getRetailer(springSecurityService.principal.retailerId)
            flash.message = ["Retailer saved successfully."]

            redirect(action: "index")
        }
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
    boolean twoDimensionalBarcodesEnabled
    boolean qrCodeScanningEnabled
    boolean showSinglesWhenScanningWeighted
    boolean sendStockUpdates
    String rabbitMqUrl
    boolean rabbitMqSslEnabled
    int rabbitMqPort
    String rabbitMqVirtualHost
    String rabbitMqUsername
    String rabbitMqPassword
    String rabbitMqTransactionsExchange
    String rabbitMqDataSyncExchange
    String rabbitMqReceiptsExchange
    String imageWebServiceUrl
    Integer cfdMaxImageUpload
    Integer cfdMaxProfiles
    Integer cfdProfileImageCount
    Integer alcoholMinimumPriceMultiplier

    MultipartFile brandLogo

    RetailerFunctionCommand retailerFunctionConfig

    RetailerTerminologyCommand retailerTerminologyConfig

    LoyaltyConfigCommand loyaltyConfig

    MenuItemDetailsCommand menuItemDetails


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
    String accentBarStoreTerm
    RetailerTerminologyLocationsTableConfigCommand locationsTableConfig
    String stockRoomTerm
    String stockRoomAbbreviatedTerm
    String unexpectedCageInDeliveryTerm
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

    Map<String,FunctionToggle> functionToggles = new HashMap<String,FunctionToggle>()
}

class MenuItemDetailsCommand{
    Map<String,FunctionToggleCommand> functionToggles
}

class FunctionToggleCommand {
    String parent
    String name
    String displayName
    Boolean enabled


}

class LoyaltyConfigCommand {
    boolean isLoyaltyEnabled
    String loyaltyUrl
    String loyaltyIIN
    Double loyaltyPointValue
}
