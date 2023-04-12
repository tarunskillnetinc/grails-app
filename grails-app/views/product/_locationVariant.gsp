<div class="row mx-5 pt-3 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="event.stopPropagation(); showLocationsModal(${index});">
%{--<div class="row mx-5 pt-2 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="addVariant(${index});">--}%
    <g:hiddenField name="variants[${index}].id" value="${variant?.id ?: ''}" />
    <g:hiddenField name="variants[${index}].sku" value="${variant?.sku ?: 0}" />
    <g:hiddenField name="variants[${index}].retailPrice" value="${variant?.retailPrice}" />
    <g:hiddenField name="variants[${index}].costPrice" value="${variant?.costPrice}" />
    <g:hiddenField name="variants[${index}].shelfLifeDays" value="${variant?.shelfLifeDays}" />
    <g:hiddenField name="variants[${index}].shelfCapacity" value="${variant?.shelfCapacity}" />
    <g:hiddenField name="variants[${index}].minimumDisplayQuantity" value="${variant?.minimumDisplayQuantity}" />
    <g:hiddenField name="variants[${index}].effectiveDate" value="${variant?.effectiveDate}" />

    <div class="col-6" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>

%{--    <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">--}%
%{--        <div class="col-1 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>--}%
%{--        <div class="col-2 my-auto" id="variants[${index}].retailPriceText"><g:formatNumber number="${variant?.currentPrice}" type="currency" /> (${variant?.retailPrice ? "store override" : "price band"})</div>--}%
%{--        <div class="col-1 my-auto" id="variants[${index}].costPriceText"><g:formatNumber number="${variant?.costPrice}" type="currency" /></div>--}%
%{--    </g:if >--}%
%{--    <g:else>--}%
%{--        <div class="col-2 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>--}%
%{--        <div class="col-2 my-auto" id="variants[${index}].retailPriceText"><g:formatNumber number="${variant?.currentPrice}" type="currency" /> (${variant?.retailPrice ? "store override" : "price band"})</div>--}%
%{--        <div class="col-2 my-auto" id="variants[${index}].costPriceText"><g:formatNumber number="${variant?.costPrice}" type="currency" /></div>--}%
%{--    </g:else>--}%

%{--    <div id="variants[${index}].barcodesContainer" class="col-2 my-auto">--}%
%{--        <g:each in="${barcodes}" var="barcode" status="i">--}%
%{--            <div id="barcodeContainer${i}">--}%
%{--                <g:render template="barcode" model="[variantIndex: index, barcodeIndex: i, barcode: barcode]" />--}%
%{--            </div>--}%
%{--        </g:each>--}%
%{--    </div>--}%

%{--    <div id="variants[${index}].packsContainer" class="col-2 my-auto">--}%
%{--        <g:render template="packs" model="[variantIndex: index, packs: variant?.packs, defaultSupplier:variant?.defaultSupplierId]" />--}%
%{--    </div>--}%

    <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">
        <div id="variants[${index}].locationsContainer" class="col-6 my-auto">
            <g:render template="locationz" model="[variantIndex: index, locations: variant?.locations]" />
        </div>
%{--        <g:render template="locationz" model="[variantIndex: index, locations: variant?.locations]" />--}%
    </g:if>

%{--    <div class="col-8 my-auto text-right">--}%
%{--        <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">--}%
%{--            <a id="variant-${index}-locations-btn" href="#" onclick="event.stopPropagation(); showLocationsModal(${index});" class="btn btn-wl btn-">Locations</a>--}%
%{--        </g:if >--}%
%{--    </div>--}%
</div>