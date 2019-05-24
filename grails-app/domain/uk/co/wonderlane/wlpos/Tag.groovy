package uk.co.wonderlane.wlpos

class Tag {

    int id
    String description

    static hasMany = [ products: Product ]
    static belongsTo = Product

    static mapping = {
        table "tag"
        version false

        description column: "`description`"

        products joinTable: [name: 'tagproduct', key: 'tagId', column: 'productId']
    }
}