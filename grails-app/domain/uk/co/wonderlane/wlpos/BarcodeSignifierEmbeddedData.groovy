package uk.co.wonderlane.wlpos

class BarcodeSignifierEmbeddedData {

    int id
    String type
    Integer startIndex
    Integer length
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
        type blank: true, nullable: true, validator: { val, obj ->
            if (val == null || val.trim().isEmpty()) {
                return ['embeddeddata.type.required']
            } else if (val.length() > 20 ) {
                return ['embeddeddata.type.charLength']
            }
        }
        startIndex blank: true, nullable: true, validator: { val, obj ->
            if (val == null) {
                return ['embeddeddata.startindex.required']
            }
        }
        length blank: true, nullable: true
        format blank: true, nullable: true, validator: { val, obj ->
            if (val == null || val.trim().isEmpty()) {
                return ['embeddeddata.format.required']
            } else if (val.length() > 6 ) {
                return ['embeddeddata.format.charLength']
            }
        }
    }
}
