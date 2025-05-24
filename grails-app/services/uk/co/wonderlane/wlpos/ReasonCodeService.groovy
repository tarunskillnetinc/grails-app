package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import grails.util.Pair
import uk.co.wonderlane.wlpos.enums.ReasonCodeType

@Transactional
class ReasonCodeService {

    void saveReasonCode(ReasonCode rc) {
        rc.save()
    }

    Pair<Integer, List<ReasonCode>> getReasonCodesOfTypeIncludingDeleted(int retailerId, ReasonCodeType type) {
        int count = ReasonCode.countByRetailerIdAndType(retailerId, type)
        if (count == 0) {
            return new Pair<Integer, List<ReasonCode>>(0, new ArrayList<ReasonCode>())
        }
        def result = ReasonCode.findAllByRetailerIdAndType(retailerId, type, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
        return new Pair<Integer, List<ReasonCode>>(count, result != null ? result : new ArrayList<ReasonCode>())
    }

    Pair<Integer, List<ReasonCode>> getReasonCodesOfType(int retailerId, ReasonCodeType type) {
        int count = ReasonCode.countByRetailerIdAndTypeAndDeleted(retailerId, type, false)
        if (count == 0) {
            return new Pair<Integer, List<ReasonCode>>(0, new ArrayList<ReasonCode>())
        }
        def result = ReasonCode.findAllByRetailerIdAndTypeAndDeleted(retailerId, type, false, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
        return new Pair<Integer, List<ReasonCode>>(count, result != null ? result : new ArrayList<ReasonCode>())
    }

    List<ReasonCode> getReasonCodesByType(int retailerId, ReasonCodeType type) {
         return ReasonCode.findAllByRetailerIdAndTypeAndDeleted(retailerId, type, false, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
    }

    List<ReasonCode> findReasonCodesByCodes(int retailerId, List<String> code) {
        return ReasonCode.findAllByRetailerIdAndCodeInList(retailerId, code, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
    }

    ReasonCode findByCode(int retailerId, String code, boolean additionalFunctionality, int id) {
        return ReasonCode.findByRetailerIdAndCodeAndAdditionalFunctionalityAndIdNotEqual(retailerId, code, additionalFunctionality, id, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
    }

    ReasonCode findByTypeAndCode(int retailerId, ReasonCodeType type, String code) {
        return ReasonCode.findByRetailerIdAndTypeAndCode(retailerId, type, code, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
    }

    List<ReasonCode> findReasonCodesByIds(List<Integer> id) {
        return ReasonCode.findAllByIdInList(id)
    }

    boolean isLastOfType(int retailerId, ReasonCodeType type) {
        return ReasonCode.countByRetailerIdAndTypeAndDeleted(retailerId, type, false) <= 1
    }

    boolean isDuplicateSecret(int retailerId, String secret) {
        return ReasonCode.countByRetailerIdAndSecret(retailerId, secret) > 0
    }

    List<ReasonCode> getReasonCodesByRetailer(int retailerId) {
        return ReasonCode.findAllByRetailerIdAndDeleted(retailerId, false, [offset: 0, max: 9999, sort: [priority: 'asc', id: 'asc']])
    }
}
