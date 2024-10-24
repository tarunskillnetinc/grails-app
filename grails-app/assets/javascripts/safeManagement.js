function searchSafe(sortParams, isPrimaryDropDownOnly) {
    $("#search-results").hide();
    $("#loading-indicator").show();

    var inactiveSafes = $('#inactiveSafes').prop("checked");
    var isDropdownOnly = isPrimaryDropDownOnly.toString().toLowerCase() === "true";
    $.ajax({
        url: SafeUrls.getSearchSafeUrl(),
        method: "POST",
        data: {inactiveSafes: inactiveSafes, isDropdownOnly: isDropdownOnly},
        success: function (resp) {
            $('#results-container').html(resp);
            $('#safeSearchTerm').data('prev', $('#memberOfferSearchTerm').val());
        },
        error: function (resp) {
            var errorMessage = resp.responseJSON && resp.responseJSON.message ? resp.responseJSON.message : "Safe search failed.";
            displayMessage('error', errorMessage);
            document.getElementById('alerts-success-container-message').style.display = 'none';
        }
    })
}

function validateAndSave() {
    var error = false;
    var errorString = "";

    var description = $('#description').val();
    if (description === "" || description.trim() === "") {
        error = true;
        errorString = errorString.concat("\nDescription can not be empty");
    }

    var type = $('#type').val();
    if (type === "" || type.trim() === "") {
        error = true;
        errorString = errorString.concat("\nSafe type can not be empty. Please select type");
    }

    var shiftStatus = $('input[name="active"]:checked').val();
    if (shiftStatus === undefined) {
        error = true;
        errorString = errorString.concat("\nSafe status must be selected");
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

function handleCancelAddShift(url) {
    confirmAndSubmit("Are you sure you want to cancel ?", function() {
        cancelAddShiftView(url);
    });
}

function cancelAddShiftView(url) {
    var tempLink = document.createElement('a'); // Create a temporary anchor element
    tempLink.href = url;
    addInactiveSafesParam(tempLink); // Use addInactiveSafesParam to modify the URL
    document.location.href = tempLink.href; // Navigate to the modified URL
}

function addInactiveSafesParam(link) {
    //var inactiveSafes = $('#inactiveSafes').prop("checked");
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
    searchSafe(null, false);
}

function confirmAndSubmit(message, yesCallBack) {
    let result = confirm(message);
    if (result) {
        yesCallBack();
    }
}
