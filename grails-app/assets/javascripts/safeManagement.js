function getSafeSessions() {

    $.ajax({
        url: SafeManagementUrls.getSafeSessionUrl(),
        method: "POST",
        success: function(resp) {
            $("#results-container").html(resp);
        },
        error: function() {
            $("#search-results").show();
            const result = document.createElement('div');
            $(result).addClass('col pt-2 pb-2 text-center my-auto wl-striped0').html('No shifts found.');
            $("#search-results").html(result);
        },
    });
}


function showSafeSessionReconcileModal(sessionId, isRecount, isFinal, safeDescription, configuredRecountAttempt, currentRecountAttempt) {
    var proceedWithWarning = true;
    if ((!isRecount && configuredRecountAttempt === 0) || (isRecount && configuredRecountAttempt === currentRecountAttempt + 1)) { //This is only for reconcile actions to show warning
        proceedWithWarning = confirm("Warning! This is your last available chance to count the safe");
    }
    if (proceedWithWarning) {
        $.ajax({
            url: SafeManagementUrls.getSafeSessionCashUpUrl(),
            method: "POST",
            data: { sessionId: sessionId, isRecount: isRecount, isFinalise: isFinal, safeDescription: safeDescription },
            success: function(resp) {
                $('#sessionModal').modal({ show: true, backdrop: 'static', keyboard: false });
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
            },
            error: function (resp){
                var errorMessage = resp.responseJSON && resp.responseJSON.message ?
                    resp.responseJSON.message : "Action failed for safe: " + safeDescription;
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
            }
        });
    }
}

function saveSafeSessionCashUrl(safeSessionId, isRecount, safeDescription) {
    var cashUpBy = $("#cashUpBy").val();
    if (cashUpBy === "VALUE" && !isFormValid()) {
        return;
    }
    var formValues = $("#cashUpForm").serializeArray();
    formValues.push({name:'safeSessionId', value: safeSessionId})
    formValues.push({name:'isRecount', value: isRecount})
    formValues.push({name:'safeDescription', value: safeDescription})


    $.ajax({
        url: SafeManagementUrls.getUpdateSafeSessionReconcileUrl(),
        method: "POST",
        data: formValues,
        success: function(resp) {
            $("#modal-content").html(resp);
        },
        error: function (resp) {
            if ($("#modal-content").length) {
                $("#modal-content").empty();
            }
            if ($('#shiftModal').length) {
                $('#shiftModal').modal('hide');
            }

            if ($('.modal-backdrop').length) {
                $('.modal-backdrop').remove();
            }
            $('body').removeClass('modal-open');
            var errorMessage = resp.responseJSON && resp.responseJSON.message ?
                resp.responseJSON.message : "Action failed for safe: " + safeDescription;
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
        }
    });
}

function submitSafeSession(safeSessionId, isRecount, isFinalise, safeDescription, isSafeFinalisingWarningRequired) {
    var proceedWithSubmission = true;
    if (isFinalise && isSafeFinalisingWarningRequired) {
        proceedWithSubmission = confirm("Safe is in inactive and still contain tender value. Are you sure you want to finalise the safe?");
    }
    if (proceedWithSubmission) {

        var formValues = $("#safeSessionVarianceForm").serializeArray();
        formValues.push({name:'safeSessionId', value: safeSessionId})
        formValues.push({name:'isRecount', value: isRecount})
        formValues.push({name:'isFinalise', value: isFinalise})
        formValues.push({name:'safeDescription', value: safeDescription})

        $.ajax({
            url: SafeManagementUrls.getSafeSessionSaveUrl(),
            method: "POST",
            data: formValues,
            success: function(resp) {
                if (isFinalise){
                    $("#modal-content").html('')
                    $('#sessionModal').modal('hide'); // This line hides the modal
                    $("#results-container").html(resp);
                } else {
                    $("#modal-content").html(resp);
                }
            },
            error: function (resp) {
                if ($("#modal-content").length) {
                    $("#modal-content").empty();
                }
                if ($('#sessionModal').length) {
                    $('#sessionModal').modal('hide');
                }

                if ($('.modal-backdrop').length) {
                    $('.modal-backdrop').remove();
                }
                $('body').removeClass('modal-open');
                var errorMessage = resp.responseJSON && resp.responseJSON.message ?
                    resp.responseJSON.message : "Action failed for safe " + safeDescription;
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
            }
        });
    }
}