function validateAndSubmitUserEdit(isLoggedInFromStoreLevel, defaultStoreId) {
    clearOldFlashMessages();
    let selectElement = document.getElementById('defaultStoreIdSelector');
    let selectedId = selectElement.value;
    if (isLoggedInFromStoreLevel && (selectedId.toString() !== defaultStoreId.toString())) {
        // Show confirmation dialog
        if (confirm("Changing the Home Store will prevent you from editing this user’s details from the current store.")) {
            // User clicked OK, submit the form
            $('#edit-user-form').submit();
        } else {
            // User clicked Cancel, do nothing
            return;
        }
    } else {
        // No change in store or not logged in from store level, submit the form
        $('#edit-user-form').submit();
    }
}

function filter(inputName, dropDownName) {
    clearOldFlashMessages();
    var keyword = document.getElementById(inputName).value.toLowerCase();
    var select = document.getElementById(dropDownName);
    for (var i = 0; i < select.length; i++) {
        var txt = select.options[i].text.toLowerCase();
        if (!txt.match(keyword)) {
            $(select.options[i]).attr('disabled', 'disabled').hide();
        } else {
            $(select.options[i]).removeAttr('disabled').show();
        }
    }
}

function updateFields(selectElement) {
    clearOldFlashMessages();
    // Update the selected store display field
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    if (selectedOption && selectedOption.value !== '0') {
        updateHiddenFieldValue(selectedOption.value);
        document.getElementById('selectedStoreDisplay').value = selectedOption.text;
    } else {
        updateHiddenFieldValue('0');
        document.getElementById('selectedStoreDisplay').value = 'No Home Store';
    }
}

function updateHiddenFieldValue(elementValue) {
    document.getElementsByName('defaultStoreId')[0].value = elementValue
}

function clearStoreSearch() {
    clearOldFlashMessages();

    // Clear the hidden field value
    document.getElementById('defaultStoreId').value = '0';

    // Update the display field
    document.getElementById('selectedStoreDisplay').value = 'No Home Store';

    // Reset the dropdown selection if needed
    const dropdown = document.getElementById('defaultStoreIdSelector');
    dropdown.selectedIndex = -1;
}

function clearFlashMessages() {
    $('#alert-success').empty();
    $('#errors-container').empty();
}

function clearOldFlashMessages() {
    ['alert-success', 'errors-container'].forEach(function(id) {
        var element = document.getElementById(id);
        if (element && element.getAttribute('data-flash-message') === 'true') {
            element.textContent = '';
            element.style.display = 'none';
            element.setAttribute('data-flash-message', 'false');
        }
    });
}