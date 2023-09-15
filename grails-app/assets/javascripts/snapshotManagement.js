function getSnapshots() {
    $("#search-results").hide();
    $("#loading-indicator").show();

    const startDate = $("#startDate").val();
    const endDate = $("#endDate").val();
    const tillId = $("#tillId").val();

    $.ajax({
        url: SnapshotUrls.getSnapshotsUrl(),
        method: "POST",
        data: { startDate: startDate, endDate: endDate, tillId: tillId },
        success: function(resp) {
            $("#results-container").html(resp);
        },
        error: function() {
            $("#loading-indicator").hide();
            $("#search-results").show();

            const result = document.createElement('div');
            $(result).addClass('col pt-2 pb-2 text-center my-auto wl-striped0')
                .html('No snapshots found.');
            $("#search-results").html(result);
        }
    });
}

function resetSnapshotFilters(startDate, endDate) {
    setDatePickers(
        'startDate',
        'endDate',
        startDate,
        endDate
    );
    $("#tillId").val("");
    getSnapshots();
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

