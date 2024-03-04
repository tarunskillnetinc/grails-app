package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
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

    protected LoyaltyOffer populateUpdatedOffer(LoyaltyOffer originalLoyaltyOffer, LoyaltyOfferCommand loyaltyOfferCommand) {
        originalLoyaltyOffer.id = loyaltyOfferCommand.id
        originalLoyaltyOffer.offerDescription = loyaltyOfferCommand.offerDescription
        originalLoyaltyOffer.retailerOfferId = loyaltyOfferCommand.retailerOfferId
        originalLoyaltyOffer.retailerId = springSecurityService.principal.retailerId
        originalLoyaltyOffer.status = loyaltyOfferCommand.status
        originalLoyaltyOffer.startDate = loyaltyOfferCommand.startDate
        originalLoyaltyOffer.endDate = loyaltyOfferCommand.endDate
        originalLoyaltyOffer.maxBudget = loyaltyOfferCommand.maxBudget
        originalLoyaltyOffer.maxRedemptions = loyaltyOfferCommand.maxRedemptions
        originalLoyaltyOffer.dateModified =  new DateTime()
        originalLoyaltyOffer.status =  loyaltyOfferCommand.status
//        loyaltyOfferCommand.loyaltyOfferSegments.each {
//            offerSegment ->
//                {
//                    LoyaltyOfferSegment loyaltyOfferSegment = new LoyaltyOfferSegment()
//                    loyaltyOfferSegment.id = offerSegment.id
//                    loyaltyOfferSegment.offerId = offerSegment.offerId
//                    loyaltyOfferSegment.segmentId = offerSegment.segmentId
//                    loyaltyOfferSegment.loyaltyOffer = originalLoyaltyOffer
//                    originalLoyaltyOffer.addToLoyaltyOfferSegments(loyaltyOfferSegment)
//
//                }
//        }
        return originalLoyaltyOffer
    }

    protected List<LoyaltyOfferSegment> updateLoyaltySegments(LoyaltyOfferCommand loyaltyOfferCommand, LoyaltyOffer originalLoyaltyOffer){
        List<LoyaltyOfferSegment> loyaltyOfferSegments = new ArrayList<>()
        List<LoyaltyOfferSegment> originalOfferSegments = originalLoyaltyOffer.getLoyaltyOfferSegments()
        List<LoyaltyOfferSegmentCommand> updatedOfferSegments = loyaltyOfferCommand.getLoyaltyOfferSegments()

        //Loop over existing loyalty segments to identify deleted segments
        originalOfferSegments?.each {
            originalOfferSegment ->
                def updatedOfferSegment = updatedOfferSegments?.find { updatedOfferSegment -> updatedOfferSegment.id == originalOfferSegment.id }
                if (!updatedOfferSegment){
                    originalOfferSegment.delete = true
                    loyaltyOfferSegments.add(originalOfferSegment)
                }

        }

        //Loop over updated loyalty segments to identify newly added segments
        updatedOfferSegments?.each {
            updatedOfferSegment ->
                def originalOfferSegment = originalOfferSegments?.find { originalOfferSegment -> originalOfferSegment.id == updatedOfferSegment.id }
                if (!originalOfferSegment){
                    LoyaltyOfferSegment loyaltyOfferSegment = new LoyaltyOfferSegment()
                    loyaltyOfferSegment.id = updatedOfferSegment.id
                    loyaltyOfferSegment.offerId = updatedOfferSegment.offerId
                    loyaltyOfferSegment.segmentId = updatedOfferSegment.segmentId
                    loyaltyOfferSegment.loyaltyOffer = originalLoyaltyOffer
                    loyaltyOfferSegment.delete = false
                    loyaltyOfferSegments.add(loyaltyOfferSegment)
                }

        }
        return loyaltyOfferSegments
    }

    @Transactional(connection="loyalty")
    def loyaltyOfferSave(LoyaltyOffer updatedOffer, List<LoyaltyOfferSegment> loyaltyOfferSegmentList){
        try {
            updateLoyaltyOffer(updatedOffer)
            updateLoyaltySegments(loyaltyOfferSegmentList)
        }catch(Exception ex){
            log.error("Error at saving loyalty offer and loyalty offer segments, Exception " + ex)
            throw ex
        }
    }

    def pushLoyaltyOfferIntoRabbitMQ(){

    }

    def getEligibleOfferStatus(){
        return Arrays.asList(LoyaltyOfferStatus.values());
    }

    def updateLoyaltyOffer(LoyaltyOffer updatedOffer){
        try {
            if (updatedOffer.validate() && !updatedOffer.hasErrors()){
                updatedOffer.save(flush: true)
            }
        }catch(Exception ex){
            log.error("Error at saving loyalty offer, Exception " + ex)
            throw ex
        }

    }

    def updateLoyaltySegments(List<LoyaltyOfferSegment> loyaltyOfferSegmentList){
        try {
            for (LoyaltyOfferSegment loyaltyOfferSegment : loyaltyOfferSegmentList){
                if (loyaltyOfferSegment.validate() && !loyaltyOfferSegment.hasErrors()){
                    if (loyaltyOfferSegment.delete){
                        loyaltyOfferSegment.delete(flush: true)
                    } else {
                        loyaltyOfferSegment.save(flush: true)
                    }
                }
            }
        }catch(Exception ex){
            log.error("Error at saving loyalty segments,  Exception " + ex)
            throw ex
        }
    }
}
