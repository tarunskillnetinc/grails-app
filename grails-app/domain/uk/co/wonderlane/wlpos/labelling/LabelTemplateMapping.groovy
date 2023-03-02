package uk.co.wonderlane.wlpos.labelling

import uk.co.wonderlane.wlpos.enums.wlim.PrintProcess
import uk.co.wonderlane.wlpos.enums.wlim.PrintType

class LabelTemplateMapping implements Serializable {

    int retailerId
    PrintProcess printProcess
    PrintType printType

    static belongsTo = [ labelTemplate: LabelTemplate ]

    static mapping = {
        table "labeltemplatemapping"
        version false

        id composite: ['retailerId', 'printProcess', 'printType', 'labelTemplate']

        retailerId column: "retailerId", sqlType: "tinyint"
        printProcess column: "printProcess"
        printType column: "printType"
        labelTemplate column: "labelTemplateId"
    }

    static constraints = {

    }
}