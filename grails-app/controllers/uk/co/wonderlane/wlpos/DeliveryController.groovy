package uk.co.wonderlane.wlpos

import grails.databinding.BindingFormat
import org.joda.time.DateTimeZone
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.FieldError
import uk.co.wonderlane.wlpos.enums.wlim.ProductListStatus
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

class DeliveryController {

    static def timeZone = DateTimeZone.forID("Europe/London")

    def springSecurityService
    def productListService
    def productService
    def storeService
    def availableStores

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        redirect(action: "deliveries")
    }

    def deliveries() {
    }
}