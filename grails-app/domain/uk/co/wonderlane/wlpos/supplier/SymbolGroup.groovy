package uk.co.wonderlane.wlpos.supplier

class SymbolGroup {

    int id
    String name
    String apiUrl

    static mapping = {
        table "symbolgroup"
        version false

        apiUrl column: "apiUrl"
    }

    static constraints = {
        name nullable: false, maxSize: 50
        apiUrl nullable: false, maxSize: 200
    }

    public uk.co.wonderlane.wlpos.entities.supplier.SymbolGroup getSymbolGroup() {
        uk.co.wonderlane.wlpos.entities.supplier.SymbolGroup symbolGroup = new uk.co.wonderlane.wlpos.entities.supplier.SymbolGroup()

        symbolGroup.setId(id)
        symbolGroup.setName(name)
        symbolGroup.setApiUrl(apiUrl)

        return symbolGroup
    }
}