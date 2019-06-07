import uk.co.wonderlane.wlpos.ButtonGrid
import uk.co.wonderlane.wlpos.enums.ButtonGridType

class EposTagLib {

    def springSecurityService

    def quicksellMenu = { attrs, body ->
        def buttonGrids = ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, springSecurityService.principal.retailerId, springSecurityService.principal.storeId)

        buttonGrids.each { buttonGrid ->
            out << """<a class="dropdown-item" href="${createLink(controller: "buttonGrid", action: "show", id: buttonGrid.id)}">${buttonGrid.description}</a>"""
        }
    }
}