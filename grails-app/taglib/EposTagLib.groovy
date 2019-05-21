import uk.co.wonderlane.wlpos.ButtonGrid
import uk.co.wonderlane.wlpos.enums.ButtonGridType

class EposTagLib {

    def quicksellMenu = { attrs, body ->
        // TODO Retailer ID, store ID.
        def buttonGrids = ButtonGrid.findAllByTypeAndRetailerIdAndStoreId(ButtonGridType.OTHER, 1, 23034)

        buttonGrids.each { buttonGrid ->
            out << """<a class="dropdown-item" href="${createLink(controller: "buttonGrid", action: "show", id: buttonGrid.id)}">${buttonGrid.description}</a>"""
        }
    }
}