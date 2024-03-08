package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import grails.util.Pair
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

@Transactional
class ReasonCodeService {

    void saveReasonCode(ReasonCode rc) {
        rc.save()
    }

    Pair<Integer, List<ReasonCode>> getReasonCodesOfType(int retailerId, ReasonCodeType type, int offset, int max, String order) {
        int count = ReasonCode.countByRetailerIdAndTypeAndDeleted(retailerId, type, false)
        if (count == 0) {
            return new Pair<Integer, List<ReasonCode>>(0, new ArrayList<ReasonCode>())
        }
        def result = ReasonCode.findAllByRetailerIdAndTypeAndDeleted(retailerId, type, false, [offset: offset, max: max, sort: "description", order: order])
        return new Pair<Integer, List<ReasonCode>>(count, result != null ? result : new ArrayList<ReasonCode>())
    }

    ReasonCode findByCode(int retailerId, String code) {
        return ReasonCode.findByRetailerIdAndCode(retailerId, code)
    }
    boolean isLastOfType(int retailerId, ReasonCodeType type) {
        return ReasonCode.countByRetailerIdAndTypeAndDeleted(retailerId, type, false) <= 1
    }

    boolean isDuplicateSecret(int retailerId, String secret) {
        return ReasonCode.countByRetailerIdAndSecret(retailerId, secret) > 0
    }
}
