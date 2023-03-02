package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.PayPointSale
import uk.co.wonderlane.wlpos.reporting.ReportColumn
import uk.co.wonderlane.wlpos.reporting.ReportColumns
import uk.co.wonderlane.wlpos.reporting.SortParams

class ReportingControllerPayPointSalesSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {
    Class<?>[] getDomainClassesToMock() {
        return [StoreSettings] as Class[]
    }

    def setup() {
        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", 1)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            getReportColumns(_) >> new ArrayList<>()
        }
    }

    def 'Should generate the pay-point sales report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        StoreSettings mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        mockDomain(StoreSettings, [mockStoreSettings])

        when:
        HashMap model = controller.paypointSales()

        then:
        view == '/reporting/paypointSales.gsp'
        model

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the pay-point sales report successfully - ajaxPayPointSales'() {
        given:

        SortParams sortParams = new SortParams()

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['descriptionFilter'] = descriptionFilter
        params['statusFilter'] = statusFilter
        params['csv'] = "false"
        params['storeFilter'] = storeFilter

        controller.reportingService = Stub(ReportingService) {
            getPayPointSales(_, _, _, _, _, _, _, _, _) >> new TestPagedResultList(getPayPointSalesList())
        }

        when:
        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        def mockView = '<div class="_paypointSalesResults"> </div>'
        views['/reporting/_paypointSalesResults.gsp'] = mockView

        controller.ajaxPayPointSales(sortParams)

        then:
        noExceptionThrown()

        assert model
        assert model.sales

        where:
        principalStoreId | storeFilter | descriptionFilter | statusFilter | startDate    | endDate
        1                | null        | "description"     | "Success"    | "11/10/2020" | "11/10/2021"
        1                | null        | "description"     | "Success"    | null         | "11/10/2021"
        null             | "1"         | "description"     | "Success"    | "11/10/2020" | "11/10/2021"
        1                | null        | "description"     | "Pending"    | "11/10/2020" | "11/10/2021"
    }

    def 'Should generate the pay-point csv sales report successfully - ajaxPayPointSales: csv'() {
        given:

        SortParams sortParams = new SortParams()

        params['startDate'] = "11/10/2020"
        params['endDate'] = "11/10/2021"
        params['descriptionFilter'] = "description"
        params['statusFilter'] = "Success"
        params['standard'] = isStandard

        params['csv'] = "true"

        controller.reportingService = Stub(ReportingService) {
            getPayPointSales(_, _, _, _, _, _, _, _, _) >> new TestPagedResultList(getPayPointSalesList())
            getReportColumns(_) >> getMockReportColumns()
        }

        StoreSettings mockStoreSettings = getMockStoreSettings(1, 1, 100)
        mockStoreSettings.springSecurityService = controller.springSecurityService
        mockStoreSettings.save(flush: true, failOnError: true)

        when:
        controller.ajaxPayPointSales(sortParams)

        then:
        noExceptionThrown()

        where:

        isStandard | _
        "true"     | _
        "false"    | _

    }

    private List getPayPointSalesList() {
        List payPointSalesList = new ArrayList()

        PayPointSale payPointSale_1 = getMockPayPointSale(1, 1, 1)
        PayPointSale payPointSale_2 = getMockPayPointSale(2, 1, 1)

        payPointSalesList.add(payPointSale_1)
        payPointSalesList.add(payPointSale_2)

        return payPointSalesList
    }

    private PayPointSale getMockPayPointSale(int id, int retailerId, int storeId) {
        PayPointSale payPointSale = new PayPointSale()

        payPointSale.setId(id)
        payPointSale.setRetailerId(retailerId)
        payPointSale.setStoreId(storeId)
        payPointSale.setTransactionDate(new DateTime())

        return payPointSale
    }

    private ReportColumns getMockReportColumns() {
        Set<ReportColumn> reportColumnSet = new HashSet<>();
        ReportColumns reportColumns = new ReportColumns()

        reportColumnSet.add(getReportColumn(1, "storeId"))
        reportColumnSet.add(getReportColumn(2, "wlTransactionId"))
        reportColumnSet.add(getReportColumn(3, "ppTransactionId"))
        reportColumnSet.add(getReportColumn(4, "terminalId"))
        reportColumnSet.add(getReportColumn(5, "description"))
        reportColumnSet.add(getReportColumn(6, "type"))
        reportColumnSet.add(getReportColumn(7, "value"))
        reportColumnSet.add(getReportColumn(8, "status"))
        reportColumnSet.add(getReportColumn(9, "transactionDate"))

        reportColumns.setColumns(reportColumnSet)

        return reportColumns
    }

    private ReportColumn getReportColumn(int id, String column) {
        ReportColumn reportColumn = new ReportColumn("id": id, "column": column)
        reportColumn.setEnabled(true)

        return reportColumn
    }
}
