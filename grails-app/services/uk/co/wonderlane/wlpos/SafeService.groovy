package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.springframework.validation.Errors
import uk.co.wonderlane.wlpos.entities.SyncMessage
import uk.co.wonderlane.wlpos.enums.SyncMessageType

@Transactional
class SafeService {

    def springSecurityService
    def messageSource
    def rabbitService
    def locationService
    def storeService

    def serviceMethod() {}

    @Transactional
    def saveSafe(Safe safe){
        safe.save(flush: true)
    }

    @Transactional
    def updatePrimarySafe(Integer selectedSafeId) {
        try {
            int updatedCount = updatePrimaryToFalse(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
            if (updatedCount > 0) {
                boolean updateSuccess = updateNewSafePrimary(selectedSafeId)
                if (!updateSuccess) {
                    throw new RuntimeException("Failed to update new primary safeId: ${selectedSafeId}")
                }
            } else {
                throw new RuntimeException("Failed to update current primary safe to non-primary when trying to update safeId: ${selectedSafeId} to primary")
            }
            return true // Return true if everything succeeded
        } catch (Exception ex) {
            log.error("Unexpected failure updating primary safeId: ${selectedSafeId} with message: ${ex.message}", ex)
            throw ex // Re-throw the exception to trigger a rollback
        }
    }

    def updateLocationDescriptionBySafeId(int safeId, String safeDescription){
        try {
            locationService.updateLocationDescriptionsBySafeId(safeId, springSecurityService.principal.retailerId,
                    springSecurityService.principal.storeId, safeDescription)
        } catch (Exception ex) {
            log.error("Failed to update primary safeId: ${safeId} description: ${safeDescription} with message: ${ex.message}", ex)
        }
    }

    def populateSafe(Safe existingSafe, boolean isUpdate, String safeDescription, String safeType, boolean safeActive){
       Safe safe = existingSafe ? existingSafe : new Safe()
       safe.description = safeDescription
       safe.type = safeType  // Assuming SafeType is an enum or can be cast from String
       safe.active = safeActive  // Convert to boolean
       safe.dateModified = DateTime.now()
       if (!isUpdate) {
           safe.dateCreated = DateTime.now()
           safe.retailerId = springSecurityService.principal.retailerId
           safe.storeId = springSecurityService.principal.storeId
           safe.primary = false
           safe.active = true //If newly created safe make it active
           if (!isPrimaryExists(springSecurityService.principal.retailerId,springSecurityService.principal.storeId)) {
               safe.primary = true
           }
       }
       return safe
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

    def pushAllUpdatedSafesIntoRabbitMQ() {
        try {
            List<Safe> safeList = getStoreSafes()
            long pushedCount = safeList.stream().filter(safe -> safe.active)
                    .peek(this::pushSafeIntoRabbitMQ).count()
            log.info("Successfully pushed all available safes ${pushedCount} into RabbitMQ after primary updated")
        } catch (Exception ex) {
            log.error("Failed to push all available safes into RabbitMQ after primary updated: ${ex.message} ", ex)
        }
    }

    def pushSafeIntoRabbitMQ(Safe safe){
        try {
            SyncMessage safeSyncMessage = new SyncMessage(SyncMessageType.SAFE, springSecurityService.principal.retailerId, springSecurityService.principal.storeNumber, springSecurityService.principal.storeId, null)
            safeSyncMessage.setInsert(true)
            safeSyncMessage.setSafe(safe.getSafe())
            rabbitService.sendMessage(safeSyncMessage)
        }catch(Exception ex){
            log.error("Failed to push updated safe into rabbitMQ, Exception: ${ex.message} " + ex)
        }
    }

    List<Safe> getStoreSafes() {
        return Safe.withCriteria {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", springSecurityService.principal.storeId)
            order("active", "desc")
            order("description")
        }
    }

    List<Safe> getSafesByStoreNumber(int storeNumber) {
        return Safe.withCriteria {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", storeService.getStoreIdByStoreNumber(storeNumber))
            order("active", "desc")
            order("description")
        }
    }

    def createDefaultSafe() {
        /* Creates a default safe for the current store, should only be called if there is no safe for a store, but will not be set as primary if that is not the case */
        def safe = populateSafe(null, false, "Safe 1", "MANUAL", true)
        return saveSafe(safe)
    }

    def getSafeById(Integer id){
        return Safe.findById(id)
    }

    def getSafeDescriptionForId(Integer id){
        if (id != null) {
            Safe safe = getSafeById(id)
            if (safe != null) {
                return safe.description
            }
        }
        return ""
    }

    boolean isPrimaryExists(Integer retailerId, Integer storeId){
        return Safe.findByRetailerIdAndStoreIdAndPrimaryAndActive(retailerId, storeId, true, true) != null
    }

    private boolean updateNewSafePrimary(Integer selectedSafeId) {
        Safe safe = getSafeById(selectedSafeId)
        if (safe) {
            safe.primary = true
            safe.dateModified = new DateTime()
            return safe.save(flush: true) != null
        }
        return false
    }

    private int updatePrimaryToFalse(Integer retailerId, Integer storeId) {
        if (storeId == null || retailerId == null) {
            throw new IllegalArgumentException("Both storeId and retailerId must be provided")
        }

        int updatedCount = Safe.withTransaction { status ->
            Safe.executeUpdate("""
            UPDATE Safe s 
            SET s.primary = false, s.dateModified = :currentDate 
            WHERE s.storeId = :storeId 
            AND s.retailerId = :retailerId 
            AND s.primary = true """, [
                    currentDate: new DateTime(),
                    storeId: storeId,
                    retailerId: retailerId
            ])
        }

        return updatedCount
    }

    Safe getPrimaryStoreSafes() {
        return Safe.createCriteria().get {
            eq("retailerId", springSecurityService.principal.retailerId)
            eq("storeId", springSecurityService.principal.storeId)
            eq("primary", true)
            eq("active", true)
        } as Safe
    }

}
