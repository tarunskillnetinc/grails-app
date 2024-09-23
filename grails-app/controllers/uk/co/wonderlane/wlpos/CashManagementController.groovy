package uk.co.wonderlane.wlpos

import com.google.gson.GsonBuilder
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
        CashManagement cashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId,
                storeId)
        def storeLevelExist = storeId != null && cashManagement != null
        def onlyRetailerLevel = storeId == null;
        def isStoreLevelLogin = null;
        if (params.onlyRetailerLevel) {
            onlyRetailerLevel = Boolean.parseBoolean(params.onlyRetailerLevel)
        }
        if (params.storeLevelExist) {
            storeLevelExist = Boolean.parseBoolean(params.storeLevelExist)
        }
        if (params.storeId) {
            storeId = Integer.parseInt(params.storeId)
        }
        if (params.isStoreLevelLogin) {
            isStoreLevelLogin = Boolean.parseBoolean(params.isStoreLevelLogin)
        }
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
        if (storeId != null && (!isStoreLevelLogin || params.isStoreLevelLogin==null)) {
            // Render the example template when storeLevelExist is false
            render(template: "/cashManagement/cashManagementTemp", model: [config: cashManagementConfigViewAdapter, storeLevelExist: storeLevelExist, onlyRetailerLevel: false, storeId:storeId])
        } else {
            [config: cashManagementConfigViewAdapter, storeLevelExist: storeLevelExist, onlyRetailerLevel: onlyRetailerLevel, isStoreLevelLogin:isStoreLevelLogin,  storeId:storeId]
        }
    }

    def save(CashManagementFormData cashManagementFormData) {
        def errorMessages = []

        def onlyRetailerLevel = cashManagementFormData.modelOnlyRetailerLevel

        def patternDays = /^(1?2?3?4?5?6?7?)$/
        def patternTime = /^(?:[01]\d|2[0-3]):[0-5]\d$/

        def expectRetailerConfig = false
        if (onlyRetailerLevel) {
            if (cashManagementFormData.automaticCloseDays != null && !(cashManagementFormData.automaticCloseDays ==~ patternDays)) {
                errorMessages << "Automatic close days format incorrect."
            }
            if (cashManagementFormData.automaticCloseTime != null && !(cashManagementFormData.automaticCloseTime ==~ patternTime)) {
                errorMessages << "Automatic close time format incorrect."
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
        } else {
            CashManagement retailerLevelCashManagement = cashManagementService.getCashManagement(springSecurityService.principal.retailerId,null)
            if (retailerLevelCashManagement == null || retailerLevelCashManagement.getConfig() == null) {
                expectRetailerConfig = true
                errorMessages << "Retailer level Cash Management not configured yet."
            } else {
                //Restricted fields are not allowed to modify.
                preventRestrictedFieldModifications(retailerLevelCashManagement, cashManagementFormData)
            }
        }

        if (!expectRetailerConfig) {
            if (cashManagementFormData.rollingFloatValue == null) {
                errorMessages << "Rolling float value cannot be empty."
            } else if (cashManagementFormData.rollingFloatValue < 1.00 || cashManagementFormData.rollingFloatValue > 999.99) {
                errorMessages << "Rolling float value must have a value between 1.00 and 999.99."
            }
            if (cashManagementFormData.tillShiftVarianceLimit != null && (cashManagementFormData.tillShiftVarianceLimit < 0
                    || cashManagementFormData.tillShiftVarianceLimit > 999.99)) {
                errorMessages << "Till shift variance limit must have a value between 0.00 and 999.99."
            }
            if (cashManagementFormData.safeVarianceLimit != null && (cashManagementFormData.safeVarianceLimit < 0 || cashManagementFormData.safeVarianceLimit > 999.99)) {
                errorMessages << "Safe variance limit must have a value between 0.00 and 999.99."
            }
            if (cashManagementFormData.tillCashHoldingLimit != null && (cashManagementFormData.tillCashHoldingLimit < 1 || cashManagementFormData.tillCashHoldingLimit > 9999.99)) {
                errorMessages << "Till cash holding limit must have a value between 1.00 and 9999.99."
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
        }

        if (errorMessages != null && !errorMessages.isEmpty()) {
            flash.error = errorMessages
            redirect(action: "index", params:[onlyRetailerLevel:cashManagementFormData.modelOnlyRetailerLevel,storeLevelExist:cashManagementFormData.modelStoreLevelExist, storeId:cashManagementFormData.storeId, isStoreLevelLogin:cashManagementFormData.modelIsStoreLevelLogin])
        } else {
            cashManagementService.saveCashManagement(cashManagementFormData.toConfig(), cashManagementFormData.storeId)

            flash.message = ["Configurations saved successfully."]
            redirect(action: "index", params:[onlyRetailerLevel:cashManagementFormData.modelOnlyRetailerLevel,storeLevelExist:cashManagementFormData.storeId != null, storeId:cashManagementFormData.storeId, isStoreLevelLogin:cashManagementFormData.modelIsStoreLevelLogin])
        }
    }

    /**
     * Restricted fields are not allowed to modify.
     */
    private void preventRestrictedFieldModifications(CashManagement retailerLevelCashManagement, CashManagementFormData cashManagementFormData) {
        CashManagementConfig cashManagementConfig = retailerLevelCashManagement.getConfig()
        cashManagementFormData.manualOrAutoOpen = cashManagementConfig.isTillShiftsManualOpen()
        cashManagementFormData.manualOrAutoClose = cashManagementConfig.isTillShiftsManualClose()
        if (cashManagementConfig.getTillShiftsAutoCloseDays() != null && cashManagementConfig.getTillShiftsAutoCloseDays().size() > 0) {
            cashManagementFormData.automaticCloseDays = new String(cashManagementConfig.getTillShiftsAutoCloseDays())
        } else {
            cashManagementFormData.automaticCloseDays = ""
        }
        cashManagementFormData.automaticCloseTime = cashManagementConfig.getTillShiftsAutoCloseTime()
        if (cashManagementConfig.getTillAutoSnapshotDays() != null && cashManagementConfig.getTillAutoSnapshotDays().size() > 0) {
            cashManagementFormData.tillAutoSnapshotDays = new String(cashManagementConfig.getTillAutoSnapshotDays())
        } else {
            cashManagementFormData.tillAutoSnapshotDays = ""
        }
        cashManagementFormData.tillAutoSnapshotTime = cashManagementConfig.getTillAutoSnapshotTime()
        if (cashManagementConfig.getSafeAutoSnapshotDays() != null && cashManagementConfig.getSafeAutoSnapshotDays().size() > 0) {
            cashManagementFormData.safeAutoSnapshotDays = new String(cashManagementConfig.getSafeAutoSnapshotDays())
        } else {
            cashManagementFormData.safeAutoSnapshotDays = ""
        }
        cashManagementFormData.safeAutoSnapshotTime = cashManagementConfig.getSafeAutoSnapshotTime()
    }

    def deleteStoreLevelConfig(Integer storeId) {
        if (params.storeId) {
            storeId = Integer.parseInt(params.storeId)
        }
        def isStoreLevelLogin = params.isStoreLevelLogin
        cashManagementService.deleteStoreLevelConfig(storeId)
        flash.message = ["Successfully reverted to retailer level."]
        redirect(action: "index", params:[isStoreLevelLogin:isStoreLevelLogin, storeId: storeId])
    }

}

class CashManagementFormData implements Validateable {

    Integer storeId;
    String manualOrAutoOpen
    String manualOrAutoClose
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
    boolean modelOnlyRetailerLevel
    boolean modelStoreLevelExist
    Boolean modelIsStoreLevelLogin

    public CashManagementConfig toConfig() {
        CashManagementConfig cashManagementConfig = new CashManagementConfig()
        cashManagementConfig.setTillShiftsManualOpen(manualOrAutoOpen == "manual")
        cashManagementConfig.setTillShiftsManualClose(manualOrAutoClose == "manual")
        cashManagementConfig.setTillShiftsAutoCloseDays((automaticCloseDays != null ? automaticCloseDays: "").toCharArray())
        cashManagementConfig.setTillShiftsAutoCloseTime(automaticCloseTime != null ? automaticCloseTime : "")
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
