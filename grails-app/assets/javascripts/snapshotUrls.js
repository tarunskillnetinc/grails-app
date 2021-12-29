var SnapshotUrls = SnapshotUrls || (function () {
    var _getSafeUrl;
    var _getSnapshotsUrl;
    var _getSnapshotUrl;
    var _saveSafeCountUrl;
    var _saveSnapshotUrl;

    return {
        init : function (getSafeUrl, getSnapshotsUrl, getSnapshotUrl, saveSafeCountUrl, saveSnapshotUrl) {
            _getSafeUrl = getSafeUrl;
            _getSnapshotsUrl = getSnapshotsUrl;
            _getSnapshotUrl = getSnapshotUrl;
            _saveSafeCountUrl = saveSafeCountUrl;
            _saveSnapshotUrl = saveSnapshotUrl;
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
        }
    }
} ());