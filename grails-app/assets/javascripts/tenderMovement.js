function processTenderLift() {
    // Get form elements
    const safeIdElement = $("select[name='safeId']");
    const tillNoElement = $("select[name='tillNo']");
    const tenderElement = $("select[name='tender']");
    const amountElement = $("#amount");

    // Get form values
    const safeId = safeIdElement.val();
    const tillNo = tillNoElement.val();
    const tender = tenderElement.val();
    const amount = parseFloat(amountElement.val());
    const tillNos = [tillNo]; // For future use with multiple tills

    // Reset validation styles
    [safeIdElement, tillNoElement, tenderElement, amountElement].forEach(el => el.removeClass("is-invalid"));

    // Validation
    const validationErrors = [];

    if (!safeId) validationErrors.push({ element: safeIdElement, message: "Please select a Safe." });
    if (!tillNo) validationErrors.push({ element: tillNoElement, message: "Please select a Till No." });
    if (!tender) validationErrors.push({ element: tenderElement, message: "Please select a Tender." });
    if (isNaN(amount) || amount < 0.01 || amount > 999999.99) {
        validationErrors.push({ element: amountElement, message: "Amount must be between £0.01 and £999,999.99." });
    }

    if (validationErrors.length > 0) {
        validationErrors.forEach(error => error.element.addClass("is-invalid"));
        const errorMessage = validationErrors.map(error => error.message).join("<br>");
        $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
        return;
    }

    // Proceed with getTillBalance
    getTillBalance(tillNos, tender, amount, (error, result) => {
        if (error) { //Handle if server send an error
            if (error.errorMessages && error.errorMessages.length > 0) {// Display specific error messages from the server
                const errorMessage = error.errorMessages.join("<br>");
                $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
            } else {  // Fallback to generic error message if no specific messages are available
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">Error fetching till balance. Please try again.</div>');
            }
            return;
        }

        const { availableAmount, isTillAmountLessThanEntered } = result;

        if (isTillAmountLessThanEntered) {
            if (confirm(`Entered amount £${amount.toFixed(2)} is more than available amount £${availableAmount.toFixed(2)} in till. Do you want to continue?`)) {
                submitTenderLift();
            } else {
                $("#messages-container").html('');
                amountElement.focus();
            }
        } else {
            submitTenderLift();
        }
    });
}

function processIssueFloat() {
    // Get form elements
    const safeIdElement = $("select[name='safeId']");
    const tenderElement = $("select[name='tender']");
    const amountElement = $("#amount");

    // Get form values
    const safeId = safeIdElement.val();
    const tender = tenderElement.val();
    const amount = parseFloat(amountElement.val());
    const tillNos = $("input[name='tillNos']:checked").map(function() {
        return $(this).val();
    }).get();

    // Reset validation styles
    [safeIdElement, tenderElement, amountElement].forEach(el => el.removeClass("is-invalid"));
    $("#tillNo").removeClass("is-invalid");

    // Validation
    const validationErrors = [];

    if (!safeId) validationErrors.push({ element: safeIdElement, message: "Please select a Safe." });
    if (tillNos.length === 0) validationErrors.push({ element: $("#tillNo"), message: "Please select at least one Till No." });
    if (!tender) validationErrors.push({ element: tenderElement, message: "Please select a Tender." });
    if (isNaN(amount) || amount < 0.01 || amount > 999999.99) {
        validationErrors.push({ element: amountElement, message: "Amount must be between £0.01 and £999,999.99." });
    }

    if (validationErrors.length > 0) {
        validationErrors.forEach(error => error.element.addClass("is-invalid"));
        const errorMessage = validationErrors.map(error => error.message).join("<br>");
        $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
        return;
    }

    //Proceed with getTillBalance
    getSafeBalance(tillNos, tender, amount, safeId,(error, result) => {
        if (error) { //Handle if server send an error
            if (error.errorMessages && error.errorMessages.length > 0) {// Display specific error messages from the server
                const errorMessage = error.errorMessages.join("<br>");
                $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
            } else {  // Fallback to generic error message if no specific messages are available
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">Error fetching save balance. Please try again.</div>');
            }
            return;
        }

        const { availableAmount, isSafeAmountLessThanEntered } = result;

        if (isSafeAmountLessThanEntered) {
            if (confirm(`Entered amount £${amount.toFixed(2)} is more than available amount £${availableAmount.toFixed(2)} in the safe. Do you want to continue?`)) {
                submitIssueFloat();
            } else {
                $("#messages-container").html('');
                amountElement.focus();
            }
        } else {
            submitIssueFloat();
        }
    });
}


function getTillBalance(tillNos, tender, enteredAmount, callback) {
    $.ajax({
        url: TenderMovementUrls.getTillAvailableBalance(),
        method: 'GET',
        data: { tillNos: JSON.stringify(tillNos), tender: tender, enteredAmount: enteredAmount },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                callback(null, {
                    availableAmount: response.availableAmount,
                    isTillAmountLessThanEntered: response.isTillAmountLessThanEntered
                });
            } else {
                // If the response indicates an error, pass the error messages to the callback
                callback({
                    errorMessages: response.errorMessages || ["Unknown error occurred"]
                });
            }
        },
        error: function(xhr, status, error) {
            try {
                // Try to parse the error response
                const errorResponse = JSON.parse(xhr.responseText);
                callback({
                    errorMessages: errorResponse.errorMessages || [error]
                });
            } catch (e) {
                // If parsing fails, return the original error
                callback({
                    errorMessages: [error]
                });
            }
        }
    });
}

function getSafeBalance(tillNos, tender, enteredAmount, safeId, callback) {
    $.ajax({
        url: TenderMovementUrls.getSafeAvailableBalance(),
        method: 'GET',
        data: { tillNos: JSON.stringify(tillNos), tender: tender, enteredAmount: enteredAmount, safeId: safeId },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                callback(null, {
                    availableAmount: response.availableAmount,
                    isSafeAmountLessThanEntered: response.isSafeAmountLessThanEntered
                });
            } else {
                // If the response indicates an error, pass the error messages to the callback
                callback({
                    errorMessages: response.errorMessages || ["Unknown error occurred"]
                });
            }
        },
        error: function(xhr, status, error) {
            try {
                // Try to parse the error response
                const errorResponse = JSON.parse(xhr.responseText);
                callback({
                    errorMessages: errorResponse.errorMessages || [error]
                });
            } catch (e) {
                // If parsing fails, return the original error
                callback({
                    errorMessages: [error]
                });
            }
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

function submitIssueFloat() {
    $.ajax({
        url: TenderMovementUrls.getProcessIssueFloat(),
        method: "POST",
        data: $("#processIssueFloat").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
    });
}

function handleCancelTenderLift(url) {
    confirmAndSubmit("Are you sure you want to cancel ?", function() {
        window.location.href = '/';
    });
}

function updateTenderMovementContainer(resp) {
    $("#tender-movement-container").html(resp);
    $(".mask-money").maskMoney({allowZero: true}).maskMoney('mask');
}

function confirmAndSubmit(message, yesCallBack) {
    let result = confirm(message);
    if (result) {
        yesCallBack();
    }
}
