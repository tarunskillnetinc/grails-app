var SnapshotUrls = SnapshotUrls || (function () {
    var _safeSelectionUrl;
    var _getSafeUrl;
    var _getSnapshotsUrl;
    var _getSnapshotUrl;
    var _saveSafeCountUrl;
    var _saveSnapshotUrl;
    var _startCashLiftUrl;
    var _saveCashLiftUrl;

    return {
        init : function (safeSelectionUrl, getSafeUrl, getSnapshotsUrl, getSnapshotUrl, saveSafeCountUrl, saveSnapshotUrl, startCashLiftUrl, saveCashLiftUrl) {
            _safeSelectionUrl = safeSelectionUrl;
            _getSafeUrl = getSafeUrl;
            _getSnapshotsUrl = getSnapshotsUrl;
            _getSnapshotUrl = getSnapshotUrl;
            _saveSafeCountUrl = saveSafeCountUrl;
            _saveSnapshotUrl = saveSnapshotUrl;
            _startCashLiftUrl = startCashLiftUrl;
            _saveCashLiftUrl = saveCashLiftUrl;
        },
        safeSelectionUrl : function () {
            return _safeSelectionUrl;
        },
        getSafeUrl : function () {
            return _getSafeUrl;
        },
        getSnapshotsUrl : function () {
            return _getSnapshotsUrl;
        },
        getSnapshotUrl : function () {
            return _getSnapshotUrl;
        },
        saveSafeCountUrl : function () {
            return _saveSafeCountUrl;
        },
        saveSnapshotUrl : function () {
            return _saveSnapshotUrl;
        },
        startCashLiftUrl : function () {
            return _startCashLiftUrl;
        },
        saveCashLiftUrl : function () {
            return _saveCashLiftUrl;
        }
    }
} ());