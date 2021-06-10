package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.ButtonGridType

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
}