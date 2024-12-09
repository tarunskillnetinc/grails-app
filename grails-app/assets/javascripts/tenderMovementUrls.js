var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processIssueFloat;

    return {
        init : function (processTenderLift, getTillAvailableBalance, processIssueFloat) {
            _processTenderLift = processTenderLift;
            _getTillAvailableBalance = getTillAvailableBalance;
            _processIssueFloat = processIssueFloat;
        },

        getProcessTenderLift : function () {
            return _processTenderLift;
        },

        getTillAvailableBalance : function () {
            return _getTillAvailableBalance;
        },

        getProcessIssueFloat : function () {
            return _processIssueFloat;
        }
    }
} ());