package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import org.springframework.validation.Errors

@Transactional
class SafeService {

    def springSecurityService
    def messageSource

    def serviceMethod() {}

    List<Safe> getSafesByRetailerAndStore(Integer retailerId, Integer storeId){
        return Safe.withCriteria {
            eq ("retailerId", retailerId)
            eq ("storeId", storeId)
        }
    }

    def getSafeById(Integer id){
        return Safe.findById(id)
    }

    @Transactional
    def saveSafe(Safe safe){
        safe.save(flush: true)
    }

    boolean isPrimaryExists(Integer retailerId, Integer storeId){
        return Safe.findByRetailerIdAndStoreIdAndPrimaryAndActive(retailerId, storeId, true, true) != null
    }

    def updatePrimarySafe(Integer selectedSafeId){
        int updatedCount = updatePrimaryToFalse(springSecurityService.principal.retailerId, springSecurityService.principal.storeId)
        if (updatedCount > 0) {
            updateNewSafePrimary(selectedSafeId)
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
           if (!isPrimaryExists(springSecurityService.principal.retailerId,springSecurityService.principal.storeId)) {
               safe.primary = true
           }
       }
       return safe
    }

    private updateNewSafePrimary(Integer selectedSafeId){
        Safe safe = getSafeById(selectedSafeId)
        safe.primary = true
        safe.dateModified = new DateTime()
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

    List<String> extractErrorMessages(Errors errors) {
        Locale locale = new Locale("en","GB");
        List<String> errorMessages = []
        errors.allErrors.each { error ->
            String text = messageSource.getMessage(error, locale)
            errorMessages << text
        }
        return errorMessages
    }

    List<Safe> findSearchSafes(String searchTerm, boolean  inactiveSafes, Integer max, Integer offset, String sortColumn, String sortOrder){
        return Safe.createCriteria().list(max: max, offset: offset) {
            like ("description", "%$searchTerm%")

            if (inactiveSafes) {
                or {
                    eq("active", false)
                }
            } else{
                or {
                    eq("active", true)
                }
            }
            order(sortColumn ?: "offerDescription", sortOrder ?: "asc")
        }
    }
}
