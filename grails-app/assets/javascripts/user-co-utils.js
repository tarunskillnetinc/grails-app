function validateAndSubmitUserEdit(isLoggedInFromStoreLevel, defaultStoreId) {
    clearFlashMessages();
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
    clearFlashMessages();
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
    clearFlashMessages();
    updateTextField(selectElement, 'storeIdInput');
    updateHiddenField(selectElement);
}

function updateTextField(selectElement, elementId) {
    var selectedText = selectElement.options[selectElement.selectedIndex].text;
    document.getElementById(elementId).value = selectedText;
}

function updateHiddenField(selectElement) {
    document.getElementsByName('defaultStoreId')[0].value = selectElement.value;
}

function clearFlashMessages() {
    $('#alert-success').remove();
    $('#errors-container').remove();
}