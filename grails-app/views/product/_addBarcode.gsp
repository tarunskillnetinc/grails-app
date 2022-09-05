<g:hiddenField name="addVariantBarcodes[${index}].id" value="${barcode?.id ?: ''}" />
<g:hiddenField name="addVariantBarcodes[${index}].effectiveDate" value="${barcode?.effectiveDate}" />
<g:hiddenField name="addVariantBarcodes[${index}].recordStatus" value="${barcode?.recordStatus ?: 'C'}" />

<g:textField name="addVariantBarcodes[${index}].barcode" value="${barcode?.barcode}" class="form-control bottom-border" onkeyup="toggleDisalbled(this.value, ${index})" />
<span class="toggleDisabled${index}">
    <a href="#" onclick="event.stopPropagation(); deleteBarcode(${index});" class="input-group-append btn btn-danger">Delete Barcode</a>
</span>
<script>
    $(function() {
        if (document.getElementsByName("addVariantBarcodes[${index}].barcode").value == null ||
            document.getElementsByName("addVariantBarcodes[${index}].barcode").value == '' ) {
            $(".toggleDisabled${index}").find("a").addClass("disabled");
        }
    });
    function toggleDisalbled(val, index) {
        if (val == null || val == '') {
            $(".toggleDisabled"+index).find("a").addClass("disabled");
        } else {
            $(".toggleDisabled"+index).find("a").removeClass("disabled");
        }
    }
</script>