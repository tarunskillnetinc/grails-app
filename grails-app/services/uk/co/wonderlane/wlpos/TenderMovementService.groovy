package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType

import java.util.stream.Collectors

@Transactional
class TenderMovementService {

    def springSecurityService
    def storeService
    def reportingService
    def locationService
    def safeManagementService

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
        return Arrays.stream(TenderType.values()).filter(type -> type == TenderType.CASH || type == TenderType.VOUCHER)
                .collect(Collectors.toList());
    }

    void updateTenderLiftShiftTotals(Shift shift, TenderType tenderType, BigDecimal updateAmount){
        BigDecimal adjustedCashAmount = updateAmount.negate()
        updateShiftBalance(shift, tenderType, adjustedCashAmount)
    }

   void tenderMovementUpdate(int tillId, int safeId, TenderMovementType tenderMovementType, TenderType tenderType, BigDecimal adjustAmount){
        uk.co.wonderlane.wlpos.reporting.Location tillLocation = locationService.getTillLocation(tillId) as uk.co.wonderlane.wlpos.reporting.Location
        uk.co.wonderlane.wlpos.reporting.Location safeLocation = locationService.getOrCreateLocationForSafe(safeId) as uk.co.wonderlane.wlpos.reporting.Location
        createNewTenderMovement(safeLocation, tillLocation, tenderMovementType, tenderType, adjustAmount)
    }

    // This method can generally use for shift balance update
    private void updateShiftBalance(Shift shift, TenderType tenderType, BigDecimal updateAmount){
        updateShiftTenderTotals(shift, tenderType, updateAmount)
        if (tenderType.equals(TenderType.CASH)){
            updateCashDrawer(shift, updateAmount)
        }
    }

    // Generic method for shift's tender total update
    private void updateShiftTenderTotals(Shift shift, TenderType tenderType, BigDecimal updateAmount){
        if (updateAmount != 0) { //update amount either can be negative or positive
            TenderTotal tenderTotal = shift.getTenderTotals().stream()
                    .filter(tt -> tt.getTenderType() == tenderType).findFirst()
                    .orElseGet(() -> {
                        TenderTotal newTenderTotal = new TenderTotal(tenderType);
                        shift.getTenderTotals().add(newTenderTotal);
                        return newTenderTotal;
                    });

            tenderTotal.setQuantity(tenderTotal.getQuantity() + 1);
            tenderTotal.setValue(tenderTotal.getValue().add(updateAmount));
        }
    }

    // Generic method for shift's cash drawer update
    private void updateCashDrawer(Shift shift, BigDecimal cashAmount){
        if (cashAmount != null){
            BigDecimal currentCash = shift.getCashInDrawer();
            if (currentCash == null) {
                currentCash = BigDecimal.ZERO;
            }
            BigDecimal newCashAmount = currentCash.add(cashAmount);
            shift.setCashInDrawer(newCashAmount);
        }
    }

    // Method to create new tender movement
    private void createNewTenderMovement(uk.co.wonderlane.wlpos.reporting.Location tillLocation, uk.co.wonderlane.wlpos.reporting.Location safeLocation, TenderMovementType tenderMovementType, TenderType tenderType, BigDecimal updateAmount){
        if (updateAmount.compareTo(BigDecimal.ZERO) != 0) {
            reportingService.saveTenderMovement(reportingService.createNewTenderMovement(tenderMovementType,
                    tenderType,
                    tillLocation as uk.co.wonderlane.wlpos.reporting.Location,
                    safeLocation as uk.co.wonderlane.wlpos.reporting.Location,
                    updateAmount))
        }
    }

}
