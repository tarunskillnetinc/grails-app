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