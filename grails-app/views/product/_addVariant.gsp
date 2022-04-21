<div class="modal-header">
    <h2>Add SKU</h2>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please complete the following form to add a new SKU.</div>

    <g:form name="addVariantForm">
        <g:hiddenField name="addVariantId" value="${variant?.id ?: ''}" />

        <div class="row form-group mb-4">
            <label for="addVariantSku" class="col-3 offset-1 col-form-label text-right">SKU</label>

            <div class="input-group col-4">
                <g:field type="number" name="addVariantSku" value="${variant?.sku}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantRetailPrice" class="col-3 offset-1 col-form-label text-right">Retail Price</label>

            <div class="input-group col-4">
                <g:textField name="addVariantRetailPrice" value="${variant?.retailPrice}" class="form-control bottom-border" disabled="${!sec.loggedInUserInfo(field: 'storeId')}" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantCostPrice" class="col-3 offset-1 col-form-label text-right">Cost Price</label>

            <div class="input-group col-4">
                <g:textField name="addVariantCostPrice" value="${variant?.costPrice}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantSize" class="col-3 offset-1 col-form-label text-right">Size</label>

            <div class="input-group col-4">
                <g:textField name="addVariantSize" value="${variant?.size}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantColour" class="col-3 offset-1 col-form-label text-right">Colour</label>

            <div class="input-group col-4">
                <g:textField name="addVariantColour" value="${variant?.colour}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">Barcodes</label>

            <div id="addBarcodesContainer" class="input-group col-4">
                <g:if test="${!variant.barcodes}">
                    <div id="addBarcode0">
                        <g:render template="addBarcode" model="[index: 0, barcode: null]" />
                    </div>
                </g:if>

                <g:each in="${variant.barcodes}" var="barcode" status="i">
                    <div id="addBarcode${i}">
                        <g:render template="addBarcode" model="[index: i, barcode: barcode]" />
                    </div>
                </g:each>
            </div>
        </div>

        <div class="row form-group mb-4">
            <div class="col-4 offset-4">
                <a href="#" onclick="addBarcode();" class="btn btn-wl">Add Barcode</a>
            </div>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddVariantButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <button type="button" id="saveAddVariantButton" class="btn btn-success" onclick="saveVariant(${variant?.index});">Ok</button>
</div>