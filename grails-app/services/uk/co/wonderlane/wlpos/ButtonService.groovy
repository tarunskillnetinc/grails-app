package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class ButtonService {

    def saveButton(Button button) {
        button.save()
    }

    def saveButtonGrid(ButtonGrid buttonGrid) {
        buttonGrid.save()
    }
}