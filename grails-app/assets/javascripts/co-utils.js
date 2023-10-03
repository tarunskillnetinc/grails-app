function intListener(elementId, maxLength = 9, maxValue = 999999999) {
    var element = document.getElementById(elementId)

    if (element != null) {
        element.addEventListener("input", function () {
            if (element.value.length > maxLength) {
                element.value = element.value.slice(0, maxLength)
            }
            if (element.value > maxValue) {
                element.value = maxValue
            }
        });
    }
}

function currencyListener(elementId, minValue = 0, maxValue = 999999999) {
    var element = $('#' + elementId);

    if (element != null) {
        element.on("keyup", function () {
            const unmaskedNumber = parseFloat(element.maskMoney('unmasked')[0]);
            if (unmaskedNumber < minValue) {
                element.val(minValue)
            }
            if (unmaskedNumber > maxValue) {
                element.val(maxValue)
            }
            element.maskMoney('mask')
        });
    }
}