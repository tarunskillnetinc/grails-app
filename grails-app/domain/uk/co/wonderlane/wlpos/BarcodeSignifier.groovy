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
        pattern size: 1..45, blank: false, nullable: false
        startIndex blank: true, nullable: true
        length blank: true, nullable: true
        retailerId nullable: false
        type size: 1..20, nullable: false, blank: false
        description size:0..45, blank: true, nullable: true
        receiptDescription size:0..20, blank: true, nullable: true
        checkDigit blank: true, nullable: true
        discountPercentage blank: true, nullable: true
    }
}
