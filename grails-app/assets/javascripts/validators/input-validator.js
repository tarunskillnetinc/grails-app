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

function validateIntQuantity(input, min, max) {
    let inputValue = parseInt(input.value);
    if (isNaN(inputValue) || inputValue < min) {
        input.value = min;
    } else if (inputValue > max) {
        input.value = max;
    }
}

function acceptFloat(e) {
    if (typeof e.key !== 'string' || e.key.length !== 1 || (e.key >= '0' && e.key <= '9') || e.key === '.') {
        return;
    }
    e.preventDefault();
}

function validateFloatQuantity(input, min, max) {
    const splits = input.value.split('.');
    if (splits.length > 2) {
        input.value = splits[0] + '.' + splits[1];
        return;
    } else if (splits.length === 2 && splits[1].length > 3) {
        input.value = splits[0] + '.' + splits[1].substring(0, 3);
    }

    const val = parseFloat(input.value);
    if (isNaN(val)) {
        return;
    }

    if (val < min) {
        input.value = min;
    } else if (val > max) {
        input.value = max;
    }
}

function acceptQuantity(event, weighted) {
    if (weighted) {
        acceptFloat(event);
        return;
    }
    acceptNumeric(event);
}

function validateQuantity(input, min, max, weighted) {
    if (weighted) {
        validateFloatQuantity(input, min, max);
        return;
    }
    validateIntQuantity(input, min, max);
}

function validateInput(input){
    // Remove leading minus sign if present
    input.value = input.value.replace(/^-/, '');

    // Ensure the value is greater than or equal to 0
    if (parseInt(input.value, 10) < 0 || input.value === '-') {
        input.value = 0;
    }
}