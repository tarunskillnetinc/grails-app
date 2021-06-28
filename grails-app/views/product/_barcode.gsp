<g:hiddenField name="variants[${variantIndex}].barcodes[${barcodeIndex}].id" value="${barcode?.id ?: ''}" />
<g:hiddenField name="variants[${variantIndex}].barcodes[${barcodeIndex}].barcode" value="${barcode.barcode}" />
<g:hiddenField name="variants[${variantIndex}].barcodes[${barcodeIndex}].recordStatus" value="${barcode.recordStatus}" />
<g:hiddenField name="variants[${variantIndex}].barcodes[${barcodeIndex}].effectiveDate" value="${barcode.effectiveDate}" />

<div id="variants[${variantIndex}].barcodes[${barcodeIndex}].barcodeText">${barcode.barcode}</div>