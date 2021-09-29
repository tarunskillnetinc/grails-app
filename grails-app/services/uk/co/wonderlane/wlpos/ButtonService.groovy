package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.ProcessType

@Transactional
class ButtonService {

    def springSecurityService

    def saveButton(Button button) {
        button.save()
    }

    def saveButtonGrid(ButtonGrid buttonGrid) {
        buttonGrid.save()
    }

    def deleteButton(Button button) {
        button.delete()
    }

    def getButtonGrid(int buttonGridId) {
        return ButtonGrid.findByIdAndRetailerIdAndStoreId(buttonGridId, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def getButtonGrid(ButtonGridType type) {
        return ButtonGrid.findByTypeAndRetailerIdAndStoreId(type, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }

    def getAvailableProcesses() {
        return [ProcessType.NAVIGATE_SALES, ProcessType.NAVIGATE_QUICK_SELL, ProcessType.NAVIGATE_SEARCH, ProcessType.NAVIGATE_RECEIPTS, ProcessType.NAVIGATE_MANAGER_FUNCTIONS,
                ProcessType.NAVIGATE_CUSTOMER_REFUSAL, ProcessType.NAVIGATE_BACK, ProcessType.NAVIGATE_REFUND, ProcessType.NAVIGATE_ADD_FLOAT, ProcessType.NAVIGATE_CASH_LIFT,
                ProcessType.NAVIGATE_PAID_OUT, ProcessType.NAVIGATE_TRAINING, ProcessType.NAVIGATE_CREATE_DOCKET, ProcessType.NAVIGATE_COMPLETE_DOCKET, ProcessType.NAVIGATE_DISCOUNT,
                ProcessType.SAVE_BASKET, ProcessType.NAVIGATE_RETRIEVE_BASKET, ProcessType.LOCK_TILL, ProcessType.VOID_BASKET, ProcessType.NO_SALE, ProcessType.LOG_OFF, ProcessType.NAVIGATE_TO_WLIM]
    }

    def getAvailableSubPages() {
        return ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
    }
}