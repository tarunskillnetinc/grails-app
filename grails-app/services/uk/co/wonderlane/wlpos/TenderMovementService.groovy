package uk.co.wonderlane.wlpos

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import uk.co.wonderlane.wlpos.entities.cash.SafeSession
import uk.co.wonderlane.wlpos.entities.cash.Shift
import uk.co.wonderlane.wlpos.entities.cash.TenderTotal
import uk.co.wonderlane.wlpos.enums.SafeSessionAction
import uk.co.wonderlane.wlpos.enums.ShiftAction
import uk.co.wonderlane.wlpos.enums.ShiftStatus
import uk.co.wonderlane.wlpos.enums.TenderMovementType
import uk.co.wonderlane.wlpos.enums.TenderType
import uk.co.wonderlane.wlpos.reporting.TenderMovement

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.stream.Collectors

@Transactional
class TenderMovementService {

    def springSecurityService
    def storeService
    def reportingService
    def locationService
    def safeManagementService
    def shiftService
    def userService
    def safeService

    private static final MIN_AMOUNT_ADD_FLOAT = new BigDecimal("0.01")
    private static final MAX_AMOUNT_ADD_FLOAT = new BigDecimal("99999.99")

    private static final MIN_AMOUNT_BANK_TRANSFER = new BigDecimal("0.01")
    private static final MAX_AMOUNT_BANK_TRANSFER = new BigDecimal("999999.99")

    List fetchSafeLocations() {
        List<Safe> safeLocations = safeService.getStoreSafes() ?.findAll { it.active }
        Safe primarySafe = safeLocations?.find { it.primary }
        // Place primary safe at the top and sort remaining safes by id
        if (primarySafe) {
            safeLocations = [primarySafe] + (safeLocations - primarySafe)?.sort { it.id }
        } else {
            safeLocations = safeLocations?.sort { it.id }
        }
        return [safeLocations, primarySafe]
    }

    //Return eligible tenders for tender lift and add float (Here it is only CASH and VOUCHER)
    List<TenderType> getEligibleTendersForTenderUpdate() {
        return Arrays.stream(TenderType.values()).filter(type -> type == TenderType.CASH || type == TenderType.VOUCHER)
                .collect(Collectors.toList());
    }

    List<TenderType> getCashOnlyTenders() {
        return Arrays.stream(TenderType.values()).filter(type -> type == TenderType.CASH)
                .collect(Collectors.toList());
    }

    //Return eligible tenders for Pay Out (Here it is only CASH)
    List<TenderType> getEligibleTendersForPayOut() {
        return Arrays.stream(TenderType.values()).filter(type -> type == TenderType.CASH)
                .collect(Collectors.toList());
    }

    List<TenderType> getCashTenders() {
        return Arrays.stream(TenderType.values()).filter(type -> type == TenderType.CASH)
                .collect(Collectors.toList());
    }

    //Update shift values and add a audit for tender lift
    void updateShiftBalanceTotals(ShiftAction shiftAction, TenderType tenderType, BigDecimal updateAmount, Integer tenderMovementId, int tillId){
        Shift shift = shiftService.getOpenShift(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, tillId)
        User loggedInUser = loadLoggedInUser()
        updateShiftBalance(shift, tenderType, updateAmount)
        addShiftAudit(shift, shiftAction, true,  loggedInUser, tenderMovementId)
    }

    //Update safe session values and add a audit for tender lift
    void updateSafeSessionBalanceTotals(SafeSessionAction safeSessionAction, TenderType tenderType, BigDecimal updateAmount, Integer tenderMovementId, int safeId){
        User loggedInUser = loadLoggedInUser()
        SafeSession safeSession =  safeSessionUpdate(safeId, tenderType, updateAmount) //Update safe session
        addSafeSessionAudit(safeSession, safeSessionAction, true,  loggedInUser, tenderMovementId)
    }

    Integer tenderMovementUpdate(int tillId, int safeId, TenderMovementType tenderMovementType, TenderType tenderType, BigDecimal adjustAmount){
        uk.co.wonderlane.wlpos.reporting.Location tillLocation = locationService.getTillLocation(tillId) as uk.co.wonderlane.wlpos.reporting.Location
        uk.co.wonderlane.wlpos.reporting.Location safeLocation = locationService.getOrCreateLocationForSafe(safeId) as uk.co.wonderlane.wlpos.reporting.Location
        return createNewTenderMovement(tillLocation, safeLocation, tenderMovementType, tenderType, adjustAmount)
    }

    Integer tenderMovementUpdate(int safeId, TenderMovementType tenderMovementType, TenderType tenderType, String reasonCode, BigDecimal adjustAmount){
        uk.co.wonderlane.wlpos.reporting.Location safeLocation = locationService.getOrCreateLocationForSafe(safeId) as uk.co.wonderlane.wlpos.reporting.Location
        return createNewTenderMovement(safeLocation, tenderMovementType, tenderType, reasonCode, adjustAmount)
    }

    int tenderMovementUpdate(int safeId, TenderMovementType tenderMovementType, TenderType tenderType,String bankingDate,
                             String bank, String bagReferenceNumber, String comments, BigDecimal adjustAmount) {
        uk.co.wonderlane.wlpos.reporting.Location safe = locationService.getOrCreateLocationForSafe(safeId) as uk.co.wonderlane.wlpos.reporting.Location
        return createNewTenderMovement(safe, tenderMovementType, tenderType, bankingDate, bank, bagReferenceNumber, comments, adjustAmount)
    }

   void addSafeSessionAudit(SafeSession safeSession, SafeSessionAction safeSessionAction, boolean isAutoGenerated,  User loggedInUser, Integer tenderMovementId){
        if (safeSession != null) {
            safeManagementService.addAudit(safeSession, safeSessionAction, isAutoGenerated, loggedInUser, tenderMovementId)
        }
   }

   void addShiftAudit(Shift shift, ShiftAction shiftAction, boolean isAutoGenerated,  User loggedInUser, Integer tenderMovementId){
        if (shift != null) {
            shiftService.addAudit(shift, shiftAction, isAutoGenerated, loggedInUser, tenderMovementId)
        }
   }

    boolean isSafeActive(int safeId){
        Safe safe = safeService.getSafeById(safeId)
        return safe != null && safe.active
    }

    boolean isOpenShiftAvailable(int tillId){
        Shift shift = shiftService.getOpenShift(springSecurityService.principal.retailerId, springSecurityService.principal.storeId, tillId)
        return shift != null
    }

    List<String> preValidateAddFloatRequest(int safeId, List<Integer> tillNos, BigDecimal amount, TenderType tenderType){
        List<String> failureMessages = []
        validateSafeId(safeId, failureMessages)
        validateAddAmount(amount, failureMessages)
        validateSelectedTillIds(tillNos, failureMessages)
        validateTender(tenderType, failureMessages)
        validateSafeStatus(safeId, failureMessages)
        return failureMessages
    }

    List<TillConfiguration> returnAllActiveOpenTills(){
        List<Shift> shiftList = shiftService.getShifts(null) // Load existing active shifts

        List<Shift> openShifts = shiftList.findAll {Shift shift ->
            shift.getShiftStatus() == ShiftStatus.OPEN // Pull back ONLY the shifts that are open.
        }

        List<TillConfiguration> openTills = openShifts.collect { Shift shift ->
            TillConfiguration.findByRetailerIdAndTillId( shift.retailerId, shift.tillId )
        }

        return openTills;
    }

    List<Integer> returnRequestedTillIds(Map params) {
        List<Integer> tillNos = [] // Declare tillNos as a List of Integers
        if (params.tillNos) { // Parse tillNos into list of till nos
            if (params.tillNos instanceof String[]) {
                // Handle the case where tillNos is already a String array
                tillNos = params.tillNos.collect { it.toInteger() }
            } else if (params.tillNos instanceof String) {
                // Parse JSON string into a list of integers
                tillNos = new JsonSlurper().parseText(params.tillNos).collect { it.toInteger() }
            } else if (params.tillNos instanceof Collection) {
                // Convert collection to a list of integers
                tillNos = params.tillNos.collect { it.toInteger() }
            } else {
                // Handle single string value as integer list
                tillNos = [params.tillNos.toInteger()]
            }
        } else if (params.tillNo) {
            // Handle single tillNo as integer
            tillNos = [params.tillNo.toInteger()]
        }
        return tillNos
    }

    SafeSession safeSessionUpdate(int safeId, TenderType tenderType, BigDecimal cashAmount) {
        List<TenderTotal> addedTenderAmounts = new ArrayList<>()
        if (cashAmount != null && cashAmount.compareTo(BigDecimal.ZERO) != 0) {
            TenderTotal tenderTotal = new TenderTotal(tenderType)
            tenderTotal.value = cashAmount
            tenderTotal.quantity = 1
            addedTenderAmounts.add(tenderTotal)
        }
        //This method will check if any active safe session available if not create new one
        //Then will update tender totals
        return safeManagementService.createNewSafeSessionWithTenderTotals(springSecurityService.principal.retailerId, springSecurityService.principal.storeId,
                safeId, addedTenderAmounts as List<TenderTotal>, true)
    }

    List<String> preValidateBankTransferRequest(int safeId, String bankDate, BigDecimal amount, TenderType tenderType){
        List<String> failureMessages = []
        validateSafeId(safeId, failureMessages)
        validateBankTransferAmount(amount, failureMessages)
        validateBankingDate(bankDate, failureMessages)
        validateBankDateFormat(bankDate, failureMessages)
        validateTender(tenderType, failureMessages)
        validateBankTransferTenderType(tenderType, failureMessages)
        validateSafeStatus(safeId, failureMessages)
        return failureMessages
    }


    // This method can generally use for shift balance update
    private void updateShiftBalance(Shift shift, TenderType tenderType, BigDecimal updateAmount){
        updateShiftTenderTotals(shift, tenderType, updateAmount)
        if (tenderType.equals(TenderType.CASH)){
            updateCashDrawer(shift, updateAmount)
        }
        shiftService.saveShift(shift)
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
            BigDecimal currentCash = shift.getCashInDrawer()
            if (currentCash == null) {
                currentCash = BigDecimal.ZERO
            }
            BigDecimal newCashAmount = currentCash.add(cashAmount)
            shift.setCashInDrawer(newCashAmount)
        }
    }

    // Method to create new tender movement
    private Integer createNewTenderMovement(
            uk.co.wonderlane.wlpos.reporting.Location tillLocation,
            uk.co.wonderlane.wlpos.reporting.Location safeLocation,
            TenderMovementType tenderMovementType,
            TenderType tenderType,
            BigDecimal updateAmount) {

        TenderMovement tenderMovement = reportingService.createNewTenderMovement(
                tenderMovementType,
                tenderType,
                tillLocation,
                safeLocation,
                null,  // reasonCode
                null,  // bankingDate
                null,  // bank
                null,  // bankReferenceNumber
                null,  // comments
                updateAmount
        )
        return reportingService.saveTenderMovement(tenderMovement)
    }

    // Method to create new tender movement
    private Integer createNewTenderMovement(
            uk.co.wonderlane.wlpos.reporting.Location safeLocation,
            TenderMovementType tenderMovementType,
            TenderType tenderType,
            String reasonCode,
            BigDecimal updateAmount) {

        TenderMovement tenderMovement = reportingService.createNewTenderMovement(
                tenderMovementType,
                tenderType,
                safeLocation,
                null,  // toLocation
                reasonCode,
                null,  // bankingDate
                null,  // bank
                null,  // bankReferenceNumber
                null,  // comments
                updateAmount
        )
        return reportingService.saveTenderMovement(tenderMovement)
    }

    private Integer createNewTenderMovement(
            uk.co.wonderlane.wlpos.reporting.Location location,
            TenderMovementType tenderMovementType,
            TenderType tenderType,
            String bankingDate,
            String bank,
            String bagReferenceNumber,
            String comments,
            BigDecimal adjustAmount) {

        TenderMovement tenderMovement = reportingService.createNewTenderMovement(
                tenderMovementType,
                tenderType,
                location,
                null,  // toLocation
                null,  // reasonCode
                bankingDate,
                bank,
                bagReferenceNumber,
                comments,
                adjustAmount
        )
        return reportingService.saveTenderMovement(tenderMovement)
    }

    private User loadLoggedInUser(){
        int id = springSecurityService.principal.id
        User loggedInUser = userService.getUser(id)
        return loggedInUser
    }

    private validateSafeId(int safeId, List<String> failureMessages){
        if (safeId <= 0){
            failureMessages.add("Please select a Safe.")
        }
    }

    private validateAddAmount(BigDecimal amount, List<String> failureMessages){
        if (!isAddFloatValidAmount(amount)){
            failureMessages.add("Amount must be between £${MIN_AMOUNT_ADD_FLOAT} and £${MAX_AMOUNT_ADD_FLOAT}.")
        }
    }

    private validateSafeStatus(int safeId, List<String> failureMessages){
        if (!isSafeActive(safeId)) {
            failureMessages.add("Selected safe is not active please try with another")
        }
    }

    private validateSelectedTillIds(List<Integer> tillIds, List<String> failureMessages){
        if (tillIds.isEmpty() || tillIds.size() == 0) {
            failureMessages.add("Please select at least one Till No.")
        }
    }

    private validateTender(TenderType tenderType, List<String> failureMessages){
        if (tenderType == null) {
            failureMessages.add("Please select a Tender.")
        }
    }

    private validateBankingDate(String bankDate, List<String> failureMessages){
        if (bankDate == null){
            failureMessages.add("Banking date cannot be empty.")
        }
    }

    private validateBankDateFormat(String bankDate, List<String> failureMessages){
        if (!isValidDateFormat(bankDate)) {
            failureMessages.add("Invalid date format. Please enter the date in dd/MM/yyyy format (e.g., 31/12/2023).")
        }
    }

    private boolean isAddFloatValidAmount(BigDecimal amount) {
        amount >= MIN_AMOUNT_ADD_FLOAT && amount <= MAX_AMOUNT_ADD_FLOAT
    }

    private validateBankTransferAmount(BigDecimal amount, List<String> failureMessages){
        if (!(amount >= MIN_AMOUNT_BANK_TRANSFER && amount <= MAX_AMOUNT_BANK_TRANSFER)){
            failureMessages.add("Bank deposit amount must be between ${MIN_AMOUNT_BANK_TRANSFER} and ${MAX_AMOUNT_BANK_TRANSFER}.")
        }
    }

    private validateBankTransferTenderType(TenderType tenderType, List<String> failureMessages){
        if (tenderType != TenderType.CASH){
            failureMessages.add("Only cash tender type allowed.")
        }
    }


    boolean isValidDateFormat(String dateStr) {
        // Define the expected date format
        def dateFormat = "dd/MM/yyyy"
        def sdf = new SimpleDateFormat(dateFormat)
        sdf.setLenient(false)  // This will enforce strict date parsing

        // First, check if the string matches the expected pattern
        if (!(dateStr =~ /\d{2}\/\d{2}\/\d{4}/)) {
            return false
        }

        // If the pattern is correct, try to parse the date
        try {
            sdf.parse(dateStr)
            return true
        } catch (ParseException e) {
            return false
        }
    }



}
