package uk.co.wonderlane.wlpos

class BarcodeSignifierEmbeddedData {

    int id
    String type
    int startIndex
    int length
    String format

    static belongsTo = [ barcodeSignifier: BarcodeSignifier ]

    static mapping = {
        table "barcodesignifierembeddeddata"
        version false

        id column: "id"
        type column: "type"
        startIndex column: "startIndex"
        length column: "length"
        format column: "format", sqlType: 'char'
        barcodeSignifier column: "barcodeSignifierId", sqlType: 'smallint'
    }

    static constraints = {
        id nullable: true
        type nullable: false, blank: false
        startIndex nallable: true, blank: true
        length nullable:true, blank: true
        format nullable: false, blank: false
    }
}
