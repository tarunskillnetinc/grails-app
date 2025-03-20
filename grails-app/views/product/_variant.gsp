<div class="row mx-5 pt-2 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="addVariant(${index}, false);">
    <g:hiddenField name="variants[${index}].id" value="${variant?.id ?: ''}" />
    <g:hiddenField name="variants[${index}].storeId" value="${variant?.storeId ?: ''}" />
    <g:hiddenField name="variants[${index}].sku" value="${variant?.sku ?: 0}" />
    <g:hiddenField name="variants[${index}].retailPrice" value="${variant?.retailPrice}" />
    <g:hiddenField name="variants[${index}].costPrice" value="${variant?.costPrice}" />
    <g:hiddenField name="variants[${index}].weightedAverageCostPrice" value="${variant?.weightedAverageCostPrice}" />
    <g:hiddenField name="variants[${index}].shelfLifeDays" value="${variant?.shelfLifeDays}" />
    <g:hiddenField name="variants[${index}].shelfCapacity" value="${variant?.shelfCapacity}" />
    <g:hiddenField name="variants[${index}].minimumDisplayQuantity" value="${variant?.minimumDisplayQuantity}" />
    <g:hiddenField name="variants[${index}].description" value="${variant?.description}" />
    <g:hiddenField name="variants[${index}].receiptDescription" value="${variant?.receiptDescription}" />
    <g:hiddenField name="variants[${index}].priceMarked" value="${variant?.priceMarked}" />
    <g:hiddenField name="variants[${index}].unitSize" value="${variant?.unitSize}" />
    <g:hiddenField name="variants[${index}].unitOfMeasure" value="${variant?.unitOfMeasure?.id}" />
    <g:hiddenField name="variants[${index}].itemsInUnit" value="${variant?.itemsInUnit}" />
    <g:hiddenField name="variants[${index}].heightCm" value="${variant?.heightCm}" />
    <g:hiddenField name="variants[${index}].widthCm" value="${variant?.widthCm}" />
    <g:hiddenField name="variants[${index}].depthCm" value="${variant?.depthCm}" />
    <g:hiddenField name="variants[${index}].effectiveDate" value="${variant?.effectiveDate}" />
    <g:hiddenField name="variants[${index}].preferredSku" value="${variant?.preferredSku}" />
    <g:hiddenField name="variants[${index}].stockManagementType" value="${variant?.stockManagementType}" />
    <g:hiddenField name="variants[${index}].minAlcoholUnitPrice" value="${variant?.minAlcoholUnitPrice}" />

    <div class="col-2 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>
    <div class="col-2 my-auto" id="variants[${index}].retailPriceText"><g:formatNumber number="${variant?.currentPrice}" type="currency" /> (${variant?.retailPrice ? "store override" : "price band"})</div>
    <div class="col-2 my-auto" id="variants[${index}].costPriceText"><g:formatNumber number="${variant?.costPrice}" type="currency" /></div>

    <div id="variants[${index}].barcodesContainer" class="col-2 my-auto">
        <g:each in="${barcodes}" var="barcode" status="i">
            <div id="barcodeContainer${i}">
                <g:render template="barcode" model="[variantIndex: index, barcodeIndex: i, barcode: barcode]" />
            </div>
        </g:each>
    </div>

    <div id="variants[${index}].packsContainer" class="col-2 my-auto">
        <g:render template="packs" model="[variantIndex: index, packs: variant?.packs, defaultSupplier:variant?.defaultSupplierId]" />
    </div>

    <div id="variants[${index}].preferredSku" class="col-1 my-auto">
        ${variant?.preferredSku ? 'Yes' : ''}
    </div>

    <div class="col-1 my-auto text-right">
        <a id="variant-${index}-suppliers-btn" href="#" onclick="event.stopPropagation(); showSuppliersModal(${index});" class="btn btn-wl">Suppliers</a>
    </div>
</div>