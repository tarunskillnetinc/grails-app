package uk.co.wonderlane.wlpos.reporting

class ReportColumns {

    int id
    int userId
    ReportType reportType

    static hasMany = [ columns: ReportColumn ]

    static mapping = {
        datasources (["reporting", "reportingReadOnly"])
        table "reportcolumns"
        version false

        userId column: "userId"
        reportType column: "reportType"
    }

    static constraints = {
        id nullable: false
        userId nullable: false
        reportType nullable: false
    }
}