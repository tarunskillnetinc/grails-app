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
        return ButtonGrid.findByIdAndRetailerId(buttonGridId, springSecurityService.principal.retailerId)
    }

    def getButtonGrid(ButtonGridType type) {
        def buttonGridCriteria = ButtonGrid.createCriteria()

        def buttonGrids = buttonGridCriteria.list() {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                eq ("storeId", springSecurityService.principal.storeId)
                isNull ("storeId")
            }
        }
        if (buttonGrids){
            return buttonGrids?.sort { it.storeId }?.last()
        }
        return null
    }
    def getButtonGridByStoreId(ButtonGridType type, String description) {
        def buttonGridCriteria = ButtonGrid.createCriteria()

        def buttonGrids = buttonGridCriteria.list() {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("description", description)
            eq ("storeId", springSecurityService.principal.storeId)
        }
        if (buttonGrids){
            return buttonGrids?.sort { it.storeId }?.last()
        }
        return null
    }


    def getButtonGrid(ButtonGridType type, String description) {
        def buttonGridCriteria = ButtonGrid.createCriteria()

        def buttonGrids = buttonGridCriteria.list() {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("description", description)
            or {
                eq ("storeId", springSecurityService.principal.storeId)
                isNull ("storeId")
            }
        }
        if (buttonGrids){
            return buttonGrids?.sort { storeId }?.last()
        }
        return null
    }

    def getOtherButtonGrids() {
        def buttonGridCriteria = ButtonGrid.createCriteria()

        def buttonGrids = buttonGridCriteria.list() {
            eq ("type", ButtonGridType.OTHER)
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                eq ("storeId", springSecurityService.principal.storeId)
                isNull ("storeId")
            }
            order ("description")
        }

        // TODO Need to find the store override of each grid, if it exists, otherwise the null one.
        return buttonGrids
    }

    def getAvailableProcesses() {
        return [ProcessType.NAVIGATE_SALES,
                ProcessType.NAVIGATE_QUICK_SELL,
                ProcessType.NAVIGATE_SEARCH,
                ProcessType.NAVIGATE_RECEIPTS,
                ProcessType.NAVIGATE_MANAGER_FUNCTIONS,
                ProcessType.NAVIGATE_CUSTOMER_REFUSAL,
                ProcessType.NAVIGATE_BACK,
                ProcessType.NAVIGATE_REFUND,
                ProcessType.NAVIGATE_ADD_FLOAT,
                ProcessType.NAVIGATE_CASH_LIFT,
                ProcessType.NAVIGATE_PAID_OUT,
                ProcessType.NAVIGATE_TRAINING,
                ProcessType.SAVE_BASKET,
                ProcessType.NAVIGATE_RETRIEVE_BASKET,
                ProcessType.LOCK_TILL,
                ProcessType.VOID_BASKET,
                ProcessType.NO_SALE,
                ProcessType.LOG_OFF,
                ProcessType.NAVIGATE_TO_WLIM,
                ProcessType.NAVIGATE_PAYPOINT,
                ProcessType.NAVIGATE_PAYPOINT_ADMIN,
                ProcessType.NAVIGATE_PAYPOINT_EOD,
                ProcessType.NAVIGATE_X_READ,
                ProcessType.NAVIGATE_Z_READ,
                ProcessType.EDIT_BASKET,
                ProcessType.ACCEPT_AGE_CHECK,
                ProcessType.REPRINT_RECEIPT,
                ProcessType.NAVIGATE_TRANSACTIONS]
    }


}