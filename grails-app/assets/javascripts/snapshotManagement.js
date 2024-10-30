function getSnapshots() {
    $('#alert-container').html("");
    $("#search-results").hide();
    $("#loading-indicator").show();

    const startDate = $("#startDate").val();
    const endDate = $("#endDate").val();
    const safeId = $("#safeId").val();

    $.ajax({
        url: SnapshotUrls.getSnapshotsUrl(),
        method: "POST",
        data: { startDate: startDate, endDate: endDate, safeId: safeId },
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
    $('#safeId').prop('selectedIndex', 0);
    $("#tillId").val("");
    getSnapshots();
}

function showModal(type, id) {
    $("#modal-content").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#snapshotModal').modal({ show: true });

    let requestUrl = ""
    switch (type) {
        case "BANKING":
            requestUrl = SnapshotUrls.bankingUrl()
            break;
        case "SNAPSHOT":
            requestUrl = SnapshotUrls.getSnapshotUrl()
            break;
        case "CASH_INBOUND":
            requestUrl = SnapshotUrls.cashInboundUrl()
            break;
        case "CASH_LIFT":
            requestUrl = SnapshotUrls.cashLiftUrl()
            break;
    }

    $.ajax({
        url: requestUrl,
        method: "POST",
        data: { id: id},
        success: function(resp) {
            $("#modal-content").html(resp);

            $(".mask-money").maskMoney({allowZero: true});
            $(".mask-money").maskMoney('mask');
        }
    });
}

function saveModal(type) {
    let formValues = $("#modal-form").serialize();

    let requestUrl = "";
    let successMessage = "";
    switch (type) {
        case "BANKING":
            requestUrl = SnapshotUrls.saveBankingUrl()
            successMessage = "Banking completed successfully"
            break;
        case "CASH_INBOUND":
            requestUrl = SnapshotUrls.saveCashInboundUrl()
            successMessage = "Cash Inbound completed successfully"
            break;
        case "CASH_LIFT":
            requestUrl = SnapshotUrls.saveCashLiftUrl()
            successMessage = "Cash lift completed successfully"
            break;
    }

    $.ajax({
        url: requestUrl,
        method: "POST",
        data: formValues ,
        success: function(resp) {
            if (resp === "OK") {
                $('#snapshotModal').modal('hide');
                $('#alert-container').html("<div class=\"alert alert-success alert-wl mx-0\" role=\"alert\">" + successMessage + "</div>")
            } else {
                $("#modal-content").html(resp);
                $(".mask-money").maskMoney({allowZero: true});
                $(".mask-money").maskMoney('mask');
            }
        }
    });
}

