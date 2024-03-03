package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.hibernate.Transaction
import org.joda.time.DateTime
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus

@Transactional(connection="loyalty")
class LoyaltyService extends MySqlDal {

    def springSecurityService

    protected LoyaltyService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getSegment(String searchTerm, String searchBy, int max, int offset, String sortColumn, String sortOrder) {
        def totalCount = Segment.createCriteria().get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    like("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
            projections {
                countDistinct("id")
            }
        }

        def segments = Segment.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    ilike("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
        }

        return [totalCount: totalCount, segments: segments]
    }


    def getLoyaltyOffers(String searchTerm, String searchBy, int max, int offset, String sortColumn, String sortOrder){
        def totalCount = LoyaltyOffer.createCriteria().get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    like("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
            projections {
                countDistinct("id")
            }
        }

        def offers = LoyaltyOffer.createCriteria().list([offset: offset, max: max, sort: sortColumn, order: sortOrder]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    ilike("description", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
        }

        return [totalCount: totalCount, offers: offers]
    }

    List<Segment> getLoyaltySegmentForRetailer(int retailerId){
        return Segment.withCriteria {
            eq ("retailerId", retailerId)
        }
    }

    def getLoyaltyOfferById(int id){
        return LoyaltyOffer.createCriteria().get {
            eq ("id", id)
        }

    }

    protected LoyaltyOffer populateUpdatedOffer(LoyaltyOfferCommand loyaltyOfferCommand) {
        LoyaltyOffer loyaltyOffer = new LoyaltyOffer();
        loyaltyOffer.id = loyaltyOfferCommand.id
        loyaltyOffer.offerDescription = loyaltyOfferCommand.offerDescription
        loyaltyOffer.retailerOfferId = loyaltyOfferCommand.retailerOfferId
        loyaltyOffer.retailerId = loyaltyOfferCommand.retailerId
        loyaltyOffer.status = loyaltyOfferCommand.status
        loyaltyOffer.startDate = loyaltyOfferCommand.startDate
        loyaltyOffer.endDate = loyaltyOfferCommand.endDate
        loyaltyOffer.maxBudget = loyaltyOfferCommand.maxBudget
        loyaltyOffer.maxRedemptions = loyaltyOfferCommand.maxRedemptions
        loyaltyOffer.dateModified =  new DateTime()
        loyaltyOffer.status =  LoyaltyOfferStatus.OPEN
        loyaltyOfferCommand.loyaltyOfferSegments.each {
            it ->
                {
                    LoyaltyOfferSegment loyaltyOfferSegment = new LoyaltyOfferSegment()
                    loyaltyOfferSegment.id = it.id
                    loyaltyOfferSegment.offerId = it.offerId
                    loyaltyOfferSegment.segmentId = it.segmentId
                    loyaltyOfferSegment.loyaltyOffer = loyaltyOffer
                    loyaltyOffer.addToLoyaltyOfferSegments(loyaltyOfferSegment)

                }
        }
        return loyaltyOffer
    }

    protected List<LoyaltyOfferSegment> updateLoyaltySegments(LoyaltyOffer updatedOffer, LoyaltyOffer originalLoyaltyOffer){
        List<LoyaltyOfferSegment> LoyaltyOfferSegments = new ArrayList<>()
        List<LoyaltyOfferSegment> originalOfferSegments = originalLoyaltyOffer.getLoyaltyOfferSegments()
        List<LoyaltyOfferSegment> updatedOfferSegments = updatedOffer.getLoyaltyOfferSegments()

        //Loop over existing loyalty segments to identify deleted segments
        originalOfferSegments?.each {
            originalOfferSegment ->
                def updatedOfferSegment = updatedOfferSegments?.find { updatedOfferSegment -> updatedOfferSegment.id == originalOfferSegment.id }
                if (!updatedOfferSegment){
                    originalOfferSegment.delete = true
                }
                LoyaltyOfferSegments.add(originalOfferSegment)
        }

        //Loop over updated loyalty segments to identify newly added segments
        updatedOfferSegments?.each {
            updatedOfferSegment ->
                def originalOfferSegment = originalOfferSegments?.find { originalOfferSegment -> originalOfferSegment.id == updatedOfferSegment.id }
                if (!originalOfferSegment){
                    updatedOfferSegment.delete = false
                }
                LoyaltyOfferSegments.add(originalOfferSegment)
        }

        return LoyaltyOfferSegments

    }

    @Transactional(connection="loyalty")
    def loyaltyOfferSave(LoyaltyOffer updatedOffer){
        try {
            if (updatedOffer.validate() && !updatedOffer.hasErrors()){
                updatedOffer.save(flush: true)
            }
        }catch(Exception ex){

        }
    }

    def pushLoyaltyOfferIntoRabbitMQ(){

    }

    def getEligibleOfferStatus(){
        return Arrays.asList(LoyaltyOfferStatus.values());
    }
}
