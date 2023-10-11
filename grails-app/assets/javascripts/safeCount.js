function showSafeSelectionModal() {
    $("#modal-content").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#shiftModal').modal({ show: true });

    $("#saveShiftButton").hide();
    $("#saveShiftButton").prop("onclick", null).off("click");

    $("#cancelShiftButton").text("Cancel");

    $.ajax({
        url: SnapshotUrls.safeSelectionUrl(),
        method: "POST",
        data: {} ,
        success: function(resp) {
            $("#modal-content").html(resp);

            $(".mask-money").maskMoney({ allowZero: true });
            $(".mask-money").maskMoney('mask');
        }
    });
}

function showSafeModal(id) {
    $("#modal-content").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#shiftModal').modal({ show: true });

    $.ajax({
        url: SnapshotUrls.getSafeUrl(),
        method: "POST",
        data: { id: id } ,
        success: function(resp) {
            $("#modal-content").html(resp);

            $(".mask-money").maskMoney({ allowZero: true });
            $(".mask-money").maskMoney('mask');
            $(".denomination").focusout(function () {
                if (!this.value || this.value < 0) {
                    this.value = 0;
                }
            })
            .keypress(function (e) {
                if (["e", "E", "+", "-"].includes(e.key)) {
                    e.preventDefault();
                }
            });
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
            $("#modal-content").html(resp);
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
            $("#modal-content").html(resp);
        }
    });
}