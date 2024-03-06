package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.validation.Errors
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.enums.SyncMessageType

import javax.xml.bind.ValidationException

@Transactional(connection="loyalty")
class LoyaltyService extends MySqlDal {

    def springSecurityService
    def messageSource
    def rabbitService

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
                    like("offerDescription", "%$searchTerm%")
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
                    ilike("offerDescription", "%$searchTerm%")
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

    List<LoyaltyOfferSegment> getLoyaltyOfferSegmentsById(int offerId){
        return LoyaltyOfferSegment.withCriteria {
            eq ("offerId", offerId)
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
        originalLoyaltyOffer.dateModified = DateTime.now(DateTimeZone.UTC)
        originalLoyaltyOffer.status =  loyaltyOfferCommand.status
        if (originalLoyaltyOffer.dateCreated == null){
            originalLoyaltyOffer.dateCreated = DateTime.now(DateTimeZone.UTC)
        }
        originalLoyaltyOffer.getLoyaltyOfferSegments().clear()
        loyaltyOfferCommand.loyaltyOfferSegments.each {
            offerSegment ->
                {
                    LoyaltyOfferSegment loyaltyOfferSegment = new LoyaltyOfferSegment()
                    loyaltyOfferSegment.id = offerSegment.id
                    loyaltyOfferSegment.offerId = offerSegment.offerId
                    loyaltyOfferSegment.segmentId = offerSegment.segmentId
                    loyaltyOfferSegment.loyaltyOffer = originalLoyaltyOffer
                    originalLoyaltyOffer.addToLoyaltyOfferSegments(loyaltyOfferSegment)

                }
        }
        return originalLoyaltyOffer
    }

    protected List<LoyaltyOfferSegment> updateLoyaltySegments(List<LoyaltyOfferSegment> originalLoyaltyOfferSegments, List<LoyaltyOfferSegment> updatedLoyaltyOfferSegments){
        List<LoyaltyOfferSegment> loyaltyOfferSegments = new ArrayList<>()

        //Loop over existing loyalty segments to identify deleted segments
        originalLoyaltyOfferSegments?.each {
            originalOfferSegment ->
                def updatedOfferSegment = updatedLoyaltyOfferSegments?.find { updatedOfferSegment -> updatedOfferSegment.id == originalOfferSegment.id }
                if (!updatedOfferSegment){
                    originalOfferSegment.delete = true
                    loyaltyOfferSegments.add(originalOfferSegment)
                }

        }

        //Loop over updated loyalty segments to identify newly added segments
        updatedLoyaltyOfferSegments?.each {
            updatedOfferSegment ->
                def originalOfferSegment = originalLoyaltyOfferSegments?.find { originalOfferSegment -> originalOfferSegment.id == updatedOfferSegment.id }
                if (!originalOfferSegment){
                    updatedOfferSegment.delete = false
                    loyaltyOfferSegments.add(updatedOfferSegment)
                }
        }
        return loyaltyOfferSegments
    }

    @Transactional(connection="loyalty")
    def loyaltyOfferSave(LoyaltyOffer updatedOffer, List<LoyaltyOfferSegment> loyaltyOfferSegmentList){
        try {
            LoyaltyOffer insertedOffer = saveLoyaltyOffer(updatedOffer)
            saveLoyaltySegments(loyaltyOfferSegmentList, insertedOffer)
            pushLoyaltyOfferIntoRabbitMQ(updatedOffer)
        }catch(Exception ex){
            log.error("Error at saving loyalty offer and loyalty offer segments, Exception " + ex)
            throw ex
        }
    }

    def pushLoyaltyOfferIntoRabbitMQ(LoyaltyOffer updatedOffer){
        try {
            SyncMessage loyaltyOfferSyncMessage = new SyncMessage(SyncMessageType.LOYALTY_OFFER, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, null)
            loyaltyOfferSyncMessage.setInsert(true)
            loyaltyOfferSyncMessage.setLoyaltyOffer(updatedOffer.getLoyaltyOffer())
            rabbitService.sendMessage(loyaltyOfferSyncMessage)
        }catch(Exception ex){
            log.error("Error at pushing updated loyalty offer into rabbitMQ, Exception " + ex)
            throw ex
        }
    }

    def getEligibleOfferStatus(){
        return Arrays.asList(LoyaltyOfferStatus.values());
    }

    LoyaltyOffer saveLoyaltyOffer(LoyaltyOffer updatedOffer){
        try {
            updatedOffer.validate()
            if (updatedOffer.hasErrors()) {
                throw new ValidationException("updatedOffer.errors") // Throw an exception with the Errors object
            }
            return updatedOffer.save(flush: true)
        } catch(ValidationException ex){
            log.error("Validation exception saving loyalty offer, Exception " + ex)
            throw ex
        } catch(Exception ex){
            log.error("Error at saving loyalty offer, Exception " + ex)
            throw ex
        }
    }

    def saveLoyaltySegments(List<LoyaltyOfferSegment> loyaltyOfferSegmentList, LoyaltyOffer insertedOffer){
        try {
            for (LoyaltyOfferSegment loyaltyOfferSegment : loyaltyOfferSegmentList){
                loyaltyOfferSegment.validate()
                if (loyaltyOfferSegment.hasErrors()){
                    throw new ValidationException("updatedOffer.errors") // Throw an exception with the Errors object
                } else {
                    if (loyaltyOfferSegment.delete){loyaltyOfferSegment.delete(flush: true)}
                    else {
                        loyaltyOfferSegment.offerId = insertedOffer.id
                        loyaltyOfferSegment.save(flush: true)
                    }
                }
            }
        }catch(ValidationException ex){
            log.error("Validation exception saving loyalty segment, Exception " + ex)
            throw ex
        } catch(Exception ex){
            log.error("Error at saving loyalty segments,  Exception " + ex)
            throw ex
        }
    }

    List<String> extractErrorMessages(Errors errors) {
        Locale locale = new Locale("en","GB");
        List<String> errorMessages = []
        errors.allErrors.each { error ->
            String text = messageSource.getMessage(error, locale)
            errorMessages << text
        }
        return errorMessages
    }
}
