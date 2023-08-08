package uk.co.wonderlane.wlpos

import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.ReportType

abstract class BaseController {
    protected enum SearchType {
        PRODUCT,
        CATEGORY
    }

    def productService
    def categoryService

    abstract getColumns()

    def ajaxSaveColumns() {
        try {
            if (params.reportColumns && params.reportType) {
                def userReportColumns = new JsonSlurper().parseText(params.reportColumns)
                def reportType = ReportType.valueOf(params.reportType)

                def reportColumns = getColumns()

                if (!reportColumns) {
                    reportColumns = new ReportColumns(userId: springSecurityService.principal.id, reportType: reportType)
                }

                userReportColumns?.each { userReportColumn ->
                    if (reportColumns?.columns?.find { it.column == userReportColumn.key }) {
                        reportColumns?.columns?.find { it.column == userReportColumn.key }?.enabled = userReportColumn.value
                    } else {
                        reportColumns.addToColumns(new ReportColumn(column: userReportColumn.key, enabled: userReportColumn.value))
                    }
                }

                if (reportType == ReportType.PRODUCT_SEARCH) {
                    productService.saveColumns(reportColumns)
                } else if (reportType == ReportType.CATEGORY_SEARCH) {
                    categoryService.saveColumns(reportColumns)
                }

                render(status: 200)
            }
        } catch (Exception e) {
            e.printStackTrace()
            render(status: 500, text: "An error occurred saving your report column preferences.")
        }
    }
}
