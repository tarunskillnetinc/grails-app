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


function showSafeSessionReconcileModal(sessionId, versionId, isRecount, isFinal, safeDescription, configuredRecountAttempt, currentRecountAttempt) {
    var proceedWithWarning = true;
    if ((!isRecount && configuredRecountAttempt === 0) || (isRecount && configuredRecountAttempt === currentRecountAttempt + 1)) { //This is only for reconcile actions to show warning
        proceedWithWarning = confirm("Warning! This is your last available chance to count the safe");
    }
    if (proceedWithWarning) {
        $.ajax({
            url: SafeManagementUrls.getSafeSessionCashUpUrl(),
            method: "POST",
            data: { sessionId: sessionId, versionId: versionId, isRecount: isRecount, isFinalise: isFinal, safeDescription: safeDescription },
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
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
            }
        });
    }
}

function saveSafeSessionCashUrl(safeSessionId, versionId, isRecount, safeDescription) {
    var cashUpBy = $("#cashUpBy").val();
    if (cashUpBy === "VALUE" && !isFormValid()) {
        return;
    }
    var formValues = $("#cashUpForm").serializeArray();
    formValues.push({name:'safeSessionId', value: safeSessionId})
    formValues.push({name:'versionId', value: versionId})
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
            if ($('#sessionModal').length) {
                $('#sessionModal').modal('hide');
            }

            if ($('.modal-backdrop').length) {
                $('.modal-backdrop').remove();
            }
            $('body').removeClass('modal-open');

            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
        }
    });
}

function submitSafeSession(safeSessionId, versionId, isRecount, isFinalise, safeDescription, isSafeFinalisingWarningRequired) {
    var proceedWithSubmission = true;
    if (isFinalise && isSafeFinalisingWarningRequired) {
        proceedWithSubmission = confirm("This safe is inactive and still contains tender value. Are you sure you want to finalise the safe?");
    }
    if (proceedWithSubmission) {

        var formValues = $("#safeSessionVarianceForm").serializeArray();
        formValues.push({name:'safeSessionId', value: safeSessionId})
        formValues.push({name:'versionId', value: versionId})
        formValues.push({name:'isRecount', value: isRecount})
        formValues.push({name:'isFinalise', value: isFinalise})
        formValues.push({name:'safeDescription', value: safeDescription})

        $.ajax({
            url: SafeManagementUrls.getSafeSessionSaveUrl(),
            method: "POST",
            data: formValues,
            success: function(resp) {
                $("#modal-content").html('')
                $('#sessionModal').modal('hide'); // This line hides the modal
                $("#results-container").html(resp);
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

                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
            }
        });
    }
}

function safeSpotCheck(safeSessionId) {
    $.ajax({
        url: SafeManagementUrls.spotCheckUrl(),
        method: "POST",
        data: {safeSessionId: safeSessionId},
        success: function(resp) {
            $('#sessionModal').modal({ show: true });
            $("#modal-content").html(resp);
        },
        error: function(resp) {
            $("#search-results").show();
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
        },
    });
}