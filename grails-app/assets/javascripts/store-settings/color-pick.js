function onColorPickerValueChange(event, val, targetId) {
    event.stopPropagation();

    let targetField = $('#' + targetId);

    if (val) {
        targetField.val(val.substring(1, 7));
    }
}

function onTextFieldChange(event, colorString, targetId) {
    event.stopPropagation();

    if (!colorString && !targetId && colorString == "") {
        return;
    }

    colorString = "#" + colorString;

    if (isValidColour(colorString)) {
        let targetField = $('#' + targetId);
        targetField.val(colorString);
    }
}

function isValidColour(colorString) {
    return CSS.supports('color', colorString);
}