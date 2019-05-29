package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ButtonGridType

class ButtonGrid {

    int id
    int retailerId
    int storeId
    ButtonGridType type
    String description
    int rows
    int columns
    Collection<Button> buttons

    static hasMany = [ buttons: Button ]

    static mapping = {
        table "buttongrid"
        version false

        retailerId column: "retailerId"
        storeId column: "storeId"
        type sqlType: "enum", enumType: "string"
        description column: "`description`"
        rows column: "`rows`"
        columns column: "`columns`"
        buttons lazy: false
    }

    static constraints = {
        retailerId nullable: false
        storeId nullable: false
        type nullable: false
        description nullable: true, blank: true, validator: { val, obj ->
            if (obj.type == ButtonGridType.OTHER && !val) {
                return false; // Description is not nullable for sub pages.
            }
        }
        rows nullable: false, min: 1
        columns nullable: false, min: 1
        buttons nullable: true
    }

    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false
        ButtonGrid buttonGrid = (ButtonGrid)that
        if (id != buttonGrid.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }

    /**
     * Convert this Groovy ButtonGrid into the pure Java version for data sync etc.
     *
     * @return
     */
    public uk.co.wonderlane.wlpos.entities.ButtonGrid getButtonGrid() {
        uk.co.wonderlane.wlpos.entities.ButtonGrid buttonGrid = new uk.co.wonderlane.wlpos.entities.ButtonGrid()

        buttonGrid.setId(id)
        buttonGrid.setRetailerId(retailerId)
        buttonGrid.setStoreId(storeId)
        buttonGrid.setType(type)
        buttonGrid.setDescription(description)
        buttonGrid.setRows(rows)
        buttonGrid.setColumns(columns)

        Collection<uk.co.wonderlane.wlpos.entities.Button> buttonz = new ArrayList<>()

        buttons.each { buttonz.add(it.getButton()) }

        buttonGrid.setButtons(buttonz)

        return buttonGrid
    }
}