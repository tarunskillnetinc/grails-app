var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processIssueFloat;
    var _getSafeAvailableBalance;

    return {
        init : function (processTenderLift, getTillAvailableBalance, processIssueFloat, getSafeAvailableBalance) {
            _processTenderLift = processTenderLift;
            _getTillAvailableBalance = getTillAvailableBalance;
            _processIssueFloat = processIssueFloat;
            _getSafeAvailableBalance = getSafeAvailableBalance;
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
        }
    }
} ());