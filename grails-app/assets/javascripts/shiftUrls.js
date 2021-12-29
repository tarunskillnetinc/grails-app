var ShiftUrls = ShiftUrls || (function () {
    var _getShiftsUrl;
    var _getCashDetailsUrl;
    var _changeCashUpTypeUrl;
    var _saveCashUrl;
    var _saveShiftUrl;

    return {
        init : function (getShiftsUrl, getCashDetailsUrl, changeCashUpTypeUrl, saveCashUrl, saveShiftUrl) {
            _getShiftsUrl = getShiftsUrl;
            _getCashDetailsUrl = getCashDetailsUrl;
            _changeCashUpTypeUrl = changeCashUpTypeUrl;
            _saveCashUrl = saveCashUrl;
            _saveShiftUrl = saveShiftUrl;
        },
        getShiftsUrl : function () {
            return _getShiftsUrl;
        },
        getCashDetailsUrl : function () {
            return _getCashDetailsUrl;
        },
        changeCashUpTypeUrl : function() {
            return _changeCashUpTypeUrl;
        },
        saveCashUrl : function() {
            return _saveCashUrl;
        },
        saveShiftUrl : function() {
            return _saveShiftUrl;
        }
    }
} ());