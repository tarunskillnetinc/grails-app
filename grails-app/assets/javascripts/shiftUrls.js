var ShiftUrls = ShiftUrls || (function () {
    var _getShiftsUrl;
    var _getCashDetailsUrl;
    var _changeCashUpTypeUrl;
    var _saveCashUrl;
    var _saveShiftUrl;
    var _openShiftUrl;
    var _closeShiftUrl;

    return {
        init : function (getShiftsUrl, getCashDetailsUrl, changeCashUpTypeUrl, saveCashUrl, saveShiftUrl, openShiftUrl, closeShiftUrl) {
            _getShiftsUrl = getShiftsUrl;
            _getCashDetailsUrl = getCashDetailsUrl;
            _changeCashUpTypeUrl = changeCashUpTypeUrl;
            _saveCashUrl = saveCashUrl;
            _saveShiftUrl = saveShiftUrl;
            _openShiftUrl = openShiftUrl;
            _closeShiftUrl = closeShiftUrl;
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
        },
        openShiftUrl : function() {
            return _openShiftUrl;
        },
        closeShiftUrl : function() {
            return _closeShiftUrl;
        }
    }
} ());