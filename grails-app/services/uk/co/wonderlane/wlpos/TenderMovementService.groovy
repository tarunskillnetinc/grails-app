package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.enums.TenderType

import java.util.stream.Collectors

@Transactional
class TenderMovementService {

    def springSecurityService
    def storeService

    List<TillConfiguration> getAllActiveTills() {
        Integer retailerId =  springSecurityService.principal.retailerId
        Integer storeId = springSecurityService.principal.storeId
        Store store = storeService.getStore(retailerId, storeId)
        Integer storeNumber = store.getConfig().getStoreNumber()
        return TillConfiguration.createCriteria().list {
            and {
                isNotNull('serialNumber')
                ne('serialNumber', '')  // Exclude empty strings
                eq('cashManagementEnabled', true)  // Only return entries with cashManagementEnabled = true
            }
            eq('retailerId', springSecurityService.principal.retailerId)
            if (storeNumber != null) { //In Till configuration table store id means store number
                eq('storeId', storeNumber)
            }

            // Sort by tillId in ascending order
            order('tillId', 'asc')

        } as List<TillConfiguration>
    }

    List<TenderType> getEligibleTendersForTenderLift() {
        return Arrays.stream(TenderType.values())
                .filter(type -> type == TenderType.CASH || type == TenderType.VOUCHER)
                .collect(Collectors.toList());
    }
}
