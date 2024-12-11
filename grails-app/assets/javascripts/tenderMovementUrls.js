var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processIssueFloat;
    var _getSafeAvailableBalance;
    var _processPayIn;
    var _processPayOut;
    var _processBankDeposit;
    var _processBankReceipt;

    return {
        init : function (processTenderLift, getTillAvailableBalance, processIssueFloat, getSafeAvailableBalance, processPayIn, processPayOut, processBankDeposit, processBankReceipt) {
            _processTenderLift = processTenderLift;
            _processIssueFloat = processIssueFloat;
            _processPayIn = processPayIn;
            _processPayOut = processPayOut;
            _getTillAvailableBalance = getTillAvailableBalance;
            _getSafeAvailableBalance = getSafeAvailableBalance;
            _processBankDeposit = processBankDeposit;
            _processBankReceipt = processBankReceipt;
        },

        getProcessTenderLift : function () {
            return _processTenderLift;
        },

        getTillAvailableBalance : function () {
            return _getTillAvailableBalance;
        },

        getProcessPayIn : function() {
            return _processPayIn;
        },

        getProcessIssueFloat : function () {
            return _processIssueFloat;
        },

        getSafeAvailableBalance : function () {
            return _getSafeAvailableBalance;
        },

        getProcessPayOut: function () {
            return _processPayOut;
        },

        getProcessBankDeposit: function () {
            return _processBankDeposit;
        },

        getProcessBankReceipt: function () {
            return _processBankReceipt;
        }
    }
} ());