package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import uk.co.wonderlane.wlpos.enums.BarcodeSignifierType

class CashManagementController {

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        [cashManagments: []]
    }

}
