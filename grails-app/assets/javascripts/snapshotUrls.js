var SnapshotUrls = SnapshotUrls || (function () {
    let _getSafeUrl;
    let _getSnapshotsUrl;
    let _getSnapshotUrl;
    let _saveSafeCountUrl;
    let _saveSnapshotUrl;
    let _cashLiftUrl;
    let _saveCashLiftUrl;
    let _bankingUrl;
    let _saveBankingUrl;
    let _cashInboundUrl;
    let _saveCashInboundUrl;

    return {
        init : function (getSafeUrl, getSnapshotsUrl, getSnapshotUrl, saveSafeCountUrl, saveSnapshotUrl, cashLiftUrl, saveCashLiftUrl, bankingUrl, saveBankingUrl, cashInboundUrl, saveCashInboundUrl) {
            _getSafeUrl = getSafeUrl;
            _getSnapshotsUrl = getSnapshotsUrl;
            _getSnapshotUrl = getSnapshotUrl;
            _saveSafeCountUrl = saveSafeCountUrl;
            _saveSnapshotUrl = saveSnapshotUrl;
            _cashLiftUrl = cashLiftUrl;
            _saveCashLiftUrl = saveCashLiftUrl;
            _bankingUrl = bankingUrl;
            _saveBankingUrl = saveBankingUrl;
            _cashInboundUrl = cashInboundUrl;
            _saveCashInboundUrl = saveCashInboundUrl;
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
        cashLiftUrl : function () {
            return _cashLiftUrl;
        },
        saveCashLiftUrl : function () {
            return _saveCashLiftUrl;
        },
        bankingUrl : function() {
            return _bankingUrl;
        },
        saveBankingUrl : function() {
            return _saveBankingUrl;
        },
        cashInboundUrl : function() {
            return _cashInboundUrl;
        },
        saveCashInboundUrl : function() {
            return _saveCashInboundUrl;
        }
    }
} ());