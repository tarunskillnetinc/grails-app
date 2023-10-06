<asset:javascript src="validators/input-validator.js" />

<div id="addLocationTextContainer-${variantIndex}-${locationIndex}" class="row mx-4 pt-2 pb-2 ${isNewLocation ? 'hidden' : ''} wl-striped${locationIndex % 2}">
    <g:if test="${locationsType == "SIMPLE"}">
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-location" class="col-6 my-auto">${location?.location}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-position" class="col-2 my-auto">${location?.shelfCapacity}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-position" class="col-2 my-auto">${location?.minimumDisplayQuantity}</div>
    </g:if>
    <g:else>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-aisle" class="col-2 my-auto">${location?.aisle}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-bay" class="col-2 my-auto">${location?.bay}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-shelf" class="col-2 my-auto">${location?.shelf}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-position" class="col-2 my-auto">${location?.position}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-position" class="col-1 my-auto">${location?.shelfCapacity}</div>
        <div id="add-location-${variantIndex+1}-${locationIndex+1}-position" class="col-1 my-auto">${location?.minimumDisplayQuantity}</div>
    </g:else>
    <div class="col-2 text-right">
        <g:if test="${location == null}">
            <button id="add-location-${variantIndex+1}-${locationIndex+1}-delete-btn" class="btn btn-danger disabled" title="You cannot delete locations.">Delete</button>
        </g:if>
        <g:else>
            <button id="add-location-${variantIndex+1}-${locationIndex+1}-edit-btn" href="#" class="btn btn-wl" onclick="addLocation(${variantIndex}, ${locationIndex}, ${productVariantId});">Edit</button>
            <button id="add-location-${variantIndex+1}-${locationIndex+1}-delete-btn" href="#" class="btn btn-danger" onclick="deleteLocation(${variantIndex}, ${locationIndex});">Delete</button>
        </g:else>
    </div>
</div>

<div id="addLocationFieldsContainer-${variantIndex}-${locationIndex}" class="${!isNewLocation ? 'hidden' : ''}">
    <g:hiddenField name="addLocation[${locationIndex}].id" value="${location?.id ?: ''}" />
    <g:hiddenField name="addLocation[${locationIndex}].productVariantId" value="${productVariantId}" />
    <g:hiddenField name="addLocation[${locationIndex}].storeId" value="${location?.storeId}" />
    <g:hiddenField name="addLocation[${locationIndex}].sku" value="${location?.sku}" />

    <div class="row mx-4 pt-2 wl-striped${locationIndex % 2}">
        <g:if test="${locationsType == "SIMPLE"}">
            <div class="col-6 my-auto">
                <g:textField name="addLocation[${locationIndex}].location" value="${location?.location}" class="form-control bottom-border" minlength="1" maxlength="40"/>
            </div>
            <div class="col-2 my-auto">
                <g:textField name="addLocation[${locationIndex}].shelfCapacity" value="${location?.shelfCapacity}" class="form-control bottom-border" minlength="1" maxlength="3" onkeypress="return preventNegativeInteger(event);" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;" onkeyup="preventOverflowValue(this)" />
            </div>
            <div class="col-2 my-auto">
                <g:textField name="addLocation[${locationIndex}].minimumDisplayQuantity" value="${location?.minimumDisplayQuantity}" class="form-control bottom-border" minlength="1" maxlength="3" onkeypress="return preventNegativeInteger(event);" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;" onkeyup="preventOverflowValue(this)" />
            </div>
        </g:if>
        <g:else>
            <div class="col-2 my-auto">
                <g:textField name="addLocation[${locationIndex}].aisle" value="${location?.aisle}" class="form-control bottom-border" minlength="1" maxlength="10"/>
            </div>
            <div class="col-2 my-auto">
                <g:textField name="addLocation[${locationIndex}].bay" value="${location?.bay}" class="form-control bottom-border" minlength="1" maxlength="10" />
            </div>
            <div class="col-2 my-auto">
                <g:textField name="addLocation[${locationIndex}].shelf" value="${location?.shelf}" class="form-control bottom-border" minlength="1" maxlength="10" />
            </div>
            <div class="col-2 my-auto">
                <g:textField name="addLocation[${locationIndex}].position" value="${location?.position}" class="form-control bottom-border" minlength="1" maxlength="10" />
            </div>
            <div class="col-1 my-auto">
                <g:textField name="addLocation[${locationIndex}].shelfCapacity" value="${location?.shelfCapacity}" class="form-control bottom-border" minlength="1" maxlength="3" onkeypress="return preventNegativeInteger(event);" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;" onkeyup="preventOverflowValue(this)" />
            </div>
            <div class="col-1 my-auto">
                <g:textField name="addLocation[${locationIndex}].minimumDisplayQuantity" value="${location?.minimumDisplayQuantity}" class="form-control bottom-border" minlength="1" maxlength="3" onkeypress="return preventNegativeInteger(event);" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;" onkeyup="preventOverflowValue(this)" />
            </div>
        </g:else>
        <div class="col-2 text-right">
            <button id="add-location-${variantIndex+1}-${locationIndex+1}-delete-btn" href="#" class="btn btn-danger" onclick="deleteLocation(${variantIndex}, ${locationIndex});">Delete</button>
        </div>
    </div>
</div>
