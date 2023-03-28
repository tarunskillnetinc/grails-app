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

    def getButtonGrid(ButtonGridType type, String description, boolean includeHeadOffice) {
        def buttonGridCriteria = ButtonGrid.createCriteria()

        def buttonGrids = buttonGridCriteria.list() {
            eq ("type", type)
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (description != null) {
                eq("description", description)
            }
            or {
                eq ("storeId", springSecurityService.principal.storeId)
                if (includeHeadOffice) {
                    isNull("storeId")
                }
            }
        }

        if (buttonGrids){
            return buttonGrids?.sort { it.storeId }?.last()
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

    def getAvailableProcesses(ButtonGridType buttonGridType) {
        if (buttonGridType.isIn(ButtonGridType.MANAGER_FUNCTIONS, ButtonGridType.OTHER, ButtonGridType.QUICK_SELL, ButtonGridType.SALES)) {
            return ProcessType.values().findAll { it.isAvailableOnTill() }
        } else if (buttonGridType.isIn(ButtonGridType.SCO_MANAGER_FUNCTIONS, ButtonGridType.SCO_QUICK_SELL)) {
            return ProcessType.values().findAll { it.isAvailableOnSco() }
        }
    }
}