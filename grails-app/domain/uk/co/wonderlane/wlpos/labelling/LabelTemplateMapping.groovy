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

    @Override
    boolean equals(that) {
        if (this.is(that)) return true
        if (getClass() != that.class) return false

        LabelTemplateMapping labelTemplateMapping = (LabelTemplateMapping)that
        if (retailerId != labelTemplateMapping.retailerId || printProcess != labelTemplateMapping.printProcess || printType != labelTemplateMapping.printType || labelTemplate?.id != labelTemplateMapping.labelTemplate?.id) {
            return false
        }

        return true
    }

    @Override
    int hashCode() {
        return retailerId.hashCode() + printProcess?.hashCode() ?: 123 + printType?.hashCode() ?: 234 + labelTemplate?.id?.hashCode() ?: 345
    }
}