var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processIssueFloat;
    var _getSafeAvailableBalance;
    var _processPayIn;

    return {
        init : function (processTenderLift, getTillAvailableBalance, processIssueFloat, getSafeAvailableBalance, processPayIn) {
            _processTenderLift = processTenderLift;
            _getTillAvailableBalance = getTillAvailableBalance;
            _processPayIn = processPayIn
            _processIssueFloat = processIssueFloat;
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
        }
    }
} ());