package uk.co.wonderlane.wlpos

import uk.co.wonderlane.wlpos.enums.ButtonType

class Button extends uk.co.wonderlane.wlpos.entities.Button {

    static belongsTo = [ buttonGrid: ButtonGrid ]

    private Date createdDatetime
    private Integer createdUserId
    private Date updateDatetime
    private Integer updatedUserId

    static mapping = {
        table 'button'
        version false

        buttonGrid column: "buttonGridId"
        type sqlType: "enum", enumType: 'string'
        process sqlType: "enum", enumType: 'string'
        tenderType sqlType: "enum", enumType: 'string'
        row column: "`row`"
        column column: "`column`"
        productId column: "productId"
        subPageId column: "subPageId"
        tenderType column: "tenderType"
        createdDatetime column: "createdDatetime"
        createdUserId column: "createdUserId"
        updateDatetime column: "updateDatetime"
        updatedUserId column: "updatedUserId"
    }

    static constraints = {
        id nullable: true
        type nullable: false
        row  nullable: false
        column nullable: false
        description nullable: false
        amount nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.TENDER && !val) {
                return false; // Amount is not nullable for tender buttons.
            }
        }
        quantity nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.PRODUCT && !val) {
                return false; // Quantity is not nullable for product buttons.
            }
        }
        productId nullable: true, validator: { val, obj ->
            if (obj.type == ButtonType.PRODUCT && !val) {
                return false; // Product ID is not nullable for product buttons.
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
        createdDatetime nullable: true
        createdUserId nullable: true
        updateDatetime nullable: true
        updatedUserId nullable: true
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
        createdUserId = 1 // TODO
    }

    def beforeUpdate() {
        updateDatetime = new Date()
        updatedUserId = 1 // TODO
    }

    Date getCreatedDatetime() {
        return createdDatetime
    }

    void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime
    }

    Integer getCreatedUserId() {
        return createdUserId
    }

    void setCreatedUserId(Integer createdUserId) {
        this.createdUserId = createdUserId
    }

    Date getUpdateDatetime() {
        return updateDatetime
    }

    void setUpdateDatetime(Date updateDatetime) {
        this.updateDatetime = updateDatetime
    }

    Integer getUpdatedUserId() {
        return updatedUserId
    }

    void setUpdatedUserId(Integer updatedUserId) {
        this.updatedUserId = updatedUserId
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
        button.setProductId(productId)
        button.setSubPageId(subPageId)
        button.setProcess(process)
        button.setTenderType(tenderType)

        return button
    }
}