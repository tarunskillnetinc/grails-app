var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;

    return {
        init : function (processTenderLift, getTillAvailableBalance) {
            _processTenderLift = processTenderLift;
            _getTillAvailableBalance = getTillAvailableBalance;
        },

        getProcessTenderLift : function () {
            return _processTenderLift;
        },

        getTillAvailableBalance : function () {
            return _getTillAvailableBalance;
        }
    }
} ());