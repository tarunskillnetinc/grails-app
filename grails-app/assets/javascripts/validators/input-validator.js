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

function acceptNumericInt(e) {
    const maxValue = Number.MAX_SAFE_INTEGER;

    acceptMaxNumberValue(e, maxValue);
}

function acceptMaxNumberValue(e, maxValue) {
    // Allow digits, backspace, and arrow keys without further checks
    if (e.key === 'Backspace' || e.key === 'Delete') {
        return;
    } else if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
        e.preventDefault();
    }

    // Check if the key pressed is a digit
    if (e.key >= '0' && e.key <= '9') {
        // Construct the potential new value by adding the typed digit
        const newValue = parseInt(e.target.value + e.key, 10);

        // Check if the new value exceeds the maximum allowed value
        if (newValue > maxValue) {
            e.preventDefault(); // Prevent the key press if it exceeds the maximum value
        }
    } else {
        e.preventDefault(); // Prevent non-digit characters
    }
}

function acceptMinMaxNumberValue(e, minValue, maxValue) {
    // Allow digits, backspace, and arrow keys without further checks
    if (e.key === 'Backspace' || e.key === 'Delete') {
        return;
    } else if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
        e.preventDefault();
    }

    // Check if the key pressed is a digit
    if (e.key >= '0' && e.key <= '9') {
        // Construct the potential new value by adding the typed digit
        const newValue = parseInt(e.target.value + e.key, 10);

        // Check if the new value exceeds the maximum allowed value
        if (newValue > maxValue || newValue < minValue) {
            e.preventDefault(); // Prevent the key press if it exceeds the maximum value
        }
    } else {
        e.preventDefault(); // Prevent non-digit characters
    }
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

function validateFloatQuantity(input, min, max, precision) {
    const splits = input.value.split('.');
    if (splits.length > 2) {
        input.value = splits[0] + '.' + splits[1];
        return;
    } else if (splits.length === 2 && splits[1].length > precision) {
        input.value = splits[0] + '.' + splits[1].substring(0, precision);
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
        validateFloatQuantity(input, min, max, 3);
        return;
    }
    validateIntQuantity(input, min, max);
}

function validateInput(input){
    // Remove leading minus sign and leading zeros
    input.value = input.value.replace(/^-|^0+(?=\d)/, '');

    // Ensure the value is greater than or equal to 0
    if (parseInt(input.value, 10) < 0 || input.value === '-') {
        input.value = 0;
    }
}
