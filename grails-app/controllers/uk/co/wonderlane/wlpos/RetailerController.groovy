package uk.co.wonderlane.wlpos

import grails.databinding.BindUsing
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.apache.el.lang.FunctionMapperImpl
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.entities.FunctionToggle
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

            // Set those objects to the retailer config object
            terminologyConfig.locationsTableConfig = locationsTableConfig
            retailerConfig.retailerTerminologyConfig = terminologyConfig
            retailerConfig.retailerFunctionConfig = functionConfig

            retailerConfigService.saveRetailerConfig(retailerConfig)

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
