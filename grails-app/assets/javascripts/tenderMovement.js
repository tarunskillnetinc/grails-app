function processTenderLift() {
    const safeIdElement = $("select[name='safeId']");
    const tillNoElement = $("select[name='tillNo']");
    const tenderElement = $("select[name='tender']");
    const amountElement = $("#amount");

    const formElements = [safeIdElement, tillNoElement, tenderElement, amountElement];
    const requiredFields = [
        { element: safeIdElement, value: safeIdElement.val(), errorMessage: "Please select a Safe." },
        { element: tillNoElement, value: tillNoElement.val(), errorMessage: "Please select a Till No." },
        { element: tenderElement, value: tenderElement.val(), errorMessage: "Please select a Tender." }
    ];

    if (!validateForm(formElements, requiredFields)) return;

    const amount = parseFloat(amountElement.val());
    const tillNos = [tillNoElement.val()];

    getTillBalance(tillNos, tenderElement.val(), amount, (error, result) => {
        let confirmMessage = `Entered amount £${amount.toFixed(2)} is more than available amount in till. Do you want to continue?`;
        handleBalanceCheck(error, result, confirmMessage, submitTenderLift);
    });
}

function processIssueFloat() {
    const safeIdElement = $("select[name='safeId']");
    const tenderElement = $("select[name='tender']");
    const amountElement = $("#amount");

    const tillNos = $("input[name='tillNos']:checked").map(function() {
        return $(this).val();
    }).get();

    const formElements = [safeIdElement, tenderElement, amountElement, $("#tillNo")];
    const requiredFields = [
        { element: safeIdElement, value: safeIdElement.val(), errorMessage: "Please select a Safe." },
        { element: $("#tillNo"), value: tillNos.length > 0, errorMessage: "Please select at least one Till No." },
        { element: tenderElement, value: tenderElement.val(), errorMessage: "Please select a Tender." }
    ];

    if (!validateForm(formElements, requiredFields)) return;

    const amount = parseFloat(amountElement.val());

    // Multiply the amount by the number of tills
    const totalAmountToBeDistributed = amount * tillNos.length;


    getSafeBalance(totalAmountToBeDistributed, tenderElement.val(), safeIdElement.val(), (error, result) => {
        let confirmMessage = `Entered amount £${amount.toFixed(2)} is more than available amount in the safe. Do you want to continue?`;
        handleBalanceCheck(error, result, confirmMessage, submitIssueFloat);
    });
}

function processPayIn() {
    $("#messages-container").html('');

    // Get form elements
    const safeIdElement = $("select[name='safeId']");
    const tenderElement = $("select[name='tender']");
    const reasonCodeElement = $("select[name='reasoncodeId']");
    const amountElement = $("#amount");

    // Get form values
    const safeId = safeIdElement.val();
    const tender = tenderElement.val();
    const reasonCode = reasonCodeElement.val();
    const amount = parseFloat(amountElement.val());

    // Reset validation styles
    [safeIdElement, tenderElement, reasonCodeElement, amountElement].forEach(el => el.removeClass("is-invalid"));

    // Validation
    const validationErrors = [];

    if (!safeId) validationErrors.push({ element: safeIdElement, message: "Please select a Safe." });
    if (!tender) validationErrors.push({ element: tenderElement, message: "Please select a Tender." });
    if (!reasonCode) validationErrors.push({ element: reasonCodeElement, message: "Please select a Reason Code." });

    if (isNaN(amount) || amount < 0.01 || amount > 9999.99) {
        validationErrors.push({ element: amountElement, message: "Amount must be between £0.01 and £9,999.99." });
    }

    if (validationErrors.length > 0) {
        validationErrors.forEach(error => error.element.addClass("is-invalid"));
        const errorMessage = validationErrors.map(error => error.message).join("<br>");
        $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
        return;
    }

    submitPayIn();
}

function processPayOut() {
    // Get form elements
    const safeIdElement = $("select[name='safeId']");
    const reasonCodeElement = $("select[name='reasonCode']");
    const tenderElement = $("#tender");
    const amountElement = $("#amount");

    // Get form values
    const safeId = safeIdElement.val();
    const reasonCode = reasonCodeElement.val();
    const tender = tenderElement.val();
    const amount  = parseFloat(amountElement.val());

    // Reset validation styles
    [safeIdElement, reasonCodeElement, tenderElement, amountElement].forEach(el => el.removeClass("is-invalid"));

    // Validation
    const validationErrors = [];

    if (!safeId) validationErrors.push({ element: safeIdElement, message: "Please select a Safe." });
    if (!reasonCode) validationErrors.push({ element: reasonCodeElement, message: "Please select a reason code" });
    if (!tender) validationErrors.push({ element: tenderElement, message: "Please select a Tender." });
    if (isNaN(amount) || amount < 0.01 || amount > 9999.99) {
        validationErrors.push({ element: amountElement, message: "Amount must be between £0.01 and £9999.99." });
    }

    if (validationErrors.length > 0) {
        validationErrors.forEach(error => error.element.addClass("is-invalid"));
        const errorMessage = validationErrors.map(error => error.message).join("<br>");
        $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
        return;
    }
    getSafeBalance(amount, tender, safeId, (error, result) => {
        let confirmMessage = `Entered amount £${amount.toFixed(2)} is more than available amount in the safe. Do you want to continue?`;
        handleBalanceCheck(error, result, confirmMessage, submitPayOut);
    });
}

function processBankDeposit() {
    // Get form elements
    const safeIdElement = $("select[name='safeId']");
    const bankingDateElement = $("input[name='bankingDate']");
    const tenderElement = $("input[name='tender']");
    const amountElement = $("#amount");

    // Get form values
    const safeId = safeIdElement.val();
    const bankingDate = bankingDateElement.val();
    const tender = tenderElement.val();
    const amount = parseFloat(amountElement.val());

    // Reset validation styles
    [safeIdElement, bankingDateElement, tenderElement, amountElement].forEach(el => el.removeClass("is-invalid"));

    // Validation
    const validationErrors = [];

    if (!safeId) validationErrors.push({ element: safeIdElement, message: "Please select a Safe." });
    if (!bankingDate) validationErrors.push({ element: bankingDateElement, message: "Please enter a banking date." });
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

    // If validation passes, submit the form
    getSafeBalance(amount, tender, safeId, (error, result) => {
        let confirmMessage = `Entered amount £${amount.toFixed(2)} is more than available amount in the safe for a Cash tender. Do you want to continue?`;
        handleBalanceCheck(error, result, confirmMessage, submitBankDeposit);
    });
}

function processBankReceipt() {
    // Get form elements
    const safeIdElement = $("select[name='safeId']");
    const bankingDateElement = $("input[name='bankingDate']");
    const tenderElement = $("input[name='tender']");
    const amountElement = $("#amount");

    // Get form values
    const safeId = safeIdElement.val();
    const bankingDate = bankingDateElement.val();
    const tender = tenderElement.val();
    const amount = parseFloat(amountElement.val());

    // Reset validation styles
    [safeIdElement, bankingDateElement, tenderElement, amountElement].forEach(el => el.removeClass("is-invalid"));

    // Validation
    const validationErrors = [];

    if (!safeId) validationErrors.push({ element: safeIdElement, message: "Please select a Safe." });
    if (!bankingDate) validationErrors.push({ element: bankingDateElement, message: "Please enter a banking date." });
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

    // If validation passes, submit the form
    submitBankReceipt();
}

function validateForm(formElements, requiredFields) {
    const validationErrors = [];
    formElements.forEach(el => el.removeClass("is-invalid"));

    requiredFields.forEach(field => {
        if (!field.value) {
            validationErrors.push({ element: field.element, message: field.errorMessage });
        }
    });

    const amountElement = $("#amount");
    const amount = parseFloat(amountElement.val());
    if (isNaN(amount) || amount < 0.01 || amount > 999999.99) {
        validationErrors.push({ element: amountElement, message: "Amount must be between £0.01 and £9999.99." });
    }

    if (validationErrors.length > 0) {
        validationErrors.forEach(error => error.element.addClass("is-invalid"));
        const errorMessage = validationErrors.map(error => error.message).join("<br>");
        $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
        return false;
    }

    return true;
}

function handleBalanceCheck(error, result, confirmMessage,  submitFunction) {
    if (error) {
        const errorMessage = error.errorMessages && error.errorMessages.length > 0
            ? error.errorMessages.join("<br>")
            : 'Error fetching balance. Please try again.';
        $("#messages-container").html(`<div class="alert alert-danger alert-wl mx-0" role="alert">${errorMessage}</div>`);
        return;
    }

    const { availableAmount, isAmountLessThanEntered } = result;

    if (isAmountLessThanEntered) {
        if (confirm(confirmMessage)) {
            submitFunction();
        } else {
            $("#messages-container").html('');
            $("#amount").focus();
        }
    } else {
        submitFunction();
    }
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
                    isAmountLessThanEntered: response.isTillAmountLessThanEntered
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

function getSafeBalance(totalAmountToBeDistributed, tender, safeId, callback) {
    $.ajax({
        url: TenderMovementUrls.getSafeAvailableBalance(),
        method: 'GET',
        data: { totalAmountToBeDistributed: totalAmountToBeDistributed, tender: tender, safeId: safeId },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                callback(null, {
                    availableAmount: response.availableAmount,
                    isAmountLessThanEntered: response.isSafeAmountLessThanEntered
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

function submitPayIn() {
    $.ajax({
        url: TenderMovementUrls.getProcessPayIn(),
        method: "POST",
        data: $("#processPayIn").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
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

function submitPayOut() {
    $.ajax({
        url: TenderMovementUrls.getProcessPayOut(),
        method: "POST",
        data: $("#processPayOut").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
    });
}

function submitBankDeposit() {
    $.ajax({
        url: TenderMovementUrls.getProcessBankDeposit(),
        method: "POST",
        data: $("#processBankDeposit").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
    });
}

function submitBankReceipt() {
    $.ajax({
        url: TenderMovementUrls.getProcessBankReceipt(),
        method: "POST",
        data: $("#processBankReceipt").serialize(),
        success: updateTenderMovementContainer,
        error: updateTenderMovementContainer
    });
}


function handleCancelTenderUpdate(url) {
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

function enforceAlphanumeric(input) {
    // Remove any non-alphanumeric characters
    input.value = input.value.replace(/[^a-zA-Z0-9]/g, '');

    // Ensure the input doesn't exceed the maxlength
    if (input.value.length > input.maxLength) {
        input.value = input.value.slice(0, input.maxLength);
    }
}
