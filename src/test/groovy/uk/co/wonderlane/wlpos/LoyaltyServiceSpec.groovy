package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferType
import uk.co.wonderlane.wlpos.helpers.HibernateTestMockCriteria
import uk.co.wonderlane.wlpos.helpers.TestPagedResultList

class LoyaltyServiceSpec extends Specification implements ServiceUnitTest<LoyaltyService>, DataTest {

    def setup() {
        service.springSecurityService = getFakeSpringSecurityService()

        service.rabbitService = Stub(BackOfficeRabbitService) {isOpen() >> true}
    }

    def cleanup() {}

    Class<?>[] getDomainClassesToMock() {
        return [Segment, LoyaltyOffer, LoyaltyOfferSegment] as Class[]
    }

    def 'Should successfully return segments when requested '() {
        given:
        List<Segment> segmentResultList = new TestPagedResultList(List.of(
                getMockSegment(1, "Segment 1", 1),
                getMockSegment(2, "Segment 2", 1),
                getMockSegment(3, "Loyalty 1", 1)
        ))

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(segmentResultList)
        BuildableCriteria defaultCriteria = Segment.createCriteria()
        Segment.metaClass.static.createCriteria = { return mockCriteria }

        when: 'Get segment action is executed'
        def segmentReturned  = service.getSegment("Segment", searchBy,
                20, 0, "id", "asc")

        then: 'successfully return segment details'
        segmentReturned?.totalCount != null
        segmentReturned?.segments != null

        where:
        ID | searchBy
        1  | "Description"
        2  | "ID"
    }

    def 'Should successfully return loyalty offers when requested '() {
        given:
        List<LoyaltyOffer> segmentResultList = new TestPagedResultList(List.of(
                getMockLoyaltyOffer(1, "Loyalty offer 1", 1),
                getMockLoyaltyOffer(2, "Loyalty offer 2", 1),
                getMockLoyaltyOffer(3, "Loyalty offer 3", 1)
        ))

        HibernateTestMockCriteria mockCriteria = new HibernateTestMockCriteria()
        mockCriteria.getResponses().add(segmentResultList)
        BuildableCriteria defaultCriteria = LoyaltyOffer.createCriteria()
        LoyaltyOffer.metaClass.static.createCriteria = { return mockCriteria }

        when: 'Get segment action is executed'
        def segmentReturned  = service.getLoyaltyOffers("Segment", searchBy,
                20, 0, "id", "asc")

        then: 'successfully return segment details'
        segmentReturned?.totalCount != null
        segmentReturned?.offers != null

        where:
        ID | searchBy
        1  | "Description"
        2  | "ID"
    }

    def 'Should successfully return loyalty segments for selected retailer '() {
        given:
        Segment segment1 = getMockSegment(1, "Segment 1", 1)
        Segment segment2 = getMockSegment(2, "Segment 2", 1)
        segment1.save(flush:true)
        segment2.save(flush:true)


        when: 'Get segment action by retailer id is executed'
        List<Segment> segmentReturned  = service.getLoyaltySegmentForRetailer(1)

        then: 'successfully return segment details'
        segmentReturned != null
        segmentReturned.size() == 2
    }

    def 'Should successfully return loyalty offer for selected retailer '() {
        given:
        LoyaltyOffer loyaltyOffer = getMockLoyaltyOffer(1, "Loyalty Offer 1", 1)
        loyaltyOffer.save(flush:true)


        when: 'Get loyalty offer action by id is executed'
        def returnedLoyaltyOffer  = service.getLoyaltyOfferById(1)

        then: 'successfully return loyalty offer details'
        returnedLoyaltyOffer != null
    }

    def 'Should successfully return loyalty segments for selected offerId '() {
        given:
        LoyaltyOffer loyaltyOffer = getMockLoyaltyOffer(id, "Loyalty Offer 1", 1)
        loyaltyOffer.save(flush:true)


        when: 'Get loyalty offer segment action by offer id is executed'
        List<LoyaltyOfferSegment> returnedLoyaltyOfferSegments  = service.getLoyaltyOfferSegmentsById(1)

        then: 'successfully return loyalty offer segments details'
        returnedLoyaltyOfferSegments.size() == 2

        where:
        ID | id
        1  | 1
    }

    def 'Should successfully update loyalty offer bean by updated values'() {
        given:
        LoyaltyOffer originalLoyaltyOffer = new LoyaltyOffer()
        LoyaltyOfferCommand updatedRequestedLoyaltyOfferCommand = getMockLoyaltyOfferCommand(1, "Loyalty Offer 1", 1)

        when: 'Get loyalty offer segment action by offer id is executed'
        LoyaltyOffer updatedLoyaltyOffer  = service.populateUpdatedOffer(originalLoyaltyOffer, updatedRequestedLoyaltyOfferCommand)

        then: 'successfully return loyalty offer segments details'
        updatedLoyaltyOffer != null
        updatedLoyaltyOffer.id == updatedRequestedLoyaltyOfferCommand.id
        updatedLoyaltyOffer.offerDescription == updatedRequestedLoyaltyOfferCommand.offerDescription
    }

    def 'Should successfully update loyalty segments bean by updated values'() {
        given:
        LoyaltyOffer loyaltyOffer = getMockLoyaltyOffer(offerId, "Loyalty Offer 1", 1)
        List<LoyaltyOfferSegment> originalLoyaltyOffer = service.getLoyaltyOfferSegmentsById(offerId)
        LoyaltyOfferCommand updatedRequestedLoyaltyOfferCommand = getMockLoyaltyOfferCommand(offerId, "Loyalty Offer 1", 1)
        LoyaltyOffer updatedLoyaltyOffer = service.populateUpdatedOffer(loyaltyOffer, updatedRequestedLoyaltyOfferCommand)

        when: 'Get loyalty offer segment action by offer id is executed'
        List<LoyaltyOfferSegment> updatedLoyaltyOfferSegment  = service.updateLoyaltySegments(originalLoyaltyOffer, updatedLoyaltyOffer.loyaltyOfferSegments)

        then: 'successfully return loyalty offer segments details'
        updatedLoyaltyOfferSegment != null
        updatedLoyaltyOfferSegment.size() > 0

        where:
        ID | offerId
        1  | 1
    }

    def 'Should successfully save updated loyalty offer'() {
        given:
        LoyaltyOffer loyaltyOffer = getMockLoyaltyOffer(offerId, "Loyalty Offer 1", 1)
        List<LoyaltyOfferSegment> originalLoyaltyOffer = service.getLoyaltyOfferSegmentsById(offerId)
        LoyaltyOfferCommand updatedRequestedLoyaltyOfferCommand = getMockLoyaltyOfferCommand(offerId, "Loyalty Offer updated description 1", 1)
        LoyaltyOffer updatedLoyaltyOffer = service.populateUpdatedOffer(loyaltyOffer, updatedRequestedLoyaltyOfferCommand)
        List<LoyaltyOfferSegment> updatedLoyaltyOfferSegment  = service.updateLoyaltySegments(originalLoyaltyOffer, updatedLoyaltyOffer.loyaltyOfferSegments)

        when: 'Get loyalty offer segment action by offer id is executed'
        service.loyaltyOfferSave(updatedLoyaltyOffer, updatedLoyaltyOfferSegment)

        then: 'successfully return loyalty offer segments details'
        def updatedLoyaltyOfferFromDb = service.getLoyaltyOfferById(offerId)
        updatedLoyaltyOfferFromDb != null
        updatedLoyaltyOfferFromDb.offerDescription == "Loyalty Offer updated description 1"

        where:
        ID | offerId
        1  | 1
    }

    def 'Should successfully return loyalty offer status'(){

        when: 'Get loyalty offer status list'
        def eligibleStatusList = service.getEligibleOfferStatus()

        then: 'successfully return all offer eligible status list'
        eligibleStatusList != null
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

    def getMockSegment(int id, String description, int retailerId){
        Segment segment = new Segment()
        segment.id = id
        segment.retailerId = retailerId
        segment.description = description
        segment.segmentSql = "Segment SQL"
        segment.count = 1
        return segment
    }

    def getMockLoyaltyOfferSegment(int id, int offerId, int segmentId, LoyaltyOffer loyaltyOffer){
        LoyaltyOfferSegment loyaltyOfferSegment = new LoyaltyOfferSegment()
        loyaltyOfferSegment.id = id
        loyaltyOfferSegment.offerId = offerId
        loyaltyOfferSegment.segmentId = segmentId
        loyaltyOfferSegment.loyaltyOffer = loyaltyOffer
        return loyaltyOfferSegment
    }

    def getMockLoyaltyOffer(int id, String description, int retailerId){
        LoyaltyOffer loyaltyOffer = new LoyaltyOffer()
        loyaltyOffer.id = id
        loyaltyOffer.offerDescription = description
        loyaltyOffer.retailerId = retailerId
        loyaltyOffer.retailerOfferId = 1
        loyaltyOffer.type = LoyaltyOfferType.STANDARD
        loyaltyOffer.status = LoyaltyOfferStatus.PENDING
        loyaltyOffer.startDate = new Date()
        loyaltyOffer.endDate = new Date()
        loyaltyOffer.dateCreated = DateTime.now(DateTimeZone.UTC)
        loyaltyOffer.dateModified = DateTime.now(DateTimeZone.UTC)
        ArrayList<LoyaltyOfferSegment> loyaltyOfferSegmentArrayList = new ArrayList<>()
        loyaltyOfferSegmentArrayList.add(getMockLoyaltyOfferSegment(1, id,  1, loyaltyOffer))
        loyaltyOfferSegmentArrayList.add(getMockLoyaltyOfferSegment(2, id,2, loyaltyOffer))
        loyaltyOffer.loyaltyOfferSegments.addAll(loyaltyOfferSegmentArrayList)
        return loyaltyOffer
    }

    def getMockLoyaltyOfferCommand(int id, String description, int retailerId){
        LoyaltyOfferCommand loyaltyOfferCommand = new LoyaltyOfferCommand()
        loyaltyOfferCommand.id = id
        loyaltyOfferCommand.offerDescription = description
        loyaltyOfferCommand.retailerId = retailerId
        loyaltyOfferCommand.retailerOfferId = 1
        loyaltyOfferCommand.status = LoyaltyOfferStatus.PENDING
        loyaltyOfferCommand.startDate = new Date()
        loyaltyOfferCommand.endDate = new Date()
        ArrayList<LoyaltyOfferSegmentCommand> loyaltyOfferSegmentArrayList = new ArrayList<>()
        loyaltyOfferSegmentArrayList.add(getMockLoyaltyOfferSegmentCommand(1, id,  1))
        loyaltyOfferSegmentArrayList.add(getMockLoyaltyOfferSegmentCommand(2, id,3))
        loyaltyOfferSegmentArrayList.add(getMockLoyaltyOfferSegmentCommand(2, id,4))
        loyaltyOfferCommand.loyaltyOfferSegments.addAll(loyaltyOfferSegmentArrayList)
        return loyaltyOfferCommand
    }

    def getMockLoyaltyOfferSegmentCommand(int id, int offerId, int segmentId){
        LoyaltyOfferSegmentCommand loyaltyOfferSegmentCommand = new LoyaltyOfferSegmentCommand()
        loyaltyOfferSegmentCommand.id = id
        loyaltyOfferSegmentCommand.offerId = offerId
        loyaltyOfferSegmentCommand.segmentId = segmentId
        return loyaltyOfferSegmentCommand
    }
}
