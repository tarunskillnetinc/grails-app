<div class="row mx-5 pt-2 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="addVariant(${index});">
    <g:hiddenField name="variants[${index}].id" value="${variant?.id}" />
    <g:hiddenField name="variants[${index}].itemCode" value="${variant?.itemCode}" />
    <g:hiddenField name="variants[${index}].retailPrice" value="${variant?.retailPrice}" />
    <g:hiddenField name="variants[${index}].costPrice" value="${variant?.costPrice}" />
    <g:hiddenField name="variants[${index}].size" value="${variant?.size}" />
    <g:hiddenField name="variants[${index}].colour" value="${variant?.colour}" />
    <g:hiddenField name="variants[${index}].effectiveDate" value="${variant?.effectiveDate}" />

    <div class="col-2 my-auto" id="variants[${index}].itemCodeText">${variant?.itemCode}</div>
    <div class="col-2 my-auto" id="variants[${index}].retailPriceText"><g:formatNumber number="${variant?.retailPrice}" type="currency" /></div>
    <div class="col-2 my-auto" id="variants[${index}].costPriceText"><g:formatNumber number="${variant?.costPrice}" type="currency" /></div>

    <div id="variants[${index}].barcodesContainer" class="col-2 my-auto">
        <g:each in="${variant?.barcodes}" var="barcode" status="i">
            <div id="barcodeContainer${i}">
                <g:render template="barcode" model="[variantIndex: index, barcodeIndex: i, barcode: barcode]" />
            </div>
        </g:each>
    </div>

    <div id="variants[${index}].packsContainer" class="col-2 my-auto">
        <g:render template="packs" model="[variantIndex: index, packs: variant?.packs]" />
    </div>

    <div class="col-2 my-auto text-right">
        <a href="#" onclick="event.stopPropagation(); showSuppliersModal(${index});" class="btn btn-wl">Suppliers</a>
        <a href="#" onclick="event.stopPropagation(); deleteVariant(${index});" class="btn btn-danger">Delete SKU</a>
    </div>
</div>