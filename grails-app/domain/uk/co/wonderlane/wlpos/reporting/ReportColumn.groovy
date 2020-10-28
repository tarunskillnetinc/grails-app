package uk.co.wonderlane.wlpos.reporting

class ReportColumn {

    int id
    String column
    boolean enabled

    static belongsTo = [ reportColumns: ReportColumns ]

    static mapping = {
        datasources (["reporting"])

        table "reportcolumn"
        version false

        reportColumns column: "reportColumnsId"
        column column: "`column`"
        enabled column: "`enabled`"
    }

    static constraints = {
        id nullable: false
        column nullable: false, blank: false
        enabled nullable: false
    }
}