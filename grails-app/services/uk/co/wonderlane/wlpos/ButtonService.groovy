package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.ProcessType

@Transactional
class ButtonService {

    def springSecurityService
    def imageRecordService

    def saveButton(Button button) {
        button.save()
    }

    def saveButtonGrid(ButtonGrid buttonGrid) {
        buttonGrid.save()
    }

    def deleteButton(Button button) {
        button.delete()
    }

    def deleteButtonGrid(ButtonGrid grid) {
        grid.delete()
    }

    def getButtonGrid(int buttonGridId) {
        ButtonGrid grid = ButtonGrid.findByIdAndRetailerId(buttonGridId, springSecurityService.principal.retailerId)
        if (springSecurityService.principal.storeId != null) {
            addOverriddenButtons(grid)
        }
        return grid
    }

    // delete button with id matching `btnId` if it is an overridden button
    def deleteOverrideBtn(int btnId) {
        def storeId = springSecurityService.principal.storeId
        Button btn = Button.findById(btnId)
        if (btn != null && btn.overrideId != null && storeId != null && btn.storeId == storeId) {
            btn.delete()
        }
    }

    // delete all store-level overrides created for retailer-level button matching `btnId`
    def deleteOverrides(int btnId) {
        ArrayList<Button> overrides = new ArrayList<>()
        overrides.addAll(Button.findAllByOverrideId(btnId))
        overrides.forEach { it.delete() }
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
            ButtonGrid grid = buttonGrids?.sort { it.storeId }?.last()
            if (springSecurityService.principal.storeId != null) {
                addOverriddenButtons(grid)
            }
            return grid
        }

        return null
    }

    def addOverriddenButtons(ButtonGrid grid) {
        grid.buttons.forEach {
            Button override = Button.findByOverrideIdAndStoreId(it.id, springSecurityService.principal.storeId)
            if (override != null) {
                it.replaceWithOverride(override, true)
            }
        }
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
        } else if (buttonGridType.is(ButtonGridType.TENDER)) {
            return ProcessType.NAVIGATE_BACK
        }
    }
}