package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.LocationType
import uk.co.wonderlane.wlpos.reporting.Location

@Transactional("reporting")
class LocationService {

    def springSecurityService

    def getStoreSafeLocations() {
        def locationCriteria = Location.createCriteria()
        return locationCriteria.list {
            eq ("retailerId", springSecurityService.principal.retailerId)
            if (springSecurityService.principal.storeId != null) {
                eq ("storeId", springSecurityService.principal.storeId)
            }
            eq ("type", LocationType.SAFE)
        }
    }

    def getTillLocation(int tillId) {
        def locationCriteria = Location.createCriteria()
        return locationCriteria.get {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("storeId", springSecurityService.principal.storeId)
            eq ("tillId", tillId)
            eq ("type", LocationType.TILL)
        }
    }

    def getLocation(int locationId) {
        return Location.createCriteria().get {
            eq ("id", locationId)
        }
    }

    def saveLocation(Location location) {

    }
}
