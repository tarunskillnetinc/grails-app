package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import uk.co.wonderlane.wlpos.entities.cashmanagement.CashManagementConfig
import uk.co.wonderlane.wlpos.usertypes.BooleanTypeAdapter

class CashManagementController {

    def springSecurityService
    def cashManagementService

    def gson = new GsonBuilder()
            .registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
            .create()

    def index() {
        CashManagement cashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId,
                springSecurityService.principal.storeId)
        CashManagementConfigViewAdapter cashManagementConfigViewAdapter = null;
        if (cashManagement != null) {
            cashManagementConfigViewAdapter = gson.fromJson(gson.toJson(cashManagement.config),
                    CashManagementConfigViewAdapter.class)
            cashManagementConfigViewAdapter.setTillAutoSnapshotDaysFormat(cashManagementConfigViewAdapter.getTillAutoSnapshotDays())
            cashManagementConfigViewAdapter.setSafeAutoSnapshotDaysFormat(cashManagementConfigViewAdapter.getSafeAutoSnapshotDays())
            cashManagementConfigViewAdapter.setTillShiftsAutoCloseDaysFormat(cashManagementConfigViewAdapter.getTillShiftsAutoCloseDays())
        }
        [config: cashManagementConfigViewAdapter]
    }

    def save(CashManagementFormData cashManagementFormData) {
        def errorMessages = []

        def patternDays = /^(1?2?3?4?5?6?7?)$/
        def patternTime = /^(?:[01]\d|2[0-3]):[0-5]\d$/

        if (cashManagementFormData.automaticCloseDays != null && !(cashManagementFormData.automaticCloseDays ==~ patternDays)) {
            errorMessages << "Automatic close days format incorrect."
        }
        if (cashManagementFormData.automaticCloseTime != null && !(cashManagementFormData.automaticCloseTime ==~ patternTime)) {
            errorMessages << "Automatic close time format incorrect."
        }
        if (cashManagementFormData.rollingFloatValue != null &&  cashManagementFormData.rollingFloatValue > 1500.00) {
            errorMessages << "Rolling float value cannot be exceeded 1500.00"
        }
        if (cashManagementFormData.tillShiftVarianceLimit != null &&  cashManagementFormData.tillShiftVarianceLimit > 1500.00) {
            errorMessages << "Till shift variance limit cannot be exceeded 1500.00"
        }
        if (cashManagementFormData.safeVarianceLimit != null &&  cashManagementFormData.safeVarianceLimit > 1500.00) {
            errorMessages << "Safe variance limit cannot be exceeded 1500.00"
        }
        if (cashManagementFormData.tillAutoSnapshotDays != null && !(cashManagementFormData.tillAutoSnapshotDays ==~ patternDays)) {
            errorMessages << "Till auto snapshot days format incorrect."
        }
        if (cashManagementFormData.tillAutoSnapshotTime != null && !(cashManagementFormData.tillAutoSnapshotTime ==~ patternTime)) {
            errorMessages << "Till auto snapshot time format incorrect."
        }
        if (cashManagementFormData.safeAutoSnapshotDays != null && !(cashManagementFormData.safeAutoSnapshotDays ==~ patternDays)) {
            errorMessages << "Safe auto snapshot days format incorrect."
        }
        if (cashManagementFormData.safeAutoSnapshotTime != null && !(cashManagementFormData.safeAutoSnapshotTime ==~ patternTime)) {
            errorMessages << "Safe auto snapshot time format incorrect."
        }
        if (cashManagementFormData.tillCashHoldingLimit != null &&  cashManagementFormData.tillCashHoldingLimit > 1500.00) {
            errorMessages << "Till cash holding limit cannot be exceeded 1500.00"
        }

        if (errorMessages != null && !errorMessages.isEmpty()) {
            flash.error = errorMessages
            redirect(action: "index")
        } else {
            cashManagementService.saveCashManagement(cashManagementFormData.toConfig())

            flash.message = ["Cash Management saved successfully."]
            redirect(action: "index")
        }
    }

}

class CashManagementFormData implements Validateable {

    Boolean isManualOpen
    Boolean isManualClose
    String automaticCloseDays
    String automaticCloseTime
    Boolean isRollingFloatEnable
    Double rollingFloatValue
    Integer tillShiftRecountLimit
    Double tillShiftVarianceLimit
    Integer safeRecountLimit
    Double safeVarianceLimit
    Boolean isOpenShiftWithoutFloat
    String tillAutoSnapshotDays
    String tillAutoSnapshotTime
    String safeAutoSnapshotDays
    String safeAutoSnapshotTime
    Double tillCashHoldingLimit

    public CashManagementConfig toConfig() {
        CashManagementConfig cashManagementConfig = new CashManagementConfig()
        cashManagementConfig.setTillShiftsManualOpen(isManualOpen != null ? isManualOpen : false)
        cashManagementConfig.setTillShiftsManualClose(isManualClose != null ? isManualClose : false)
        cashManagementConfig.setTillShiftsAutoCloseDays((automaticCloseDays != null && !automaticCloseDays.isEmpty()? automaticCloseDays: "1234567").toCharArray())
        cashManagementConfig.setTillShiftsAutoCloseTime(automaticCloseTime != null ? automaticCloseTime : "22:00")
        cashManagementConfig.setRollingFloatEnabled(isRollingFloatEnable != null ? isRollingFloatEnable : false)
        cashManagementConfig.setRollingFloatValue(rollingFloatValue != null ? rollingFloatValue*100 as int : 0)
        cashManagementConfig.setTillsCashHoldingLimit(tillCashHoldingLimit != null && tillCashHoldingLimit != 0 ? tillCashHoldingLimit * 100 as int : 150000)
        cashManagementConfig.setTillShiftVarianceLimit(tillShiftVarianceLimit != null && tillShiftVarianceLimit != 0 ? tillShiftVarianceLimit * 100 as int: 500)
        cashManagementConfig.setTillShiftRecountLimit(tillShiftRecountLimit != null && tillShiftRecountLimit != 0 ? tillShiftRecountLimit : 3)
        cashManagementConfig.setSafeRecountLimit(safeRecountLimit != null && safeRecountLimit != 0 ? safeRecountLimit : 3)
        cashManagementConfig.setSafeVarianceLimit(safeVarianceLimit != null && safeVarianceLimit != 0 ? safeVarianceLimit * 100 as int : 500)
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
