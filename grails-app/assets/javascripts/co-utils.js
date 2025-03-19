function intListener(elementId, maxLength = 9, maxValue = 999999999, textField = false) {
    var element = document.getElementById(elementId)

    if (element != null) {
        element.addEventListener("input", function () {
            if (textField) {
                var valueString = String(element.value)
                valueString = valueString.match(/-?\d*/)
                element.value = valueString
            }
            if (element.value.length > maxLength) {
                element.value = element.value.slice(0, maxLength)
            }
            if (element.value > maxValue) {
                element.value = maxValue
            }
        });
    }
}

function filter(inputName, dropDownName) {
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

function updateTextField(selectElement, elementId) {
    var selectedText = selectElement.options[selectElement.selectedIndex].text;
    document.getElementById(elementId).value = selectedText;
}