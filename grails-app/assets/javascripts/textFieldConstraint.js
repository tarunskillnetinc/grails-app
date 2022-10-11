function preventNegativeInteger(evt) {
    if (evt.key === "-") {
        return false;
    } else if (evt.key === ".") {
        return false;
    } else {
        return true;
    }
}

function limit(val, len) {
    if (val.value.length > len) {
        val.value = val.value.slice(0, len);
    }
}