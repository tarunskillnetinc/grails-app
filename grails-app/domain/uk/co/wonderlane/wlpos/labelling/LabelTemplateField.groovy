package uk.co.wonderlane.wlpos.labelling

import uk.co.wonderlane.wlpos.enums.wlim.LabelTemplateFieldType

class LabelTemplateField {

    int id
    LabelTemplateFieldType type
    int x
    int y
    int width
    int height
    int preferredTextSize
    int preferredSmallTextSize
    boolean centrallyAligned
    String defaultValue
    int maxCharacters

    static belongsTo = [ labelTemplate: LabelTemplate ]

    static mapping = {
        table "labeltemplatefield"
        version false

        labelTemplate column: "labelTemplateId"
        type column: "`type`"
        x column: "x"
        y column: "y"
        width column: "width"
        height column: "height"
        preferredTextSize column: "preferredTextSize"
        preferredSmallTextSize column: "preferredSmallTextSize"
        centrallyAligned column: "centrallyAligned"
        defaultValue column: "defaultValue"
        maxCharacters column: "maxCharacters"
    }

    static constraints = {

    }
}