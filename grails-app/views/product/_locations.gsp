<div class="modal-header">
    <h2>Locations</h2>
</div>

<div class="modal-body">

    <g:if test="${locationsType == "SIMPLE"}">
        <div class="row mx-4 pt-3 pb-2 table-wl bottom-border">
            <div class="col-6 font-weight-bold">Location</div>
            <div class="col-2 font-weight-bold">Shelf Capacity</div>
            <div class="col-2 font-weight-bold">Minimum Display Quantity</div>
        </div>
    </g:if>
    <g:else>
        <div class="row mx-4 pt-2 pb-2 table-wl bottom-border">
            <div class="col-2 my-auto font-weight-bold">Aisle</div>
            <div class="col-2 my-auto font-weight-bold">Bay</div>
            <div class="col-2 my-auto font-weight-bold">Shelf</div>
            <div class="col-2 my-auto font-weight-bold">Position</div>
            <div class="col-1 my-auto font-weight-bold">Shelf Capacity</div>
            <div class="col-1 my-auto font-weight-bold">Minimum Display Quantity</div>
        </div>
    </g:else>

    <div id="addLocationsContainer-${variantIndex}">
        <g:each in="${variant.locationz}" var="location" status="i">
            <div id="addLocationContainer-${variantIndex}-${i}">
                <g:render template="addLocation" model="[variantIndex: variantIndex, productVariantId: variant.productVariantId, locationIndex: i, location: location, isNewLocation: location?.isNewLocation, locationsType: locationsType]" />
            </div>
        </g:each>
    </div>

    <div class="row mx-4 mt-3">
        <a id="add-location-btn" href="#" onclick="addLocation(${variantIndex}, null, ${variant.productVariantId});" class="btn btn-wl">Add Location</a>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddVariantButton" class="btn btn-secondary" onclick="cancelLocations();">Cancel</button>
    <button type="button" id="saveAddVariantButton" class="btn btn-success" onclick="saveLocations(${variantIndex}, '${locationsType}');">Ok</button>
</div>

<asset:javascript src="validators/input-validator.js" />