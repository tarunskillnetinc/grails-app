package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.validation.Errors
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials
import uk.co.wonderlane.wlpos.dataaccess.MySqlDal
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.LoyaltyOfferStatus
import uk.co.wonderlane.wlpos.enums.MemberOfferStatus
import uk.co.wonderlane.wlpos.enums.SegmentStatus
import uk.co.wonderlane.wlpos.enums.SegmentType
import uk.co.wonderlane.wlpos.enums.SyncMessageType
import uk.co.wonderlane.wlpos.loyalty.MemberOffer
import uk.co.wonderlane.wlpos.loyalty.Offer
import uk.co.wonderlane.wlpos.loyalty.OfferSegment

import javax.xml.bind.ValidationException
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

@Transactional("loyalty")
class LoyaltyService extends MySqlDal {

    def springSecurityService
    def messageSource
    def rabbitService

    LoyaltyService(DatabaseCredentials databaseCredentials) {
        super(databaseCredentials)
    }

    def getSegment(String searchTerm, String searchBy, String segmentStatus, int max, int offset, String sortColumn, String sortOrder) {
        def conn = getConnection()
        def getSegmentsStatement = conn.prepareCall("{ call getSegments(?, ?, ?, ?, ?, ?, ?, ?, ?) }")

        def segments = []
        int totalCount = 0

        try {
            getSegmentsStatement.setInt(1, springSecurityService.principal.retailerId)
            getSegmentsStatement.setString(2, searchTerm)
            getSegmentsStatement.setString(3, searchBy)
            getSegmentsStatement.setString(4, segmentStatus)
            getSegmentsStatement.setString(5, sortColumn)
            getSegmentsStatement.setString(6, sortOrder)
            getSegmentsStatement.setInt(7, max)
            getSegmentsStatement.setInt(8, offset)
            getSegmentsStatement.registerOutParameter(9, java.sql.Types.INTEGER)

            def resultSet = getSegmentsStatement.executeQuery()

            try {
                while (resultSet.next()) {
                    segments.add(new Segment(
                        resultSet.getInt("id"),
                        resultSet.getInt("retailer_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        SegmentType.valueOf(resultSet.getString("type")),
                        resultSet.getInt("min"),
                        resultSet.getInt("max"),
                        resultSet.getLong("count") as int,
                        SegmentStatus.valueOf(resultSet.getString("status"))
                    ))
                }

                totalCount = getSegmentsStatement.getInt(9);
            } finally {
                resultSet.close()
            }
        } finally {
            getSegmentsStatement.close()
            conn.close();
        }

        return [totalCount: totalCount, segments: segments]
    }

    List<Segment> getLoyaltySegmentForRetailer(int retailerId){
        return Segment.findAllByRetailerId(retailerId)
    }

    List<Segment> getLoyaltySegmentForRetailer(int retailerId, SegmentStatus status){
        return Segment.findAllByRetailerIdAndStatus(retailerId, status)
    }

    def getSegmentById(Integer id) {
        return Segment.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def checkIfSegmentExists(int id, String name) {
        def nameExists = true

        if (Segment.findByRetailerIdAndNameAndIdNotEqual(springSecurityService.principal.retailerId, name, id) == null) {
            nameExists = false
        }

        return nameExists
    }

    def saveSegment(Segment segment) {
        if (segment.validate()) {
            segment.save(flush: true)
        }
    }

    def getLoyaltyOffers(String searchTerm, String searchBy, int max, int offset, String sortColumn, String sortOrder){
        def offers = Offer.createCriteria().list([offset: offset, max: max]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            or {
                if (searchBy == 'Description') {
                    ilike("offerDescription", "%$searchTerm%")
                } else if (searchBy == 'ID') {
                    sqlRestriction "cast(id AS char(256)) like '%$searchTerm%'"
                }
            }
            if(sortColumn == 'remainingBudget'){
                sqlRestriction "1=1 order by (max_budget - current_budget) ${sortOrder == 'asc' ? 'asc' : 'desc'}"
            } else {
                order(sortColumn, sortOrder == 'asc' ? 'asc' : 'desc')
            }
        }

        int totalCount = Offer.withTransaction { offers.totalCount }

        return [totalCount: totalCount, offers: offers]
    }

    def getLoyaltyOfferById(int id){
        return Offer.findById(id)
    }

    def getLoyaltyOffersByOfferId(int id){
        def activeOffers = Offer.findAllByRetailerOfferIdAndStatus(id, "ACTIVE")
        def openOffers = Offer.findAllByRetailerOfferIdAndStatus(id, "OPEN")
        def pendingOffers = Offer.findAllByRetailerOfferIdAndStatus(id, "PENDING")
        return activeOffers + pendingOffers + openOffers
    }

    def updateLoyaltyOfferStatus(int promotionId, int retailerId) {
        /* Set any offers associated with this promotion id and retailer id to inactive */
        Offer.withTransaction {
            def offers = Offer.findAllByRetailerOfferIdAndRetailerIdAndStatusInList(promotionId, retailerId, [LoyaltyOfferStatus.ACTIVE, LoyaltyOfferStatus.PENDING, LoyaltyOfferStatus.OPEN])

            offers.each { offer ->
                offer.status = LoyaltyOfferStatus.INACTIVE
                offer.save(flush: true)

                def memberOffers = MemberOffer.findAllByOfferAndStatusInList(offer, [MemberOfferStatus.ACTIVE, MemberOfferStatus.OPEN])
                memberOffers.each { memberOffer ->
                    memberOffer.status = MemberOfferStatus.CLOSED
                    memberOffer.dateModified = DateTime.now(DateTimeZone.UTC)

                    if (memberOffer.validate()) {
                        memberOffer.save(flush: true)
                    }
                }
            }
        }
    }

    List<OfferSegment> getLoyaltyOfferSegmentsById(int offerId){
        return OfferSegment.withCriteria {
            eq ("offerId", offerId)
        }
    }

    protected Offer populateUpdatedOffer(Offer originalLoyaltyOffer, LoyaltyOfferCommand loyaltyOfferCommand) {
        originalLoyaltyOffer.id = loyaltyOfferCommand.id
        originalLoyaltyOffer.offerDescription = loyaltyOfferCommand.offerDescription
        originalLoyaltyOffer.marketingText = loyaltyOfferCommand.offerMarketingText
        originalLoyaltyOffer.termsText = loyaltyOfferCommand.offerTermsText
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
                    OfferSegment loyaltyOfferSegment = new OfferSegment()
                    loyaltyOfferSegment.id = offerSegment.id
                    loyaltyOfferSegment.offerId = offerSegment.offerId
                    loyaltyOfferSegment.segmentId = offerSegment.segmentId
                    loyaltyOfferSegment.loyaltyOffer = originalLoyaltyOffer
                    originalLoyaltyOffer.addToLoyaltyOfferSegments(loyaltyOfferSegment)

                }
        }
        return originalLoyaltyOffer
    }

    protected List<OfferSegment> updateLoyaltySegments(List<OfferSegment> originalLoyaltyOfferSegments, List<OfferSegment> updatedLoyaltyOfferSegments){
        List<OfferSegment> loyaltyOfferSegments = new ArrayList<>()

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

    def getEligibleOfferStatus(){
        return Arrays.asList(LoyaltyOfferStatus.values());
    }

    @Transactional("loyalty")
    def loyaltyOfferSave(Offer updatedOffer, List<OfferSegment> loyaltyOfferSegmentList){
        try {
            Offer insertedOffer = saveLoyaltyOffer(updatedOffer)
            saveLoyaltySegments(loyaltyOfferSegmentList, insertedOffer)
            pushLoyaltyOfferIntoRabbitMQ(updatedOffer)
        }catch(Exception ex){
            log.error("Error at saving loyalty offer and loyalty offer segments, Exception " + ex)
            throw ex
        }
    }

    private pushLoyaltyOfferIntoRabbitMQ(Offer updatedOffer){
        try {
            SyncMessage loyaltyOfferSyncMessage = new SyncMessage(SyncMessageType.LOYALTY_OFFER, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, null)
            loyaltyOfferSyncMessage.setInsert(true)
            loyaltyOfferSyncMessage.setLoyaltyOffer(updatedOffer.getLoyaltyOffer())
            rabbitService.sendOfferAllocationMessage("DataSync", loyaltyOfferSyncMessage)
        }catch(Exception ex){
            log.error("Error at pushing updated loyalty offer into rabbitMQ, Exception " + ex)
            throw ex
        }
    }

    private Offer saveLoyaltyOffer(Offer updatedOffer){
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

    private saveLoyaltySegments(List<OfferSegment> loyaltyOfferSegmentList, Offer insertedOffer){
        try {
            for (OfferSegment loyaltyOfferSegment : loyaltyOfferSegmentList){
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

    def updatedLoyaltyOfferCustomerCount(Integer offerId){
        try {
            Offer loyaltyOffer = getLoyaltyOfferById(offerId)
            loyaltyOffer.setCurrentCustomers(loyaltyOffer.getCurrentCustomers() + 1)
            saveLoyaltyOffer(loyaltyOffer)
        } catch (Exception ex) {
            log.error("Failed to update current customer count in loyalty offer table,  Exception " + ex)
            throw new RuntimeException("Failed to update current customer count in loyalty offer table,  Exception " + ex.getMessage())
        }
    }
}
