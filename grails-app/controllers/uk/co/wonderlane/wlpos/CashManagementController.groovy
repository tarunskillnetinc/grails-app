package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import uk.co.wonderlane.wlpos.enums.BarcodeSignifierType

class CashManagementController {

    def springSecurityService
    def cashManagementService

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        def cashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        [cashManagement: cashManagement]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def save(CashManagementFormData cashManagementFormData) {
        println cashManagementFormData.automaticCloseDays
    }

}

class CashManagementFormData implements Validateable {

    boolean isManualOpen
    boolean isManualClose
    String automaticCloseDays
    String automaticCloseTime
    boolean isRollingFloatEnable
    Float rollingFloatValue
    Integer tillShiftRecountLimit
    Float tillShiftVarianceLimit
    Integer safeRecountLimit
    Float safeVarianceLimit
    boolean isOpenShiftWithoutFloat
    String tillAutoSnapshotDays
    String tillAutoSnapshotTime
    String safeAutoSnapshotDays
    String safeAutoSnapshotTime
    Float tillCashHoldingLimit

}
