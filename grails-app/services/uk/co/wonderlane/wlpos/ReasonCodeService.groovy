package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import grails.util.Pair
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

@Transactional
class ReasonCodeService {

    def saveReasonCode(ReasonCode rc) {
        rc.save()
    }

    def deleteReasonCode(ReasonCode rc) {
        rc.delete()
    }

    def getReasonCodesOfType(int retailerId, ReasonCodeType type, int offset, int max) {
        int count = ReasonCode.countByRetailerIdAndTypeAndDeleted(retailerId, type, false)
        if (count == 0) {
            return new Pair<Integer, List<ReasonCode>>(0, new ArrayList<ReasonCode>())
        }
        def result = ReasonCode.findAllByRetailerIdAndTypeAndDeleted(retailerId, type, false, [offset: offset, max: max])
        return new Pair<Integer, List<ReasonCode>>(count, result != null ? result : new ArrayList<ReasonCode>())
    }

    def isDescAvailable(int retailerId, String desc) {
        List<ReasonCode> result = ReasonCode.findAllByRetailerIdAndDescription(retailerId, desc)
        return result == null || result.size() == 0
    }
}
