package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.LocationType
import uk.co.wonderlane.wlpos.reporting.Location

@Transactional("reporting")
class LocationService {

    def springSecurityService

    def getTillLocation(int tillId) {
        def locationCriteria = Location.createCriteria()
        return locationCriteria.get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            eq ("tillId", tillId)
            eq ("type", LocationType.TILL)
        }
    }

    def getLocationBySafeId(int safeId) {
        return Location.createCriteria().get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            eq ("safeId", safeId)
            eq ("type", LocationType.SAFE)
        }
    }

    def createSafeLocation(int safeId) {
        def location = new Location()
        location.safeId = safeId
        location.retailerId = springSecurityService.principal.retailerId
        location.storeId = springSecurityService.principal.storeId
        location.type = LocationType.SAFE
        location.description = "Safe 1"
        location.save()
    }

    def updateLocationDescriptionsBySafeId(Integer safeId, Integer retailerId, Integer storeId, String newDescription) {
        def updatedCount = 0

        Location.withTransaction { status ->
            updatedCount = Location.executeUpdate("""
                UPDATE Location l 
                SET l.description = :newDescription 
                WHERE l.safeId = :safeId 
                AND l.retailerId = :retailerId 
                AND l.storeId = :storeId
            """, [
                    newDescription: newDescription,
                    safeId: safeId,
                    retailerId: retailerId,
                    storeId: storeId
            ])
        }
        return updatedCount;
    }
}
