var SafeManagementUrls = SafeManagementUrls || (function () {
    var _getSafeSessionUrl;

    return {
        init : function (getSafeSessionUrl) {
            _getSafeSessionUrl = getSafeSessionUrl;
        },

        getSafeSessionUrl : function () {
            return _getSafeSessionUrl;
        }
    }
} ());