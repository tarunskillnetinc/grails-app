var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processIssueFloat;
    var _getSafeAvailableBalance;
    var _processPayOut;

    return {
        init : function (processTenderLift, getTillAvailableBalance, processIssueFloat, getSafeAvailableBalance, processPayOut) {
            _processTenderLift = processTenderLift;
            _getTillAvailableBalance = getTillAvailableBalance;
            _processIssueFloat = processIssueFloat;
            _getSafeAvailableBalance = getSafeAvailableBalance;
            _processPayOut = processPayOut;
        },

        getProcessTenderLift : function () {
            return _processTenderLift;
        },

        getTillAvailableBalance : function () {
            return _getTillAvailableBalance;
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