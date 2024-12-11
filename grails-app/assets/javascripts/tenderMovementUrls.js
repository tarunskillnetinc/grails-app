var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processIssueFloat;
    var _getSafeAvailableBalance;

    return {
        init : function (processTenderLift, getTillAvailableBalance, processIssueFloat, getSafeAvailableBalance, processPayIn, processPayOut) {
            _processTenderLift = processTenderLift;
            _processIssueFloat = processIssueFloat;
            _processPayIn = processPayIn;
            _processPayOut = processPayOut;
            _getTillAvailableBalance = getTillAvailableBalance;
            _getSafeAvailableBalance = getSafeAvailableBalance;
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
        }
    }
} ());