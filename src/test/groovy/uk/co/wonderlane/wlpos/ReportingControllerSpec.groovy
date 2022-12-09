package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns

class ReportingControllerSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {
    Class<?>[] getDomainClassesToMock() {
        return [ReportColumns] as Class[]
    }

    def setup() {
        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", 1)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }
    }

    def "Should initialized successfully"() {
        when:
        controller.index()

        then:
        noExceptionThrown()
    }

    def "Should save report columns successfully"() {
        given:
        params['reportColumns'] = getUserReportColumns()
        params['reportType'] = reportType

        controller.reportingService = Stub(ReportingService) {
            getReportColumns(_) >> reportColumns
            saveReportColumns(_) >> void
        }

        when:
        controller.ajaxSaveReportColumns()

        then:
        noExceptionThrown()

        where:
        reportType       | reportColumns
        "PRODUCT_SEARCH" | getMockReportColumns()
        "PRODUCT_SEARCH" | null
    }

    def "Should handle exceptions on save report columns"() {
        given:
        params['reportColumns'] = getUserReportColumns()
        params['reportType'] = reportType

        controller.reportingService = Stub(ReportingService) {
            getReportColumns(_) >> new Exception("Report columns load error")
            saveReportColumns(_) >> void
        }

        when:
        controller.ajaxSaveReportColumns()

        then:
        controller.response
        controller.response.status == 500
        controller.response.text == "An error occurred saving your report column preferences."

        where:
        reportType       | reportColumns
        "PRODUCT_SEARCH" | getMockReportColumns()
        "PRODUCT_SEARCH" | null
    }

    private String getUserReportColumns() {
        return '[{"key":  "column1" , "value" : true}, {"key":  "column2", "value" : true}, {"key":  "column3", "value" : false}, {"key":  "column6", "value" : true}]'
    }

    private ReportColumns getMockReportColumns() {
        Set<ReportColumn> reportColumnSet = new HashSet<>();
        ReportColumns reportColumns = new ReportColumns()

        reportColumnSet.add(getReportColumn(1, "column1"))
        reportColumnSet.add(getReportColumn(2, "column2"))
        reportColumnSet.add(getReportColumn(3, "column3"))
        reportColumnSet.add(getReportColumn(4, "column4"))
        reportColumnSet.add(getReportColumn(5, "column5", false))

        reportColumns.setColumns(reportColumnSet)

        return reportColumns
    }

    private ReportColumn getReportColumn(int id, String column) {
        return getReportColumn(id, column, true)
    }

    private ReportColumn getReportColumn(int id, String column, boolean isEnabled) {
        ReportColumn reportColumn = new ReportColumn("id": id, "column": column)
        reportColumn.setEnabled(isEnabled)

        return reportColumn
    }
}
