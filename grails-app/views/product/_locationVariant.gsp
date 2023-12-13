<%@ page import="uk.co.wonderlane.wlpos.Location" %>


<div class="row mx-5 pt-3 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;">

    <g:if test="${locationsType === 'ADVANCED'}">
        <div class="col-2 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>
    </g:if>
    <g:else>
        <div class="col-5 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>
    </g:else>

    <g:if test="${storeId != null && locationsEnabled}">
        <div id="variants[${index}].locationsContainer" class="col w-100">
            <g:render template="locationz" model="[variantIndex: index, locations: locations, locationsType: locationsType, locationHierarchy  : locationHierarchy]"/>
        </div>
    </g:if>

    <div class="col-2 my-auto text-right">
        <g:if test="${storeId != null && locationsEnabled}">
            <a id="variant-${index}-locations-btn" href="#" onclick="event.stopPropagation(); showLocationsModal(${index}, ${variant?.sku});" class="btn btn-wl btn-">Add / Edit Locations</a>
        </g:if>
    </div>
</div>