package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.loyalty.Member
import uk.co.wonderlane.wlpos.loyalty.MemberTransaction
import uk.co.wonderlane.wlpos.reporting.SortParams

class LoyaltyControllerSpec extends Specification implements ControllerUnitTest<LoyaltyController>, DataTest {

    def setup() {}
    def cleanup() {}

    def "should return the transactions view when requested"() {

        when: 'Transactions is executed'
        controller.transactions("")

        then: 'transactions response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return the offers view when requested"() {

        when: 'offers is executed'
        controller.offers("")

        then: 'offers response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return the add member offer view when requested"() {
        given:
        controller.loyaltyMemberService = Stub(LoyaltyMemberService) {
            searchForAvailableOffersForMember(_, _) >> [new LoyaltyOffer()]
        }

        when:
        controller.addMemberOffer("")

        then: 'add member offer response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return the loyalty member details view when requested"() {
        given:
        controller.loyaltyMemberService = Stub(LoyaltyMemberService) {
            findByCardNumber(_) >> new Member()
        }

        when:
        controller.showMemberDetails("")

        then: 'offers details response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return the member offer details view when requested"() {
        given:
        controller.loyaltyMemberService = Stub(LoyaltyMemberService) {
            getMemberOffer(_) >> new Member()
        }

        when:
        controller.offerDetails("", 1)

        then: 'offers details response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return the transaction details view when requested"() {
        given:
        controller.memberTransactionService = Stub(MemberTransactionService) {
            findTransactionByMemberIdAndId(_, _) >> new MemberTransaction()
        }

        when:
        controller.transactionDetails("1", "2", "3")

        then: 'offers details response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return the add member offer select template when requested"() {
        given:
        controller.loyaltyMemberService = Stub(LoyaltyMemberService) {
            findByCardNumber(_) >> new Member()
        }

        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOfferById(_) >> new LoyaltyOffer()
        }

        when:
        controller.ajaxSelectedOffer("1", "2")

        then: 'ajaxSelectedOffer response is correct'
        response.status == HttpStatus.OK.value()
    }

    def "should return loyalty members page successfully"() {

        when: 'loyalty members action is executed'
        controller.loyaltyMembers()

        then: 'loyalty segment response is correct'
        response.status == HttpStatus.OK.value()
    }

    void "should return loyalty segment page successfully"() {

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

    void "should return loyalty segment search view results successfully"() {

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

    void "should return loyalty offer search view results successfully"() {

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

    void "should return loyalty offer add view successfully"() {

        given:
        params.id = "1"
        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOfferById(Integer.parseInt(params.id)) >> []
            getLoyaltySegmentForRetailer(_) >> []
            getEligibleOfferStatus() >> []

        }
        controller.promotionService = Stub(PromotionService) {
            getPromotionForRetailer(_) >> []

        }
        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'loyalty offer add action is executed'
        controller.showLoyaltyOffer()

        then: 'loyalty offer add results are correct'
        response.status == HttpStatus.OK.value()
        model.loyaltyOffer != null
        model.promotions != null
        model.segments != null
        model.promotionsJson != null
        model.promotionsJson != null
        model.segmentsJson != null
        model.selectedSegmentIds != null
        model.eligibleOfferStatus != null
        if (params.id != null){
            model.isUpdate == true
        } else{
            model.isUpdate == false
        }
        model.defaultStatus == LoyaltyOfferStatus.PENDING
        model.startDate != null
        model.endDate != null

        where:
        ID | id
        1  | 1
        1  | -1
        2  | "Test"
        3  | null
    }

    void "should redirect back to loyalty offer view if loyalty offer add action fail"() {

        given:
        params.id = "1"
        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOfferById(Integer.parseInt(params.id)) >> {throw new Exception("Loyalty Offer Add Action error")}
        }

        when: 'loyalty offer add action is executed'
        controller.showLoyaltyOffer()

        then: 'loyalty offer add action fail and redirect back to loyalty offer view'
        response.status == HttpStatus.FOUND.value() //this is to redirect temporarily
        response.redirectUrl == "/loyalty/loyaltyOffers"
    }

    void "should successfully save loyalty offer save request"() {

        given:
        LoyaltyOfferCommand loyaltyOfferCommand = loyaltyOfferCommandInput
        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOfferById(1) >> []
            getLoyaltyOfferById(2) >> []
            getLoyaltyOfferById(3) >> null
            getLoyaltyOfferById(-1) >> null
            getLoyaltyOfferSegmentsById(loyaltyOfferCommand.id) >> []
            populateUpdatedOffer(_,_) >> []
            updateLoyaltySegments(_,_) >> []
            loyaltyOfferSave(_,_) >> []
        }
        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'loyalty offer save action is executed'
        controller.ajaxSaveLoyaltyOffers(loyaltyOfferCommand)

        then: 'loyalty offer add results are correct'
        response.status == HttpStatus.FOUND.value() //this is to redirect temporarily
        response.redirectUrl == "/loyalty/loyaltyOffers"

        where:
        ID | loyaltyOfferCommandInput
        1  | getLoyaltyOfferCommand(1)
        2  | getLoyaltyOfferCommand(2)
        3  | getLoyaltyOfferCommand(3)
        4  | getLoyaltyOfferCommand(-1)
    }

    void "should send fail response when invalid object pass to save loyalty offer save request"() {

        given:
        LoyaltyOfferCommand loyaltyOfferCommand = loyaltyOfferCommandInput

        when: 'loyalty offer save action is executed'
        controller.ajaxSaveLoyaltyOffers(loyaltyOfferCommand)

        then: 'loyalty offer add results are correct'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value() //this is to redirect temporarily

        where:
        ID | loyaltyOfferCommandInput
        1  | null

    }

    void "should send fail response when save loyalty offer save request fail"() {

        given:
        LoyaltyOfferCommand loyaltyOfferCommand = loyaltyOfferCommandInput
        controller.loyaltyService = Stub(LoyaltyService) {
            getLoyaltyOfferById(1) >> []
            getLoyaltyOfferById(2) >> []
            getLoyaltyOfferById(3) >> null
            getLoyaltyOfferSegmentsById(loyaltyOfferCommand.id) >> []
            populateUpdatedOffer(_,_) >> []
            updateLoyaltySegments(_,_) >> []
            loyaltyOfferSave(_,_) >> {throw new Exception("Offer saving error")}
        }
        controller.springSecurityService = getFakeSpringSecurityService()

        when: 'loyalty offer save action is executed'
        controller.ajaxSaveLoyaltyOffers(loyaltyOfferCommand)

        then: 'loyalty offer add results are correct'
        response.status == HttpStatus.INTERNAL_SERVER_ERROR.value() //this is to redirect temporarily

        where:
        ID | loyaltyOfferCommandInput
        1  | getLoyaltyOfferCommand(1)

    }

    void "should return loyalty offers saving cancel view when requested"() {

        when: 'loyalty offers saving cancel is executed'
        controller.ajaxShowOfferCancelWindow()

        then: 'loyalty offer response is correct'
        response.status == HttpStatus.OK.value()
        model.error_header == "Cancel Loyalty Offer"
        model.error_body == "Are you sure you want to cancel? All unsaved changes will be lost"
    }

    def getLoyaltyOfferCommand(int id){
        LoyaltyOfferCommand loyaltyOfferCommand = new LoyaltyOfferCommand()
        loyaltyOfferCommand.id = id
        loyaltyOfferCommand.offerDescription = "Loyalty Offer Description"
        loyaltyOfferCommand.retailerOfferId = 1
        loyaltyOfferCommand.retailerId = 9
        loyaltyOfferCommand.status = LoyaltyOfferStatus.PENDING
        loyaltyOfferCommand.startDate = new Date()
        loyaltyOfferCommand.endDate = new Date()
        loyaltyOfferCommand.maxBudget = 1000
        loyaltyOfferCommand.maxRedemptions = 1000
        loyaltyOfferCommand.getLoyaltyOfferSegments().addAll(getLoyaltyOfferSegmentCommand(id))
        return loyaltyOfferCommand
    }

    def getLoyaltyOfferSegmentCommand(int offerId){
        ArrayList loyaltyOfferSegments = new ArrayList()
        LoyaltyOfferSegmentCommand loyaltyOfferSegmentCommand1 = new LoyaltyOfferSegmentCommand()
        loyaltyOfferSegmentCommand1.id = 1
        loyaltyOfferSegmentCommand1.offerId = offerId
        loyaltyOfferSegmentCommand1.offerId = 1
        loyaltyOfferSegmentCommand1.count = 0

        loyaltyOfferSegments.add(loyaltyOfferSegments)

        return loyaltyOfferSegments

    }

    def getFakeSpringSecurityService() {
        return Stub(SpringSecurityService) {
            getPrincipal() >> new HashMap() {
                {
                    put("storeId", 100)
                    put("storeNumber", 100)
                    put("retailerId", 9)
                    put("id", 1)
                }
            }
        }
    }
}
