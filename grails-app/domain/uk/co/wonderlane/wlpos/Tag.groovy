package uk.co.wonderlane.wlpos

class Tag {

    int id
    int retailerId
    String description
    Integer maxSellQuantity
    boolean hidden

    static hasMany = [ tagProducts: TagProduct ]

    static mapping = {
        table "tag"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        description column: "`description`"
        maxSellQuantity column: "maxSellQuantity"
        hidden column: "hidden"
        tagProducts cascade: "all,delete-orphan"
    }

    public uk.co.wonderlane.wlpos.entities.Tag getTag() {
        uk.co.wonderlane.wlpos.entities.Tag tag = new uk.co.wonderlane.wlpos.entities.Tag()
        tag.setId(id)
        tag.setDescription(description)
        tag.setMaxSellQuantity(maxSellQuantity)

        tagProducts?.each {
            tag.getTagProducts().add(it.getTagProduct())
        }

        return tag
    }
}