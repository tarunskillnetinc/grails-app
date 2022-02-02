function showSafeSelectionModal() {
    $("#cashModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#cashModal').modal({ show: true });

    $("#saveShiftButton").hide();
    $("#saveShiftButton").prop("onclick", null).off("click");

    $("#cancelShiftButton").text("Cancel");

    $.ajax({
        url: SnapshotUrls.safeSelectionUrl(),
        method: "POST",
        data: {} ,
        success: function(resp) {
            $("#cashModalHeader").html("<h2>Safe Management</h2>")
            $("#cashModalContent").html(resp);

            $(".mask-money").maskMoney({ allowZero: true });
            $(".mask-money").maskMoney('mask');
        }
    });
}

function showSafeModal(locationId) {
    $("#cashModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#cashModal').modal({ show: true });

    $("#saveShiftButton").prop("onclick", null).off("click");
    $("#saveShiftButton").click(function () {
        submitSafeCount();
    });

    $("#saveShiftButton").show();
    $("#cancelShiftButton").text("Cancel");

    $.ajax({
        url: SnapshotUrls.getSafeUrl(),
        method: "POST",
        data: { locationId: locationId } ,
        success: function(resp) {
            $("#cashModalHeader").html("<h2>Safe Management</h2>")
            $("#cashModalContent").html(resp);

            $(".mask-money").maskMoney({ allowZero: true });
            $(".mask-money").maskMoney('mask');
        }
    });
}

function submitSafeCount() {
    var cashUpBy = $("#cashUpBy").val();

    if (cashUpBy === "VALUE" && !isFormValid()) {
        return;
    }

    var snapshotId = $("#snapshotId").val();

    var formValues = $("#cashUpForm").serialize();
    formValues += "&snapshotId=" + snapshotId;

    $.ajax({
        url: SnapshotUrls.saveSafeCountUrl(),
        method: "POST",
        data: formValues,
        success: function(resp) {
            $("#cashModalContent").html(resp);

            $("#saveShiftButton").prop("onclick", null).off("click");
            $("#saveShiftButton").click(function() {
                submitSafe();
            });
        }
    });
}

function submitSafe() {
    var formValues = $("#snapshotVarianceForm").serialize();

    $.ajax({
        url: SnapshotUrls.saveSnapshotUrl(),
        method: "POST",
        data: formValues,
        success: function(resp) {
            $("#cashModalContent").html(resp);

            $("#saveShiftButton").prop("onclick", null).off("click");
            $("#saveShiftButton").hide();

            $("#cancelShiftButton").text("Close");
        }
    });
}