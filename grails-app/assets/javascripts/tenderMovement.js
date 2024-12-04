var TenderMovementUrls = TenderMovementUrls || (function () {
    var _processTenderLift;

    return {
        init : function (processTenderLift) {
            _processTenderLift = processTenderLift;
        },

        getProcessTenderLift : function () {
            return _processTenderLift;
        }
    }
} ());