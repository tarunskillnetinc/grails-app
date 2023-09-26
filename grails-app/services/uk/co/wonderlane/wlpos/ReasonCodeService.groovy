package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

@Transactional
class ReasonCodeService {

    def saveReasonCode(ReasonCode rc) {
        rc.save()
    }

    def deleteReasonCode(ReasonCode rc) {
        rc.delete()
    }

    def getReasonCode(int id) {
        return ReasonCode.findById(id)
    }

    def getAllReasonCodesOfType(int retailerId, ReasonCodeType type) {
        def result = ReasonCode.findAllByRetailerIdAndType(retailerId, type)
        return result != null ? result : new ArrayList<ReasonCode>()
    }
}
