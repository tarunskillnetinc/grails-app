package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import org.joda.time.LocalTime
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter
import uk.co.wonderlane.wlpos.utils.DateTimeUtils

class CashManagementController {

    def springSecurityService
    def cashManagementService

    def gson = new GsonBuilder()
            .registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
            .create()

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def index() {
        CashManagement cashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId,
                springSecurityService.principal.storeId)
        CashManagementConfigViewAdapter cashManagementConfigViewAdapter = gson.fromJson(gson.toJson(cashManagement.config),
                CashManagementConfigViewAdapter.class)
        cashManagementConfigViewAdapter.setTillAutoSnapshotDaysFormat(cashManagementConfigViewAdapter.getTillAutoSnapshotDays())
        cashManagementConfigViewAdapter.setSafeAutoSnapshotDaysFormat(cashManagementConfigViewAdapter.getSafeAutoSnapshotDays())
        cashManagementConfigViewAdapter.setTillShiftsAutoCloseDaysFormat(cashManagementConfigViewAdapter.getTillShiftsAutoCloseDays())
        [config: cashManagementConfigViewAdapter]
    }

    @Secured(['ROLE_ENGINEER', 'ROLE_HEAD_OFFICE'])
    def save(CashManagementFormData cashManagementFormData) {
        cashManagementService.saveCashManagement(cashManagementFormData.toConfig())

        flash.message = ["Cash Management saved successfully."]
        redirect(action: "index")
    }

}

class CashManagementFormData implements Validateable {

    Boolean isManualOpen
    Boolean isManualClose
    String automaticCloseDays
    String automaticCloseTime
    Boolean isRollingFloatEnable
    Integer rollingFloatValue
    Integer tillShiftRecountLimit
    Integer tillShiftVarianceLimit
    Integer safeRecountLimit
    Integer safeVarianceLimit
    Boolean isOpenShiftWithoutFloat
    String tillAutoSnapshotDays
    String tillAutoSnapshotTime
    String safeAutoSnapshotDays
    String safeAutoSnapshotTime
    Integer tillCashHoldingLimit

    public CashManagementConfig toConfig() {
        CashManagementConfig cashManagementConfig = new CashManagementConfig()
        cashManagementConfig.setTillShiftsManualOpen(isManualOpen != null ? isManualOpen : false)
        cashManagementConfig.setTillShiftsManualClose(isManualClose != null ? isManualClose : false)
        cashManagementConfig.setTillShiftsAutoCloseDays((automaticCloseDays != null ? automaticCloseDays: "1234567").toCharArray())
        cashManagementConfig.setTillShiftsAutoCloseTime(automaticCloseTime != null ? automaticCloseTime : "22:00")
        cashManagementConfig.setRollingFloatEnabled(isRollingFloatEnable != null ? isRollingFloatEnable : false)
        cashManagementConfig.setRollingFloatValue(rollingFloatValue != null ? rollingFloatValue : 0)
        cashManagementConfig.setTillsCashHoldingLimit(tillCashHoldingLimit != null ? tillCashHoldingLimit : 150000)
        cashManagementConfig.setTillShiftVarianceLimit(tillShiftVarianceLimit != null ? tillShiftVarianceLimit : 500)
        cashManagementConfig.setTillShiftRecountLimit(tillShiftRecountLimit != null ? tillShiftRecountLimit : 3)
        cashManagementConfig.setSafeRecountLimit(safeRecountLimit != null ? safeRecountLimit : 3)
        cashManagementConfig.setSafeVarianceLimit(safeVarianceLimit != null ? safeVarianceLimit : 500)
        cashManagementConfig.setOpenShiftWithoutFloat(isOpenShiftWithoutFloat != null ? isOpenShiftWithoutFloat : true)
        cashManagementConfig.setTillAutoSnapshotDays((tillAutoSnapshotDays != null ? tillAutoSnapshotDays : "1234567").toCharArray())
        cashManagementConfig.setTillAutoSnapshotTime(tillAutoSnapshotTime != null ? tillAutoSnapshotTime : "22:00")
        cashManagementConfig.setSafeAutoSnapshotDays((safeAutoSnapshotDays != null ? safeAutoSnapshotDays :"1234567").toCharArray())
        cashManagementConfig.setSafeAutoSnapshotTime(safeAutoSnapshotTime != null ? safeAutoSnapshotTime : "22:00")
        return cashManagementConfig
    }
}

class CashManagementConfigViewAdapter extends CashManagementConfig {
    String automaticCloseDaysFormatted
    String tillAutoSnapshotDaysFormatted
    String safeAutoSnapshotDaysFormatted

    void setTillShiftsAutoCloseDaysFormat(char[] tillShiftsAutoCloseDays) {
        this.automaticCloseDaysFormatted = new String(tillShiftsAutoCloseDays)
    }

    void setTillAutoSnapshotDaysFormat(char[] tillAutoSnapshotDays) {
        this.tillAutoSnapshotDaysFormatted = new String(tillAutoSnapshotDays)
    }

    void setSafeAutoSnapshotDaysFormat(char[] safeAutoSnapshotDays) {
        this.safeAutoSnapshotDaysFormatted = new String(safeAutoSnapshotDays)
    }

    String getAutomaticCloseDaysFormatted() {
        return automaticCloseDaysFormatted
    }

    String getTillAutoSnapshotDaysFormatted() {
        return tillAutoSnapshotDaysFormatted
    }

    String getSafeAutoSnapshotDaysFormatted() {
        return safeAutoSnapshotDaysFormatted
    }
}
