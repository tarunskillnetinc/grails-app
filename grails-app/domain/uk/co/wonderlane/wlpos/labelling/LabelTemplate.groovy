package uk.co.wonderlane.wlpos.labelling

class LabelTemplate {

    int id
    int retailerId
    String name
    BigDecimal marginLeft
    BigDecimal marginTop
    BigDecimal labelHeight
    BigDecimal labelWidth
    BigDecimal marginBetweenColumns
    BigDecimal marginBetweenRows
    int columns
    int rows
    String stationeryCode
    boolean rotateOrientation

    static hasMany = [ labelTemplateFields: LabelTemplateField, labelTemplateMappings: LabelTemplateMapping ]

    static mapping = {
        table "labeltemplate"
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        name column: "name"
        marginLeft column: "marginLeft"
        marginTop column: "marginTop"
        labelHeight column: "labelHeight"
        labelWidth column: "labelWidth"
        marginBetweenColumns column: "marginBetweenColumns"
        marginBetweenRows column: "marginBetweenRows"
        columns column: "columns"
        rows column: "rows"
        stationeryCode column: "stationeryCode"
        rotateOrientation column: "rotateOrientation"
    }

    static constraints = {

    }
}