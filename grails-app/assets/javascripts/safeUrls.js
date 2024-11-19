var SafeUrls = SafeUrls || (function () {
    var _getSearchSafeUrl;
    var _getUpdatePrimarySafeUrl;

    return {
        init : function (getSearchSafeUrl, getUpdatePrimarySafeUrl) {
            _getSearchSafeUrl = getSearchSafeUrl;
            _getUpdatePrimarySafeUrl = getUpdatePrimarySafeUrl;
        },
        getSearchSafeUrl : function () {
            return _getSearchSafeUrl;
        },
        getUpdatePrimarySafeUrl : function () {
            return _getUpdatePrimarySafeUrl;
        }
    }
} ());