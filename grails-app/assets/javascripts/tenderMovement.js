function processTenderLift() {
    // Get form values
    var safeIdElement = $("select[name='safeId']");
    var tillNoElement = $("select[name='tillNo']");
    var tenderElement = $("select[name='tender']");
    var amountElement = $("#amount");

    // Get form values
    var safeId = safeIdElement.val();
    var tillNo = tillNoElement.val();
    var tender = tenderElement.val();
    var amount = parseFloat(amountElement.val());

    safeIdElement.removeClass("is-invalid");
    tillNoElement.removeClass("is-invalid");
    tenderElement.removeClass("is-invalid");
    amountElement.removeClass("is-invalid");

    // Validation flags
    var isValid = true;
    var errorMessage = "";

    // Validate safeId
    if (!safeId) {
        isValid = false;
        safeIdElement.addClass("is-invalid");
        errorMessage += "Please select a Safe.\n";
    }

    // Validate tillNo
    if (!tillNo) {
        isValid = false;
        tillNoElement.addClass("is-invalid");
        errorMessage += "Please select a Till No.\n";
    }

    // Validate tender
    if (!tender) {
        isValid = false;
        tenderElement.addClass("is-invalid");
        errorMessage += "Please select a Tender.\n";
    }

    // Validate amount
    if (isNaN(amount) || amount < 0.01 || amount > 999999.99) {
        isValid = false;
        amountElement.addClass("is-invalid");
        errorMessage += "Amount must be between £0.01 and £999,999.99.\n";
    }

    if (isValid) {
        // Check till balance
        getTillBalance(tillNo, tender, function(error, availableAmount) {
            if (error) {
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">Error fetching till balance. Please try again.</div>');
                return;
            }
            if (amount > availableAmount) {
                // Show warning and ask for confirmation
                if (confirm(`Entered amount £${amount.toFixed(2)} is more than available amount £${availableAmount.toFixed(2)} in till. Do you want to continue?`)) {
                    // User selected 'Yes', proceed with tender lift
                    submitTenderLift();
                } else {
                    // User selected 'No', clear the warning and allow re-entry
                    $("#messages-container").html('');
                    amountElement.val('').focus();
                }
            } else {
                // Amount is within available balance, proceed normally
                submitTenderLift();
            }
        });
    } else {
        if (errorMessage === '') {
            errorMessage = "Validation failed please check inputs and try again";
        }
        $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
    }
}


function getTillBalance(tillNo, tender, callback) {
    $.ajax({
        url: TenderMovementUrls.getTillAvailableBalance(),
        method: 'GET',
        data: { tillNo: tillNo, tender: tender },
        dataType: 'json', // Ensure the response is parsed as JSON
        success: function(response) {
            callback(null, response.availableAmount);
        },
        error: function(xhr, status, error) {
            callback(error);
        }
    });
}

function submitTenderLift() {
    $.ajax({
        url: TenderMovementUrls.getProcessTenderLift(),
        method: "POST",
        data: $("#processTenderLift").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
    });
}

function updateTenderMovementContainer(resp) {
    $("#tender-movement-container").html(resp);
    $(".mask-money").maskMoney({allowZero: true}).maskMoney('mask');
}
