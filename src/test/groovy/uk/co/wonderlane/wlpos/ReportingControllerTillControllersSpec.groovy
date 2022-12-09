package uk.co.wonderlane.wlpos

import grails.gorm.PagedResultList
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import uk.co.wonderlane.wlpos.enums.TillControlEventType
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList
import uk.co.wonderlane.wlpos.reporting.SortParams
import uk.co.wonderlane.wlpos.reporting.TillControlEvent

class ReportingControllerTillControllersSpec extends ReportingControllerSpecBase implements ControllerUnitTest<ReportingController> {
    def setup() {
        controller.reportingService = Stub(ReportingService) {
            getReportColumns(_) >> new ArrayList<>()
        }
    }

    def 'Should generate the Till Controller Event report successfully'() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        when:
        HashMap model = controller.tillControlEvent()

        then:
        view == '/reporting/tillControlEvent.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the till Control Events successfully '() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate

        controller.reportingService = Stub(ReportingService) {}

        when:
        HashMap model = controller.tillControlEvents()

        then:
        view == '/reporting/tillControlEvents.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        where:
        startDate    | endDate
        "11/10/2020" | "11/11/2021"
        null         | "11/11/2021"
    }

    def 'Should generate the till Control Events successfully - ajaxTillControlEvents '() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['promotionId'] = promotionSaleId
        params['csv'] = csv

        controller.springSecurityService = Stub(SpringSecurityService) {
            HashMap map = new HashMap()

            map.put("retailerId", 1)
            map.put("storeId", principalStoreId)
            map.put("usersName", "TEST_USER")

            getPrincipal() >> map
        }

        controller.reportingService = Stub(ReportingService) {
            TillControlEvent event1 = getMockTillControlEvent(1, 1, 1, 1)
            TillControlEvent event2 = getMockTillControlEvent(2, 1, 1, 1)
            TillControlEvent event3 = getMockTillControlEvent(3, 1, 1, 1)
            TillControlEvent event4 = getMockTillControlEvent(4, 1, 1, 1)
            TillControlEvent event5 = getMockTillControlEvent(5, 1, 1, 1)

            event1.setType(TillControlEventType.ADD_FLOAT)
            event2.setType(TillControlEventType.CASH_LIFT)
            event3.setType(TillControlEventType.CASH_LIFT)
            event4.setType(TillControlEventType.COMPLETE_DOCKET)
            event5.setType(TillControlEventType.CUSTOMER_REFUSAL)

            getTillControlEvents(_, _) >> new ArrayList(List.of(event1, event2, event3, event4, event5))
            getReportColumns(_) >> new ArrayList<>()
        }

        when:
        def mockView = '<div class="_tillControlEventsResults"> </div>'
        views['/reporting/_tillControlEventsResults.gsp'] = mockView

        controller.ajaxTillControlEvents(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            model != null
            model.tillControlEvents != null
        }

        where:
        promotionSaleId | startDate    | endDate      | principalStoreId | storeFilter | csv     | sortColumn | sortOrder
        "1"             | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "type"     | "asc"
        "1"             | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "type"     | "desc"
        "2"             | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "quantity" | "asc"
        "2"             | "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "quantity" | "desc"
        "3"             | "11/10/2020" | "11/11/2021" | 1                | null        | "true"  | "type"     | "asc"
        "4"             | null         | "11/11/2021" | null             | "1"         | "false" | "type"     | "asc"
    }

    def 'Should generate the till Control Event successfully '() {
        given:
        params['startDate'] = startDate
        params['endDate'] = endDate
        params['type'] = tillControlEventType

        when:
        HashMap model = controller.tillControlEvent()

        then:
        view == '/reporting/tillControlEvent.gsp'
        model != null
        model.startDate != null
        model.endDate != null

        if (tillControlEventType == "NON_EXISTENT_TYPE") {
            assert model.type == null
        }

        where:
        startDate    | endDate      | tillControlEventType
        "11/10/2020" | "11/11/2021" | "CUSTOMER_REFUSAL"
        null         | "11/11/2021" | "NON_EXISTENT_TYPE"
    }

    def 'Should generate the till Control Events successfully - ajaxTillControlEvent '() {
        given:

        SortParams sortParams = new SortParams()
        sortParams.setSortColumn(sortColumn)
        sortParams.setSortOrder(sortOrder)

        params['startDate'] = startDate
        params['endDate'] = endDate
        params['tillControlEventType'] = tillControlEventType
        params['csv'] = csv

        controller.reportingService = Stub(ReportingService) {
            TillControlEvent event1 = getMockTillControlEvent(1, 1, 1, 1)
            TillControlEvent event2 = getMockTillControlEvent(2, 1, 1, 1)
            TillControlEvent event3 = getMockTillControlEvent(3, 1, 1, 1)
            TillControlEvent event4 = getMockTillControlEvent(4, 1, 1, 1)
            TillControlEvent event5 = getMockTillControlEvent(5, 1, 1, 1)
            TillControlEvent event6 = getMockTillControlEvent(6, 1, 1, 1)
            TillControlEvent event7 = getMockTillControlEvent(7, 1, 1, 1)

            event1.setType(TillControlEventType.CUSTOMER_REFUSAL)
            event2.setType(TillControlEventType.REFUND)
            event3.setType(TillControlEventType.MARKDOWN)
            event4.setType(TillControlEventType.LINE_VOID)
            event5.setType(TillControlEventType.PAID_OUT)
            event6.setType(TillControlEventType.ID_CHECK)
            event7.setType(TillControlEventType.ADD_FLOAT)
            event7.setReason(null)

            getTillControlEvents(_, _, _, _, _, _, _) >> new TestPagedResultList(new ArrayList(
                    List.of(event1, event2, event3, event4, event5, event6, event7)))
        }

        when:
        def mockView = '<div class="_tillControlEventResults"> </div>'
        views['/reporting/_tillControlEventResults.gsp'] = mockView

        controller.ajaxTillControlEvent(sortParams)

        then:
        noExceptionThrown()

        if (csv == "false") {
            model != null
            model.tillControlEvents != null
            model.totalResults == 6
        }

        where:
        startDate    | endDate      | principalStoreId | storeFilter | csv     | sortColumn | sortOrder | tillControlEventType
        "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "type"     | "asc"     | "CUSTOMER_REFUSAL"
        "11/10/2020" | "11/11/2021" | 1                | null        | "false" | "type"     | "desc"    | "NON_EXISTENT_TYPE"
        "11/10/2020" | "11/11/2021" | 1                | null        | "true"  | "type"     | "asc"     | "CUSTOMER_REFUSAL"
        null         | "11/11/2021" | null             | "1"         | "false" | "type"     | "desc"    | "CUSTOMER_REFUSAL"
    }

    private TillControlEvent getMockTillControlEvent(int id, int retailerId, int storeId, int tillId) {
        TillControlEvent tillControlEvent = new TillControlEvent()

        tillControlEvent.setId(id)
        tillControlEvent.setRetailerId(retailerId)
        tillControlEvent.setStoreId(storeId)
        tillControlEvent.setTillId(tillId)
        tillControlEvent.setReason("REASON_" + id)

        return tillControlEvent
    }
}
