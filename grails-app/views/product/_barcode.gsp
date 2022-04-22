<g:hiddenField name="variants[${variantIndex}].barcodez[${barcodeIndex}].id" value="${barcode?.id ?: ''}" />
<g:hiddenField name="variants[${variantIndex}].barcodez[${barcodeIndex}].barcode" value="${barcode.barcode}" />
<g:hiddenField name="variants[${variantIndex}].barcodez[${barcodeIndex}].recordStatus" value="${barcode.recordStatus}" />
<g:hiddenField name="variants[${variantIndex}].barcodez[${barcodeIndex}].effectiveDate" value="${barcode.effectiveDate}" />

<div id="variants[${variantIndex}].barcodez[${barcodeIndex}].barcodeText">${barcode.barcode}</div>