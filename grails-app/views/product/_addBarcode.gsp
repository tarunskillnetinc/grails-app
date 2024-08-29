<g:hiddenField name="addVariantBarcodes[${index}].id" value="${barcode?.id ?: ''}" />
<g:hiddenField name="addVariantBarcodes[${index}].effectiveDate" value="${barcode?.effectiveDate}" />
<g:hiddenField name="addVariantBarcodes[${index}].recordStatus" value="${barcode?.recordStatus ?: 'C'}" />

<g:textField name="addVariantBarcodes[${index}].barcode" value="${barcode?.barcode}" class="form-control bottom-border" onkeyup="toggleDisable(${index})"/>
<a href="#" id="barcode-disable-btn-${index}" onclick="event.stopPropagation(); deleteBarcode(${index}, '${selector}');" class="input-group-append btn btn-danger">Delete Barcode</a>

<script>
    $(toggleDisable(${index}));

    function getButtonElement(index) {
        return $('#barcode-disable-btn-' + index);
    }

    function getBarcodeTextElement(index) {
        return $('[name="addVariantBarcodes[' + index + '].barcode"]');
    }

    // TODO: [YH] Move this to an util class
    function isEmpty(value) {
        return value == null || value == '' || value == undefined;
    }

    function toggleDisable(index) {
        if (isEmpty(getBarcodeTextElement(index).val())) {
            getButtonElement(index).addClass("disabled");
        } else {
            getButtonElement(index).removeClass("disabled");
        }
    }
</script>