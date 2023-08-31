function getShifts() {
    $("#search-results").hide();
    $("#loading-indicator").show();

    var startDate = $("#startDate").val();
    var endDate = $("#endDate").val();
    var tillId = $("#tillId").val();

    $.ajax({
        url: ShiftUrls.getShiftsUrl(),
        method: "POST",
        data: { startDate: startDate, endDate: endDate, tillId: tillId },
        success: function(resp) {
            $("#results-container").html(resp);
        }
    });
}

function resetFilters(startDate, endDate) {
    $("#startDate").val(startDate);
    $("#startDate").datepicker('setDate', startDate);

    $("#endDate").val(endDate);
    $("#endDate").datepicker('setDate', endDate);

    $("#tillId").val("");

    getShifts();
}

function showCashModal(shiftId, isReconciled) {
    $("#cashModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
    $('#cashModal').modal({ show: true });

    if (isReconciled) {
        $("#saveShiftButton").prop("onclick", null).off("click");
        $("#saveShiftButton").hide();

        $("#cancelShiftButton").prop("onclick", null).off("click");
        $("#cancelShiftButton").text("Close");
    } else {
        $("#saveShiftButton").prop("onclick", null).off("click");
        $("#saveShiftButton").click(function () {
            submitCash(shiftId);
        });

        $("#saveShiftButton").show();
        $("#cancelShiftButton").text("Cancel");
    }

    $.ajax({
        url: ShiftUrls.getCashDetailsUrl(),
        method: "POST",
        data: { shiftId: shiftId },
        success: function(resp) {
            $("#cashModalHeader").html("<h2>Cash Management</h2>")
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

function changeCashUpType(type) {
    var cashUpByValueLink = $("#cashUpByValueLink");
    var cashUpByDenominationLink = $("#cashUpByDenominationLink");
    var cashUpByTotalsLink = $("#cashUpByTotalsLink");

    cashUpByValueLink.removeClass("disabled");
    cashUpByDenominationLink.removeClass("disabled");
    cashUpByTotalsLink.removeClass("disabled");

    if (type === "VALUE") {
        cashUpByValueLink.addClass("disabled");
        cashUpByValueLink.prop("onclick", null).off("click");

        cashUpByDenominationLink.click(function() {
            changeCashUpType("DENOMINATION");
        });

        cashUpByTotalsLink.click(function() {
            changeCashUpType("TOTALS");
        });
    } else if (type === "DENOMINATION") {
        cashUpByValueLink.click(function() {
            changeCashUpType("VALUE");
        });

        cashUpByDenominationLink.addClass("disabled");
        cashUpByDenominationLink.prop("onclick", null).off("click");

        cashUpByTotalsLink.click(function() {
            changeCashUpType("TOTALS");
        });
    } else if (type === "TOTALS") {
        cashUpByValueLink.click(function() {
            changeCashUpType("VALUE");
        });

        cashUpByDenominationLink.click(function() {
            changeCashUpType("DENOMINATION");
        });

        cashUpByTotalsLink.addClass("disabled");
        cashUpByTotalsLink.prop("onclick", null).off("click");
    }

    var formValues = $("#cashUpForm").serialize();
    formValues = formValues + "&type=" +type;

    $.ajax({
        url: ShiftUrls.changeCashUpTypeUrl(),
        method: "POST",
        data: formValues,
        success: function(resp) {
            $("#cashUpContainer").html(resp);

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

function submitCash(shiftId) {
    var cashUpBy = $("#cashUpBy").val();

    if (cashUpBy === "VALUE" && !isFormValid()) {
        return;
    }

    var formValues = $("#cashUpForm").serialize();
    formValues = formValues + "&shiftId=" +shiftId

    $.ajax({
        url: ShiftUrls.saveCashUrl(),
        method: "POST",
        data: formValues,
        success: function(resp) {
            $("#cashModalContent").html(resp);

            $("#saveShiftButton").prop("onclick", null).off("click");
            $("#saveShiftButton").click(function() {
                submitShift(shiftId);
            });
        }
    });
}

function isFormValid() {
    var isFormValid = true;

    var fiftyPounds = $("#fiftyPounds");
    var twentyPounds = $("#twentyPounds");
    var tenPounds = $("#tenPounds");
    var fivePounds = $("#fivePounds");
    var twoPounds = $("#twoPounds");
    var onePounds = $("#onePounds");
    var fiftyPences = $("#fiftyPences");
    var twentyPences = $("#twentyPences");
    var tenPences = $("#tenPences");
    var fivePences = $("#fivePences");
    var twoPences = $("#twoPences");
    var onePences = $("#onePences");

    fiftyPounds.removeClass("is-invalid");
    twentyPounds.removeClass("is-invalid");
    tenPounds.removeClass("is-invalid");
    fivePounds.removeClass("is-invalid");
    twoPounds.removeClass("is-invalid");
    onePounds.removeClass("is-invalid");
    fiftyPences.removeClass("is-invalid");
    twentyPences.removeClass("is-invalid");
    tenPences.removeClass("is-invalid");
    fivePences.removeClass("is-invalid");
    twoPences.removeClass("is-invalid");
    onePences.removeClass("is-invalid");

    if (fiftyPounds.val() % 50 > 0) {
        fiftyPounds.addClass("is-invalid");
        isFormValid = false;
    }
    if (twentyPounds.val() % 20 > 0) {
        twentyPounds.addClass("is-invalid");
        isFormValid = false;
    }
    if (tenPounds.val() % 10 > 0) {
        tenPounds.addClass("is-invalid");
        isFormValid = false;
    }
    if (fivePounds.val() % 5 > 0) {
        fivePounds.addClass("is-invalid");
        isFormValid = false;
    }
    if (twoPounds.val() % 2 > 0) {
        twoPounds.addClass("is-invalid");
        isFormValid = false;
    }
    if (onePounds.val() % 1 > 0) {
        onePounds.addClass("is-invalid");
        isFormValid = false;
    }
    if (Math.round(fiftyPences.val() * 100) % 50 > 0) {
        fiftyPences.addClass("is-invalid");
        isFormValid = false;
    }

    if (Math.round(twentyPences.val() * 100) % 20 > 0) {
        twentyPences.addClass("is-invalid");
        isFormValid = false;
    }
    if (Math.round(tenPences.val() * 100) % 10 > 0) {
        tenPences.addClass("is-invalid");
        isFormValid = false;
    }
    if (Math.round(fivePences.val() * 100) % 5 > 0) {
        fivePences.addClass("is-invalid");
        isFormValid = false;
    }
    if (Math.round(twoPences.val() * 100) % 2 > 0) {
        twoPences.addClass("is-invalid");
        isFormValid = false;
    }
    if (Math.round(onePences.val() * 100) % 1 > 0) {
        onePences.addClass("is-invalid");
        isFormValid = false;
    }

    return isFormValid;
}

function submitShift() {
    var formValues = $("#shiftVarianceForm").serialize();

    $.ajax({
        url: ShiftUrls.saveShiftUrl(),
        method: "POST",
        data: formValues,
        success: function(resp) {
            $("#cashModalContent").html(resp);

            $("#saveShiftButton").prop("onclick", null).off("click");
            $("#saveShiftButton").hide();

            $("#cancelShiftButton").text("Close");
            $("#cancelShiftButton").prop("onclick", null).off("click");
            $("#cancelShiftButton").click(function() {
                getShifts();
            });
        }
    });
}