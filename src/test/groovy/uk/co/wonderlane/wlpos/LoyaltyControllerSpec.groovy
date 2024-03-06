package uk.co.wonderlane.wlpos

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.reporting.SortParams

class LoyaltyControllerSpec extends Specification implements ControllerUnitTest<LoyaltyController>, DataTest {

    def setup() {}

    def cleanup() {}

    void "should return loyalty segment page page"() {

        when: 'loyalty segment action is executed'
        controller.loyaltySegment()

        then: 'loyalty segment response is correct'
        response.status == HttpStatus.OK.value()
    }

    void "should return loyalty offers page page"() {

        when: 'loyalty segment action is executed'
        controller.loyaltyOffers()

        then: 'loyalty offer response is correct'
        response.status == HttpStatus.OK.value()
    }

    void "should return loyalty segment search results successfully"() {

        given:
        params.searchTerm = searchTerm
        params.searchBy = categoryId
        controller.loyaltyService = Stub(LoyaltyService) {
            getSegment(_, _, _,_,_,_) >> []
        }

        when: 'loyalty segment action is executed'
        controller.ajaxSearchLoyaltySegment()

        then: 'loyalty segment search response is correct'
        response.status == HttpStatus.OK.value()
        model.segments != null
        model.totalCount != null

        where:
        ID | searchTerm | categoryId | tagId
        1  | "Test"     | "1"        | "1"
        2  | "Test"     | "100"      | "100"
        3  | null       | null       | null
    }

    void "should return error response when loyalty segment search fails"() {

        given:
        params.searchTerm = searchTerm
        params.searchBy = categoryId
        controller.loyaltyService = Stub(LoyaltyService) {
            getSegment(_, _, _,_,_,_) >> {throw new Exception("Segment loading error")}
        }

        when: 'loyalty segment action is executed'
        controller.ajaxSearchLoyaltySegment()

        then: 'loyalty segment search response is incorrect'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
        model.segments == null
        model.totalCount == null

        where:
        ID | searchTerm | categoryId | tagId
        1  | "Test"     | "1"        | "1"
        2  | "Test"     | "100"      | "100"
        3  | null       | null       | null
    }

    void "should return loyalty offer search results successfully"() {

        given:
        params.searchTerm = searchTerm
        params.searchBy = categoryId
        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOffers(_, _, _,_,_,_) >> []
        }
        SortParams sortParams = new SortParams()

        when: 'loyalty offer action is executed'
        controller.ajaxSearchLoyaltyOffers(sortParams)

        then: 'loyalty offer search is correct'
        response.status == HttpStatus.OK.value()
        model.offers != null
        model.totalCount != null

        where:
        ID | searchTerm | categoryId | tagId
        1  | "Test"     | "1"        | "1"
        2  | "Test"     | "100"      | "100"
        3  | null       | null       | null
    }

    void "should return error response when offer segment search fails"() {

        given:
        params.searchTerm = searchTerm
        params.searchBy = categoryId
        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOffers(_, _, _,_,_,_) >> {throw new Exception("Offer loading error")}
        }
        SortParams sortParams = new SortParams()

        when: 'loyalty offer action is executed'
        controller.ajaxSearchLoyaltyOffers(sortParams)

        then: 'loyalty offer search response is incorrect'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
        model.offers == null
        model.totalCount == null

        where:
        ID | searchTerm | categoryId | tagId
        1  | "Test"     | "1"        | "1"
        2  | "Test"     | "100"      | "100"
        3  | null       | null       | null
    }
}
