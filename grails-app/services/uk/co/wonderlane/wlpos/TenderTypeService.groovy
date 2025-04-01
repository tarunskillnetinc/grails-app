package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.ButtonGridType
import uk.co.wonderlane.wlpos.enums.ButtonType

@Transactional
class TenderTypeService {

    def springSecurityService

    TenderType getTenderType(int id) {
        return TenderType.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    void saveTenderType(TenderType tenderType) {
        tenderType.save()
    }

    def getTenderTypes(String searchTerm, boolean includeDeleted, String sort, String order, int offset, int max) {
        def criteria = TenderType.createCriteria()

        def results
        def totalCount = 0

        results = criteria.list([offset: offset, max: max, sort: sort, order: order]) {
            eq("retailerId", springSecurityService.principal.retailerId)

            if (searchTerm) {
                like("name", "%$searchTerm%")
            }

            if (!includeDeleted) {
                eq("deleted", false)
            }
        }

        totalCount = results.totalCount

        return [results, totalCount]
    }

    def overrideTenderButton(List<Button> buttonList, Button newButton) {
        def existingIndex = buttonList.findIndexOf { it.id == newButton.overrideId }

        if (existingIndex != -1) {
            buttonList[existingIndex] = newButton;
        }
    }

    def addTenderButtonIfNotExist(List<Button> buttonList, Button newButton) {
        def existingIndex = buttonList.findIndexOf { it.row == newButton.row && it.column == newButton.column }

        if (existingIndex == -1) {
            buttonList.add(newButton);
        }
    }

    def getApplicableTenderTypes() {
        def tenderTypes = TenderType.findAllByRetailerId(springSecurityService.principal.retailerId)
        def buttonGrids = ButtonGrid.findAllByRetailerIdAndType(springSecurityService.principal.retailerId, ButtonGridType.TENDER)

        def applicableTenderTypes = []
        def activeButtons = [];

        //Build a list of non-overridden buttons
        buttonGrids?.each { ButtonGrid buttonGrid ->
            if (buttonGrid.storeId == null || buttonGrid.storeId == springSecurityService.principal.storeId) {
                buttonGrid.buttons?.each { Button button ->
                    if (button.storeId == null || button.storeId == springSecurityService.principal.storeId) {
                        if (button.overrideId == null) {
                            addTenderButtonIfNotExist(activeButtons, button)
                        }
                    }
                }
            }
        }

        //Replace existing buttons entries with any overrides.
        buttonGrids?.each { ButtonGrid buttonGrid ->
            if (buttonGrid.storeId == null || buttonGrid.storeId == springSecurityService.principal.storeId) {
                buttonGrid.buttons?.each { Button button ->
                    if (button.storeId == null || button.storeId == springSecurityService.principal.storeId) {
                        if (button.overrideId != null) {
                          overrideTenderButton(activeButtons, button)
                        }
                    }
                }
            }
        }

        //Pull the tenders from the active tender buttons
        activeButtons?.each { Button button ->
            if (button.type == ButtonType.TENDER && button.tenderType) {
                applicableTenderTypes.add(tenderTypes?.find { it.id == button.tenderType.id })
            }
        }

        return applicableTenderTypes?.sort { it.name }?.unique()
    }
}
