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