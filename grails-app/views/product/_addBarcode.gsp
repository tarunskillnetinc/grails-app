<g:hiddenField name="addVariantBarcodes[${index}].id" value="${barcode?.id ?: ''}" />
<g:hiddenField name="addVariantBarcodes[${index}].effectiveDate" value="${barcode?.effectiveDate}" />
<g:hiddenField name="addVariantBarcodes[${index}].recordStatus" value="${barcode?.recordStatus ?: 'C'}" />

<g:textField name="addVariantBarcodes[${index}].barcode" value="${barcode?.barcode}" class="form-control bottom-border" />