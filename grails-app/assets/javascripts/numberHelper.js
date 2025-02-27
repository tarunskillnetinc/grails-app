function enforceDecimalLimit(element, decimalPlaces, max) {
    const decimalPlaceIndex = element.value.indexOf(".");
    if (decimalPlaceIndex === -1) {
        return;
    }
    // If greater than max number set to max
    if (max && parseFloat(element.value) > max) {
        element.value = max
    } else if (element.value.substring(decimalPlaceIndex, element.value.length).length > decimalPlaces) {
        let endSubstringIndex = decimalPlaceIndex + decimalPlaces;
        if (decimalPlaces > 0) {
            endSubstringIndex++;
        }
        // Truncate to number of decimal places and return
        element.value = element.value.substring(0, endSubstringIndex);
    } else if (element.value < 0.001) {
        // If the value is less than 0.001 or not valid then set it back to what it was before
        element.value = element.defaultValue
    }
}