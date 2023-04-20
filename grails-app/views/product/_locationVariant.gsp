<div class="row mx-5 pt-3 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;">
    <g:hiddenField name="variants[${index}].id" value="${variant?.id ?: ''}"/>
    <g:hiddenField name="variants[${index}].sku" value="${variant?.sku ?: 0}"/>
    <g:hiddenField name="variants[${index}].retailPrice" value="${variant?.retailPrice}"/>
    <g:hiddenField name="variants[${index}].costPrice" value="${variant?.costPrice}"/>
    <g:hiddenField name="variants[${index}].shelfLifeDays" value="${variant?.shelfLifeDays}"/>
    <g:hiddenField name="variants[${index}].shelfCapacity" value="${variant?.shelfCapacity}"/>
    <g:hiddenField name="variants[${index}].minimumDisplayQuantity" value="${variant?.minimumDisplayQuantity}"/>
    <g:hiddenField name="variants[${index}].effectiveDate" value="${variant?.effectiveDate}"/>

    <div class="col-5 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>

    <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">
        <div id="variants[${index}].locationsContainer" class="col-5 my-auto">
            <g:render template="locationz" model="[variantIndex: index, locations: variant?.locations]"/>
        </div>
    </g:if>

    <div class="col-2 my-auto text-right">
        <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">
            <a id="variant-${index}-locations-btn" href="#" onclick="event.stopPropagation(); showLocationsModal(${index});" class="btn btn-wl btn-">Add / Edit Locations</a>
        </g:if>
    </div>
</div>