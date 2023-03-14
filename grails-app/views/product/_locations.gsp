<div class="modal-header">
    <h2>Locations</h2>
</div>

<div class="modal-body">

    <div class="row mx-4 pt-3 pb-2 table-wl bottom-border">
        <div class="col-2 font-weight-bold">Location</div>
        <div class="col-2 font-weight-bold">Aisle</div>
        <div class="col-2 font-weight-bold">Bay</div>
        <div class="col-2 font-weight-bold">Shelf</div>
        <div class="col-2 font-weight-bold">Position</div>
        <div class="col-1 font-weight-bold">&nbsp;</div>
    </div>

    <div id="addLocationsContainer-${variantIndex}">
%{--        <g:each in="${variant.locations}" var="location" status="i">--}%
        <g:each in="${locations}" var="location" status="i">
            <div id="addLocationContainer-${variantIndex}-${i}">
                <g:render template="addLocation" model="[variantIndex: variantIndex, productVariantId: variant.productVariantId, locationIndex: i, location: location, isNewLocation: isNewLocation, locations: locations]" />
            </div>
        </g:each>
    </div>

    <div class="row mx-4 mt-3">
        <a id="add-location-btn" href="#" onclick="addLocation(${variantIndex}, null, ${variant.productVariantId});" class="btn btn-wl">Add Location</a>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddVariantButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <button type="button" id="saveAddVariantButton" class="btn btn-success" onclick="saveLocations(${variantIndex});">Ok</button>
</div>

<asset:javascript src="validators/input-validator.js" />