function getSnapshots() {
    $("#search-results").hide();
    $("#loading-indicator").show();

    var startDate = $("#startDate").val();
    var endDate = $("#endDate").val();
    var tillId = $("#tillId").val();

    $.ajax({
        url: SnapshotUrls.getSnapshotsUrl(),
        method: "POST",
        data: { startDate: startDate, endDate: endDate, tillId: tillId },
        success: function(resp) {
            $("#results-container").html(resp);
        }
    });
}

function showSnapshotModal(snapshotId) {
    $("#snapshotModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#snapshotModal').modal({ show: true });

    $.ajax({
        url: SnapshotUrls.getSnapshotUrl(),
        method: "POST",
        data: { snapshotId: snapshotId },
        success: function(resp) {
            $("#snapshotModalContent").html(resp);

            $(".mask-money").maskMoney({ allowZero: true });
            $(".mask-money").maskMoney('mask');
        }
    });
}

