<g:hiddenField name="variants[${variantIndex}].defaultSupplierId" value="${defaultSupplier}" />

<g:each in="${packs}" var="pack" status="packIndex">
    <g:if test="${pack.isActive()}">
        <div id="packContainer${packIndex}">
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].id" value="${pack.id ?: ''}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].supplier.id" value="${pack.supplier.id}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].supplier.name" value="${pack.supplier.name}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].supplier.symbolGroupId" value="${pack.supplier.symbolGroupId}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].quantity" value="${pack.quantity}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].price" value="${pack.price}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].orderCode" value="${pack.orderCode}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].recommendedRetailPrice" value="${pack.recommendedRetailPrice}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].effectiveDate" value="${pack.effectiveDate}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].effectiveEndDate" value="${pack.effectiveEndDate}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].status" value="${pack.status}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].maximumOrderQuantity" value="${pack.maximumOrderQuantity}" />
            <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].allowSubstitutes" value="${pack.allowSubstitutes}" />

            <div id="variants[${variantIndex}].packs[${packIndex}].barcodesContainer" class="col-2 my-auto">
%{--            // TODO look into why this is failing--}%
                <g:each in="${pack.barcodez? pack.barcodez : pack.hasProperty("barcodes") ? pack.barcodes : ""}" var="barcode" status="barcodeIndex">
                    <div id="barcodeContainer${barcodeIndex}">
                        <g:hiddenField name="variants[${variantIndex}].packs[${packIndex}].barcodez[${barcodeIndex}].id"
                                       value="${barcode.id}"/>
                        <g:hiddenField
                                name="variants[${variantIndex}].packs[${packIndex}].barcodez[${barcodeIndex}].barcode"
                                value="${barcode.barcode}"/>
                        <g:hiddenField
                                name="variants[${variantIndex}].packs[${packIndex}].barcodez[${barcodeIndex}].effectiveDate"
                                value="${barcode.effectiveDate}"/>
                        <g:hiddenField
                                name="variants[${variantIndex}].packs[${packIndex}].barcodez[${barcodeIndex}].recordStatus"
                                value="${barcode.recordStatus}"/>
                    </div>
                </g:each>
            </div>
            <div class="text-truncate" id="variants[${variantIndex}].packs[${packIndex}].packText">${pack.quantity} @ <g:formatNumber number="${pack.price}" type="currency" /> (${pack.supplier.name})</div>
        </div>
    </g:if>
</g:each>