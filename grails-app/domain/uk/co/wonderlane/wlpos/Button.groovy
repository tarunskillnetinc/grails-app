package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ButtonType
import uk.co.wonderlane.wlpos.enums.ProcessType
import uk.co.wonderlane.wlpos.enums.TenderType

class Button {

    def springSecurityService

    static belongsTo = [ buttonGrid: ButtonGrid ]

    int id
    ButtonType type
    int row
    int column
    String description
    BigDecimal amount
    Integer quantity
    Long sku
    Integer subPageId
    ProcessType process
    TenderType tenderType

    String bgColour
    String textColour
    boolean imageDisplay
    boolean textDisplay

    Date createdDatetime
    Integer createdUserId
    Date updateDatetime
    Integer updatedUserId

    Integer storeId
    Integer overrideId

    // This constructor is required or dependency injection (springSecurityService) breaks.
    public Button() { }

    static mapping = {
        autowire true
        table 'button'
        version false

        buttonGrid column: "buttonGridId"
        type sqlType: "enum", enumType: 'string'
        process sqlType: "enum", enumType: 'string'
        tenderType sqlType: "enum", enumType: 'string'
        row column: "`row`"
        column column: "`column`"
        sku column: "sku"
        subPageId column: "subPageId"
        tenderType column: "tenderType"
        bgColour column: "bgColour"
        textColour column: "textColour"
        imageDisplay column: "imageDisplay"
        textDisplay column: "textDisplay"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updateDatetime column: "updateDatetime"
        updatedUserId column: "updatedUserId"
        storeId column: "storeId", sqlType: "smallint"
        overrideId column: "overrideId"
    }

    static constraints = {
        id nullable: true
        type nullable: false
        row  nullable: false
        column nullable: false
        description nullable: false, maxSize: 50
        amount nullable: true, min: 0.00, max: 9999.0
        quantity nullable: true, min: 1, max: 999, validator: { val, obj ->
            if (obj.type == ButtonType.PRODUCT && !val) {
                return false; // Quantity is not nullable for product buttons.
            }
        }
        sku nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.PRODUCT && !val) {
                return false; // SKU is not nullable for product buttons.
            }
        }
        subPageId nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.SUB_PAGE && !val) {
                return false; // Sub page ID is not nullable for sub page buttons.
            }
        }
        process nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.PROCESS && !val) {
                return false; // Process is not nullable for process buttons.
            }
        }
        tenderType nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.TENDER && !val) {
                return false; // Tender type is not nullable for tender buttons.
            }
        }
        bgColour nullable: false
        textColour nullable: false
        imageDisplay nullable: false
        textDisplay nullable: false
        createdDatetime nullable: true
        createdUserId nullable: true
        updateDatetime nullable: true
        updatedUserId nullable: true
        storeId nullable: true
        overrideId nullable: true
    }

    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false
        Button button = (Button)that
        if (id != button.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }

    def beforeInsert() {
        createdDatetime = new Date()
        createdUserId = springSecurityService.principal.id
    }

    def beforeUpdate() {
        updateDatetime = new Date()
        updatedUserId = springSecurityService.principal.id
    }

    def replaceWithOverride(Button override, boolean copyId) {
        if (copyId) {
            id = override.id
        }
        type = override.type
        row = override.row
        column = override.column
        description = override.description
        amount = override.amount
        quantity = override.quantity
        sku = override.sku
        subPageId = override.subPageId
        process = override.process
        tenderType = override.tenderType
        bgColour = override.bgColour
        textColour = override.textColour
        imageDisplay = override.imageDisplay
        textDisplay = override.textDisplay
        createdDatetime = override.createdDatetime
        createdUserId = override.createdUserId
        updateDatetime = override.updateDatetime
        updatedUserId = override.updatedUserId
        storeId = override.storeId
        overrideId = override.overrideId
        buttonGrid = override.buttonGrid
    }

    def setBlankFields() {
        description = "BLANK"
        amount = null
        quantity = null
        sku = null
        subPageId = null
        process = null
        tenderType = null
        bgColour = "#FFFFFF"
        textColour = "#000000"
        imageDisplay = false
        textDisplay = false
    }

    /**
     * Convert this Groovy ButtonG into the pure Java version for data sync etc.
     *
     * @return
     */
    public uk.co.wonderlane.wlpos.entities.Button getButton() {
        uk.co.wonderlane.wlpos.entities.Button button = new uk.co.wonderlane.wlpos.entities.Button()

        button.setId(id)
        button.setType(type)
        button.setRow(row)
        button.setColumn(column)
        button.setDescription(description)
        button.setAmount(amount)
        button.setQuantity(quantity)
        button.setSku(sku)
        button.setSubPageId(subPageId)
        button.setProcess(process)
        button.setTenderType(tenderType)
        button.setBgColour(bgColour)
        button.setTextColour(textColour)
        button.setImageDisplay(imageDisplay)
        button.setTextDisplay(textDisplay)
        button.setStoreId(storeId)
        button.setOverrideId(overrideId)

        return button
    }
}