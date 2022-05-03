function showSafeModal() {
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
        data: {} ,
        success: function(resp) {
            $("#cashModalHeader").html("<h2>Safe Management</h2>")
            $("#cashModalContent").html(resp);

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

    var formValues = $("#cashUpForm").serialize();

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