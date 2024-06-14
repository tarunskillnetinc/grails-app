package uk.co.wonderlane.wlpos

class BarcodeSignifier {

    int id
    int retailerId
    String pattern
    Integer startIndex
    Integer length
    String type
    String description
    String receiptDescription
    Boolean checkDigit;
    Integer discountPercentage;

    Collection<BarcodeSignifierEmbeddedData> barcodeSignifierEmbeddedDatas = new ArrayList<>()

    static hasMany = [barcodeSignifierEmbeddedDatas: BarcodeSignifierEmbeddedData];

    static  mapping = {
        autowire true
        table "`barcodesignifiers`"
        version false

        id column: "id",  sqlType: 'smallint'
        retailerId column: "retailerId", sqlType: "tinyint"
        pattern column: "pattern"
        startIndex column: "startIndex"
        length column: "length"
        type column: "type"
        description column: "description"
        receiptDescription column: "receiptDescription"
        checkDigit column: "checkDigit", sqlType: "tinyint"
        discountPercentage column: "discountPercentage", sqlType: "tinyint"

        barcodeSignifierEmbeddedDatas joinTable: [name: 'barcodesignifierembeddeddata', key: 'barcodeSignifierId']
    }

    static constraints = {
        pattern blank: true, nullable: true, validator: { val, obj ->
            if (val == null || val.trim().isEmpty()) {
                return ['signifier.pattern.required']
            } else if (val.length() > 45 ) {
                return ['signifier.pattern.charLength']
            }
        }
        startIndex blank: true, nullable: true
        length blank: true, nullable: true, validator: { val, obj ->
            if (val == null) {
                return ['signifier.length.required']
            }
        }
        retailerId blank: true, nullable: true, validator: { val, obj ->
            if (val == null) {
                return ['signifier.retailerId.required']
            }
        }
        type blank: true, nullable: true, validator: { val, obj ->
            if (val == null || val.trim().isEmpty()) {
                return ['signifier.type.required']
            } else if (val.length() > 20 ) {
                return ['signifier.type.charLength']
            }
        }
        description blank: true, nullable: true, validator: { val, obj ->
            if (val != null && val.length() > 45 ) {
                return ['signifier.description.charLength']
            }
        }
        receiptDescription  blank: true, nullable: true, validator: { val, obj ->
            if (val != null && val.length() > 20 ) {
                return ['signifier.receiptDescription.charLength']
            }
        }
        checkDigit blank: true, nullable: true
        discountPercentage blank: true, nullable: true, validator: { val, obj ->
            if (val != null && (val < 0 || val > 100) ) {
                return ['signifier.discountPercentage.charLength']
            }
        }
    }
}
