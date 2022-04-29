package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional

@Transactional
class RestrictionsService {

    def saveRestrictions(Restrictions restrictions) {
        restrictions.save()
    }
}