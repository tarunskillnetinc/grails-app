function searchSafe(sortParams, isForceButtonClick) {
    $("#search-results").hide();
    $("#loading-indicator").show();
    var inactiveSafes = $('#inactiveSafes').prop("checked");
    if (isForceButtonClick){
        hideMessages()
    }
    $.ajax({
        url: SafeUrls.getSearchSafeUrl(),
        method: "POST",
        data: {inactiveSafes: inactiveSafes},
        success: function (resp) {
            $('#results-container').html(resp);
            $('#safeSearchTerm').data('prev', $('#memberOfferSearchTerm').val());
        },
        error: function (resp) {
            var errorMessage = resp.responseJSON && resp.responseJSON.message ? resp.responseJSON.message : "Safe search failed.";
            displayMessage('error', errorMessage);
        }
    })
}

function validateAndSave() {
    var error = false;
    var errorString = "";

    var description = $('#description').val();
    if (description === "" || description.trim() === "") {
        error = true;
        errorString = errorString.concat("\nThe description can not be empty.");
    }

    var type = $('#type').val();
    if (type === "" || type.trim() === "") {
        error = true;
        errorString = errorString.concat("\nThe safe type can not be empty. Please select type.");
    }

    var safeStatus = $('input[name="active"]:checked').val();
    if (safeStatus === undefined) {
        error = true;
        errorString = errorString.concat("\nThe safe status must be selected.");
    }


    if (!error) {
        if (confirm('Confirm changes. Are you sure you wish to save these changes?')) {
            $('#safeDetails').submit();
        }
    } else {
        displayMessage('error', errorString);
    }
}

function updatePrimarySafe(selectedSafeId, selectedDescription) {
    confirmAndSubmit("Are you sure you want to change the primary safe to " + selectedDescription + " ? ", function() {
        var inactiveSafes = $('#inactiveSafes').prop("checked");
        $.ajax({
            url: SafeUrls.getUpdatePrimarySafeUrl(),
            method: "POST",
            data: {
                inactiveSafes: inactiveSafes,
                selectedSafeId: selectedSafeId
            },
            success: function (resp) {
                $('#results-container').html(resp);
            },
            error: function (resp) {
                $('#results-container').html(resp);
            }
        });
    });
}


function handleSafeRowClickEvent(event, url) {
    if (!event.target.closest('button')) { // Check if the click didn't come from the button
        var tempLink = document.createElement('a'); // Create a temporary anchor element
        tempLink.href = url;
        addInactiveSafesParam(tempLink); // Use addInactiveSafesParam to modify the URL
        document.location.href = tempLink.href; // Navigate to the modified URL
    }
}

function handleCancelAddSafe(url) {
    confirmAndSubmit("Are you sure you want to cancel ?", function() {
        cancelAddSafeView(url);
    });
}

function cancelAddSafeView(url) {
    var tempLink = document.createElement('a'); // Create a temporary anchor element
    tempLink.href = url;
    addInactiveSafesParam(tempLink); // Use addInactiveSafesParam to modify the URL
    document.location.href = tempLink.href; // Navigate to the modified URL
}

function addInactiveSafesParam(link) {
    var inactiveSafesElement = document.getElementById('inactiveSafes');
    var inactiveSafes;

    if (inactiveSafesElement.type === 'checkbox') {
        inactiveSafes = $('#inactiveSafes').prop("checked");
    } else {
        inactiveSafes = $('#inactiveSafes').val()
    }
    var url = link.href;
    url += (url.indexOf('?') !== -1 ? '&' : '?') + 'inactiveSafes=' + inactiveSafes;
    link.href = url;
    return true;
}

function displayMessage(type, message) {
    hideMessages()
    if (type === 'success') {
        $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + message + '</div>');
    } else if (type === 'error') {
        $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + message + '</div>');
    } else {
        $("#messages-container").html(''); // Clear the container if no message is provided
    }
}

function hideMessages() {
    const containers = [
        document.getElementById('alerts-success-container-message'),
        document.getElementById('alerts-error-container-message')
    ];

    containers.forEach(container => {
        if (container) {
            container.style.display = 'none';
        }
    });
}

function resetSafeFilters() {
    $('#inactiveSafes').prop('checked', false);
    searchSafe(null, true);
}

function confirmAndSubmit(message, yesCallBack) {
    let result = confirm(message);
    if (result) {
        yesCallBack();
    }
}
