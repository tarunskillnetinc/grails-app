package uk.co.wonderlane.wlpos

class Tag {

    int id
    int retailerId
    String description
    boolean hidden

    static hasMany = [ products: Product ]
    //static belongsTo = Product

    static mapping = {
        table "tag"
        version false

        retailerId column: "retailerId"
        description column: "`description`"
        hidden column: "hidden"

        products joinTable: [name: 'tagproduct', key: 'tagId', column: 'productId']
    }

    public uk.co.wonderlane.wlpos.entities.Tag getTag() {
        uk.co.wonderlane.wlpos.entities.Tag tag = new uk.co.wonderlane.wlpos.entities.Tag()
        tag.setId(id)
        tag.setDescription(description)

        return tag
    }
}