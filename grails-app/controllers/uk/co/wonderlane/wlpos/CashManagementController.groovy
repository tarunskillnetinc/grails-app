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

    def index(Integer storeId) {
        CashManagement cashManagement = null;
        cashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId,
                storeId)
        def storeLevelExist = storeId != null && cashManagement != null;
        if (storeId != null && cashManagement == null) {
            cashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId,
                    null)
        }
        CashManagementConfigViewAdapter cashManagementConfigViewAdapter = null
        if (cashManagement != null) {
            cashManagementConfigViewAdapter = gson.fromJson(gson.toJson(cashManagement.config),
                    CashManagementConfigViewAdapter.class)
            cashManagementConfigViewAdapter.setTillAutoSnapshotDaysFormat(cashManagementConfigViewAdapter.getTillAutoSnapshotDays())
            cashManagementConfigViewAdapter.setSafeAutoSnapshotDaysFormat(cashManagementConfigViewAdapter.getSafeAutoSnapshotDays())
            cashManagementConfigViewAdapter.setTillShiftsAutoCloseDaysFormat(cashManagementConfigViewAdapter.getTillShiftsAutoCloseDays())
        }
        if (storeId != null) {
            // Render the example template when storeLevelExist is false
            render(template: "/cashManagement/cashManagementTemp", model: [config: cashManagementConfigViewAdapter, storeLevelExist: storeLevelExist, onlyRetailerLevel: false])
        } else {
            [config: cashManagementConfigViewAdapter, storeLevelExist: storeLevelExist, onlyRetailerLevel: storeId == null]
        }
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

        if (cashManagementFormData.rollingFloatValue == null) {
            errorMessages << "Rolling float value cannot be empty."
        } else if (cashManagementFormData.rollingFloatValue < 1.00 || cashManagementFormData.rollingFloatValue > 999.00) {
            errorMessages << "Rolling float value must have a value between 1.00 and 999.00."
        }
        if (cashManagementFormData.tillShiftVarianceLimit != null &&  (cashManagementFormData.tillShiftVarianceLimit < 0
                || cashManagementFormData.tillShiftVarianceLimit > 999.00)) {
            errorMessages << "Till shift variance limit must have a value between 0.00 and 999.00."
        }
        if (cashManagementFormData.safeVarianceLimit != null &&  (cashManagementFormData.safeVarianceLimit < 0 || cashManagementFormData.safeVarianceLimit > 999.00)) {
            errorMessages << "Safe variance limit must have a value between 0.00 and 999.00."
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
        if (cashManagementFormData.safeAutoSnapshotTime == null || cashManagementFormData.safeAutoSnapshotTime.isEmpty()) {
            errorMessages << "Safe auto snapshot time cannot be empty."
        } else if (!(cashManagementFormData.safeAutoSnapshotTime ==~ patternTime)) {
            errorMessages << "Safe auto snapshot time format incorrect."
        }
        if (cashManagementFormData.tillCashHoldingLimit != null &&  (cashManagementFormData.tillCashHoldingLimit < 1 || cashManagementFormData.tillCashHoldingLimit > 9999.00)) {
            errorMessages << "Till cash holding limit must have a value between 1.00 and 9999.00."
        }
        if (cashManagementFormData.tillShiftRecountLimit == null) {
            errorMessages << "Till shift recount limit cannot be empty."
        } else if (cashManagementFormData.tillShiftRecountLimit < 0 || cashManagementFormData.tillShiftRecountLimit > 99) {
            errorMessages << "The till shift recount limit must have a value between 0 and 99."
        }
        if (cashManagementFormData.safeRecountLimit == null) {
            errorMessages << "Safe recount limit cannot be empty."
        } else if (cashManagementFormData.safeRecountLimit < 0 || cashManagementFormData.safeRecountLimit > 99) {
            errorMessages << "Safe recount limit must have a value between 0 and 99."
        }

        if (errorMessages != null && !errorMessages.isEmpty()) {
            flash.error = errorMessages
            redirect(action: "index")
        } else {
            cashManagementService.saveCashManagement(cashManagementFormData.toConfig(), cashManagementFormData.storeId)

            flash.message = ["Cash Management saved successfully."]
            redirect(action: "index")
        }
    }

}

class CashManagementFormData implements Validateable {

    Integer storeId;
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
        cashManagementConfig.setTillShiftsAutoCloseDays((automaticCloseDays != null ? automaticCloseDays: "").toCharArray())
        cashManagementConfig.setRollingFloatEnabled(isRollingFloatEnable != null ? isRollingFloatEnable : false)
        cashManagementConfig.setRollingFloatValue(rollingFloatValue != null ? rollingFloatValue*100 as int : 0)
        cashManagementConfig.setTillsCashHoldingLimit(tillCashHoldingLimit != null  ? tillCashHoldingLimit * 100 as int : 0)
        cashManagementConfig.setTillShiftVarianceLimit(tillShiftVarianceLimit != null ? tillShiftVarianceLimit * 100 as int: 0)
        cashManagementConfig.setTillShiftRecountLimit(tillShiftRecountLimit != null ? tillShiftRecountLimit : 0)
        cashManagementConfig.setSafeRecountLimit(safeRecountLimit != null ? safeRecountLimit : 0)
        cashManagementConfig.setSafeVarianceLimit(safeVarianceLimit != null ? safeVarianceLimit * 100 as int : 0)
        cashManagementConfig.setOpenShiftWithoutFloat(isOpenShiftWithoutFloat != null ? isOpenShiftWithoutFloat : false)
        cashManagementConfig.setTillAutoSnapshotDays(tillAutoSnapshotDays != null ? tillAutoSnapshotDays.toCharArray() : new char[]{''})
        cashManagementConfig.setTillAutoSnapshotTime(tillAutoSnapshotTime != null ? tillAutoSnapshotTime : "")
        cashManagementConfig.setSafeAutoSnapshotDays(safeAutoSnapshotDays != null ? safeAutoSnapshotDays.toCharArray() : new char[]{''})
        cashManagementConfig.setSafeAutoSnapshotTime(safeAutoSnapshotTime != null ? safeAutoSnapshotTime : "")
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
