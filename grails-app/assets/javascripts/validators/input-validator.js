function numericOnly(event) {
    if (["-", "+", "e", "E", "."].includes(event.key)) {
        return false;
    }
}

function preventNegativeInteger(event) {
    if (!(event.key in ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'])){
        return false;
    }
}

function preventOverflowValue(obj) {
    if (obj.value > Math.pow(2, 31) -1) {
        obj.value = Math.pow(2, 31) -1;
    }
}

function acceptNumeric(e) {
    if (typeof e.key !== 'string' || e.key.length !== 1 || (e.key >= '0' && e.key <= '9')) {
        return;
    }
    e.preventDefault();
}

function acceptDecimal(e) {
    if ((e.key < '0' && e.key > '9') && e.key !== '.') {
        e.preventDefault();
    }
}

function validateDecimal(e, min, max, decimalPlaces) {
    const splits = e.value.split('.');
    if (splits.length > 2) {
        e.value = splits[0] + '.' + splits[1];
        return;
    } else if (splits.length === 2 && splits[1].length > decimalPlaces) {
        e.value = splits[0] + '.' + splits[1].substring(0, decimalPlaces);
    }

    const val = parseFloat(e.value);
    if (isNaN(val) || val < min) {
        e.value = min;
    } else if (val > max || !isFinite(val)) {
        e.value = max;
    }
}

function validateInput(input){
    // Remove leading minus sign if present
    input.value = input.value.replace(/^-/, '');

    // Ensure the value is greater than or equal to 0
    if (parseInt(input.value, 10) < 0 || input.value === '-') {
        input.value = 0;
    }
}