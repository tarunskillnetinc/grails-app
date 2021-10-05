package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.wlim.ProductListType

@Transactional
class TagService {

    def springSecurityService

    def getTags(String searchTerm = null, int offset = 0, int max = 50, String sort = "description", String order = "ASC") {
        return Tag.createCriteria().list([offset: offset, max: max, sort: sort, order: order]) {
            eq ("retailerId", springSecurityService.principal.retailerId)
            eq ("hidden", false)

            if (searchTerm) {
                like ("description", "%$searchTerm%")
            }
        }
    }

    def getTag(int id) {
        return Tag.findByIdAndRetailerId(id, springSecurityService.principal.retailerId)
    }

    def saveTag(Tag tag) {
        tag.save()
    }
}
