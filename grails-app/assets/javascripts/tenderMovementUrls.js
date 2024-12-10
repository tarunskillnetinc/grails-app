var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;
    var _getTillAvailableBalance;
    var _processPayOut

    return {
        init : function (processTenderLift, getTillAvailableBalance, processPayOut) {
            _processTenderLift = processTenderLift;
            _getTillAvailableBalance = getTillAvailableBalance;
            _processPayOut = processPayOut;
        },

        getProcessTenderLift : function () {
            return _processTenderLift;
        },

        getTillAvailableBalance : function () {
            return _getTillAvailableBalance;
        },

        getProcessPayOut: function () {
            return _processPayOut;
        }
    }
} ());