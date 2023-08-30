package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile
import uk.co.wonderlane.wlpos.entities.RetailerIMConfig

class RetailerController {

    def springSecurityService
    def brandAssetsService
    def retailerConfigService

    @Secured(['ROLE_ENGINEER'])
    def index() {
        def retailer = Retailer.get(springSecurityService.principal.retailerId)

        [retailer: retailer]
    }

    @Secured(['ROLE_ENGINEER'])
    def save(RetailerCommand retailerCommand) {
        if (retailerCommand.brandLogo?.filename != "") {
            brandAssetsService.saveBrandLogo(retailerCommand.brandLogo.bytes)
        }
        RetailerIMConfig imConfig = new RetailerIMConfig()

        if (retailerCommand?.product != "") {
            imConfig["product"] = retailerCommand.product
        } else {
            imConfig["product"] = "Product"
        }
        if (retailerCommand?.pack != "") {
            imConfig["pack"] = retailerCommand.pack;
        } else {
            imConfig["pack"] = "Pack";
        }
        if (retailerCommand?.qis != "") {
            imConfig["qis"] = retailerCommand.qis;
        } else {
            imConfig["qis"] = "Qis";
        }
        if (retailerCommand?.qoo != "") {
            imConfig["qoo"] = retailerCommand.qoo;
        } else {
            imConfig["qoo"] = "Qoo";
        }
        if (retailerCommand?.user != "") {
            imConfig["user"] = retailerCommand.user;
        } else {
            imConfig["user"] = "User";
        }
        if (retailerCommand?.store != "") {
            imConfig["store"] = retailerCommand.store;
        } else {
            imConfig["store"] = "Store";
        }

        // NOTE - This function will update both config and imConfig depending what you pass it
        retailerConfigService.saveRetailerConfig(null, imConfig)

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

    MultipartFile brandLogo
    String product
    String pack
    String qis
    String qoo
    String user
    String store
}