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
                <g:field type="number" name="addVariantSku" value="${variant?.sku}"
                         disabled="${isEditMode}"
                         class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantRetailPrice" class="col-3 offset-1 col-form-label text-right">Retail Price</label>

            <div class="input-group col-4">
                <div class="input-group-prepend">
                    <span class="input-group-text">&pound;</span>
                </div>

                <g:textField name="addVariantRetailPrice" value="${variant?.retailPrice}" class="form-control mask-money" disabled="${!sec.loggedInUserInfo(field: 'storeId') || zeroPrice}" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantCostPrice" class="col-3 offset-1 col-form-label text-right">Cost Price</label>

            <div class="input-group col-4">
                <div class="input-group-prepend">
                    <span class="input-group-text">&pound;</span>
                </div>

                <g:textField name="addVariantCostPrice" value="${variant?.costPrice}" class="form-control mask-money" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantShelfLifeDays" class="col-3 offset-1 col-form-label text-right">Shelf life (days)</label>

            <div class="input-group col-4">
                <g:textField name="addVariantShelfLifeDays" value="${variant?.shelfLifeDays}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantShelfCapacity" class="col-3 offset-1 col-form-label text-right">Shelf Capacity</label>

            <div class="input-group col-4">
                <g:textField name="addVariantShelfCapacity" value="${variant?.shelfCapacity}" class="form-control bottom-border" disabled="${!sec.loggedInUserInfo(field: 'storeId')}" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addVariantMinimumDisplayQuantity" class="col-3 offset-1 col-form-label text-right">Minimum Display Quantity</label>
            <div class="input-group col-4">
                <g:textField name="addVariantMinimumDisplayQuantity" value="${variant?.minimumDisplayQuantity}" class="form-control bottom-border" disabled="${!sec.loggedInUserInfo(field: 'storeId')}" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">Barcodes</label>

            <div id="addBarcodesContainer" class="col-6 mr-0">
                <g:if test="${!variant.barcodez}">
                    <div id="addBarcode0" class="input-group py-1">
                        <g:render template="addBarcode" model="[index: 0, barcode: null]" />
                    </div>
                </g:if>

                <g:each in="${variant.barcodez}" var="barcode" status="i">
                    <div id="addBarcode${i}" class="input-group py-1">
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
    <button type="button" id="saveAddVariantButton" class="btn btn-success" onclick="saveVariant(${variant?.index}); saveTempLocations(${variant?.index})">Ok</button>
</div>