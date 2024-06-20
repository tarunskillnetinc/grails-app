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
function validateInput(input){
    // Remove leading minus sign if present
    input.value = input.value.replace(/^-/, '');

    // Ensure the value is greater than or equal to 0
    if (parseInt(input.value, 10) < 0 || input.value === '-') {
        input.value = 0;
    }
}