<%@ page import="uk.co.wonderlane.wlpos.Location" %>
<div class="row mx-5 pt-3 pb-2 wl-striped${index % 2} hoverable" title="Click to edit." style="cursor: pointer;">

    <div class="col-5 my-auto" id="variants[${index}].skuText">${variant?.sku ?: 0}</div>

    <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">
        <div id="variants[${index}].locationsContainer" class="col-5 my-auto">
            <g:render template="locationz" model="[variantIndex: index, locations: locations]"/>
        </div>
    </g:if>

    <div class="col-2 my-auto text-right">
        <g:if test="${storeId != null && (locationsType == "SIMPLE" || locationsType == "ADVANCED")}">
            <a id="variant-${index}-locations-btn" href="#" onclick="event.stopPropagation(); showLocationsModal(${index}, ${variant?.sku});" class="btn btn-wl btn-">Add / Edit Locations</a>
        </g:if>
    </div>
</div>