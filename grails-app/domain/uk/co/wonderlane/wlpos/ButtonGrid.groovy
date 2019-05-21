package uk.co.wonderlane.wlpos

class ButtonGrid extends uk.co.wonderlane.wlpos.entities.ButtonGrid {

    Collection<Button> buttons
    static hasMany = [ buttons: Button ]

    static mapping = {
        table "buttonGrid"
        version false

        retailerId column: "retailerId"
        storeId column: "storeId"
        type sqlType: "enum", enumType: "string"
        description column: "`description`"
        buttons lazy: false
    }

    static constraints = {
        retailerId nullable: false
        storeId nullable: false
        type nullable: false
        description nullable: true, blank: true
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

    public Collection<Button> getButtons() {
        return buttons
    }

    public void setButtons(Collection<Button> buttons) {
        this.buttons = buttons
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