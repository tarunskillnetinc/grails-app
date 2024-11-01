package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.CharitySale
import uk.co.wonderlane.wlpos.reporting.SortParams

class ReportingControllerCharitySalesSpec  extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController>, DataTest {

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

        controller.storeService = Stub(StoreService) {
            getStores(_) >> new ArrayList<>()
        }
    }

    def 'Should generate the charity donations report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.charityDonations()

        then:
        view == '/reporting/charityDonations.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "01/08/2024" | "30/08/2024"
    }

    def 'Should generate the charity donations report successfully - ajaxCharityDonations'() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['storeFilter'] = storeFilter
        params['csv'] = csv

        controller.reportingService = Stub(ReportingService) {
            CharitySale charitySale1 = getMockCharitySale(2, 105, 5, 7, 1045, BigDecimal.valueOf(9.99), BigDecimal.valueOf(0.09), new DateTime())
            CharitySale charitySale2 = getMockCharitySale(2, 105, 5, 7, 1046, BigDecimal.valueOf(5.00), BigDecimal.valueOf(0.05), new DateTime())

            getCharityDonations(_, _, _, _, _, _, _) >> new TestPagedResultList(new ArrayList(List.of(charitySale1, charitySale2)))
        }

        when:
        def mockView = '<div class="_charityDonationsResults"> </div>'
        views['/reporting/_charityDonationsResults.gsp'] = mockView

        controller.ajaxCharityDonations(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            assert model
            assert model.donations
            assert model.totalResults
        }

        where:
        startDate    | endDate      | storeFilter | csv     | sortColumn        | sortOrder
        "01/08/2020" | "31/08/2024" | null        | "false" | "storeNumber"     | "asc"
        "01/08/2020" | "31/08/2024" | null        | "false" | "tillId"          | "desc"
        "01/08/2020" | "31/08/2024" | null        | "true"  | "basketTotal"     | "asc"
        "01/08/2020" | "31/08/2024" | "1"         | "false" | "donationTotal"   | "asc"
    }

    CharitySale getMockCharitySale(int storeId, int storeNumber, int retailerId, int tillId, int transactionId, BigDecimal basketTotal, BigDecimal donationTotal, DateTime dateCreated) {
        CharitySale charitySale = new CharitySale()

        charitySale.setStoreId(storeId)
        charitySale.setStoreNumber(storeNumber)
        charitySale.setRetailerId(retailerId)
        charitySale.setTillId(tillId)
        charitySale.setTransactionId(transactionId)
        charitySale.setBasketTotal(basketTotal)
        charitySale.setDonationTotal(donationTotal)
        charitySale.setDateCreated(dateCreated)

        return charitySale
    }
}