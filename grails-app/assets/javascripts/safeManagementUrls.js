var SafeManagementUrls = SafeManagementUrls || (function () {
    var _safeSessionUrl;
    var _cashUpModalUrl;
    var _updateReconcileDataUrl;
    var _saveSafeSessionCashUrl;
    var _spotCheckUrl;

    return {
        init : function (safeSessionUrl, cashUpModalUrl, updateReconcileDataUrl, saveSafeSessionCashUrl, spotCheckUrl) {
            _safeSessionUrl = safeSessionUrl;
            _cashUpModalUrl = cashUpModalUrl;
            _updateReconcileDataUrl = updateReconcileDataUrl;
            _saveSafeSessionCashUrl = saveSafeSessionCashUrl;
            _spotCheckUrl = spotCheckUrl;
        },

        getSafeSessionUrl : function () {
            return _safeSessionUrl;
        },

        getSafeSessionCashUpUrl : function () {
            return _cashUpModalUrl;
        },

        getUpdateSafeSessionReconcileUrl : function() {
            return _updateReconcileDataUrl;
        },

        getSafeSessionSaveUrl : function() {
            return _saveSafeSessionCashUrl;
        },

        spotCheckUrl: function () {
            return _spotCheckUrl;
        }
    }
} ());