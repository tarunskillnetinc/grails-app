<%@ page import="uk.co.wonderlane.wlpos.enums.StockManagementType" %>
<asset:javascript src="validators/input-validator.js" />
<div class="modal-header">
    <g:if test="${isEditMode}">
        <h2>Edit SKU</h2>
    </g:if>
    <g:else>
        <h2>Add SKU</h2>
    </g:else>
</div>

<div class="modal-body">
    <g:if test="${!isEditMode}">
        <div class="text-center mt-4">Please complete the following form to add a new SKU.</div>
    </g:if>

    <g:form name="addVariantForm">
        <g:hiddenField name="addVariantId" value="${variant?.id ?: ''}" />
        <g:hiddenField name="addVariantStoreId" value="${variant?.storeId}" />
        <g:hiddenField name="addVariantWeightedAverageCostPrice" value="${variant?.weightedAverageCostPrice}" />
        <g:hiddenField name="addEffectiveDate" value="${variant?.effectiveDate}" />

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold">SKU</div>
            <div class="col-4 my-auto font-weight-bold">Description</div>
            <div class="col-4 my-auto font-weight-bold">Receipt Description</div>
        </div>

        <div class="row mx-4 py-2">
            <div class="input-group col-4 my-auto">
                <g:field type="number" name="addVariantSku" value="${variant?.sku}"
                         disabled="${isEditMode}"
                         class="form-control bottom-border"/>
            </div>
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantDescription" value="${variant?.description}" class="form-control bottom-border" maxlength="100"/>
            </div>
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantReceiptDescription" value="${variant?.receiptDescription}" class="form-control bottom-border" maxlength="50"/>
            </div>
        </div>

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold">Retail Price</div>
            <div class="col-4 my-auto font-weight-bold">Cost Price</div>
        </div>

        <div class="row mx-4 py-2">
            <div class="input-group col-4 my-auto">
                <div class="input-group-prepend">
                    <span class="input-group-text">&pound;</span>
                </div>

                <g:textField name="addVariantRetailPrice" value="${sec.loggedInUserInfo(field: 'storeId') ? variant?.currentPrice : ''}" class="form-control mask-money" disabled="${!sec.loggedInUserInfo(field: 'storeId') || zeroPrice}" />
            </div>
            <div class="input-group col-4 my-auto">
                <div class="input-group-prepend">
                    <span class="input-group-text">&pound;</span>
                </div>

                <g:textField name="addVariantCostPrice" value="${variant?.costPrice}" class="form-control mask-money" />
            </div>
            <div id="setPriceMarked" class="col-4 mr-0 d-flex align-items-center">
                <div class="form-check form-check-inline">
                    <g:checkBox name="addVariantPriceMarked" id="addVariantPriceMarked" class="form-check-input wl-checkbox" checked="${variant?.priceMarked}" />
                    <label class="form-check-label font-weight-bold">Price Marked</label>
                </div>
            </div>
        </div>

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold" data-toggle="tooltip" title="Weighted Average Cost Price">WAC</div>
            <div class="col-4 my-auto font-weight-bold">Stock Management</div>
        </div>

        <div class="row mx-4 py-2">
            <div class="input-group col-4 my-auto">
                <div class="input-group-prepend">
                    <span class="input-group-text">&pound;</span>
                </div>
                <g:textField name="weightedAverageCostPrice" readonly="true" disabled="true" class="form-control mask-money"
                             value="${wacValue != BigDecimal.ZERO ? String.format("%,.2f", wacValue) : '-'}" />
            </div>
            <div class="input-group col-4 my-auto">
                <g:select name="stockManagementType"
                          id="stockManagementType"
                          class="form-control select-border"
                          from="${StockManagementType.values()}"
                          valueMessagePrefix="StockManagementType"
                          optionKey="${{it}}"
                          value="${variant?.stockManagementType ?: StockManagementType.STANDARD}"/>
            </div>
        </div>

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold">Shelf Life</div>
            <div class="col-4 my-auto font-weight-bold">Shelf Capacity</div>
            <div class="col-4 my-auto font-weight-bold">Minimum Display Quantity</div>
        </div>

        <div class="row mx-4 py-2">
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantShelfLifeDays" value="${variant?.shelfLifeDays}" class="form-control" />
                <div class="input-group-append">
                    <span class="input-group-text">days</span>
                </div>
            </div>
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantShelfCapacity" value="${variant?.shelfCapacity}" class="form-control" disabled="${!sec.loggedInUserInfo(field: 'storeId')}" />
            </div>
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantMinimumDisplayQuantity" value="${variant?.minimumDisplayQuantity}" class="form-control" disabled="${!sec.loggedInUserInfo(field: 'storeId')}" />
            </div>
        </div>

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold">Total Unit Size</div>
            <div class="col-4 my-auto font-weight-bold">Unit Of Measure</div>
            <div class="col-4 my-auto font-weight-bold">Items In Unit</div>
        </div>

        <div class="row mx-4 py-2">
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantUnitSize" value="${variant?.unitSize}" class="form-control" onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 9999.999, 3)"/>
            </div>
            <div class="input-group col-4 my-auto">
                <g:select name="addVariantUnitOfMeasure"
                          class="form-control select-border"
                          from="${unitsOfMeasure}"
                          optionKey="id"
                          optionValue="${{it.name + " - " + it.symbol}}"
                          value="${variant?.unitOfMeasure?.id}"
                          noSelection="${[null: '']}"/>
            </div>
            <div class="input-group col-4 my-auto">
                <g:textField name="addVariantItemsInUnit" value="${variant?.itemsInUnit}" class="form-control" onkeydown="acceptMinMaxNumberValue(event, 1, 9999)" />
            </div>
        </div>

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold">Height</div>
            <div class="col-4 my-auto font-weight-bold">Width</div>
            <div class="col-4 my-auto font-weight-bold">Depth</div>
        </div>

        <div class="row mx-4 py-2">
            <div class="input-group col-4 my-auto">
                <input name="addVariantHeightCm" id="addVariantHeightCm" value="${variant?.heightCm}" type="text"
                       class="form-control text-right" maxlength="7"
                       onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 999.99, 2)"/>
                <div class="input-group-append">
                    <span class="input-group-text" id="heightCmSymbolSuffix">
                        Cm
                    </span>
                </div>
            </div>
            <div class="input-group col-4 my-auto">
                <input name="addVariantWidthCm" id="addVariantWidthCm" value="${variant?.widthCm}" type="text"
                       class="form-control text-right" maxlength="7"
                       onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 999.99, 2)"/>
                <div class="input-group-append">
                    <span class="input-group-text" id="widthCmSymbolSuffix">
                        Cm
                    </span>
                </div>
            </div>
            <div class="input-group col-4 my-auto">
                <input name="addVariantDepthCm" id="addVariantDepthCm" value="${variant?.depthCm}" type="text"
                       class="form-control text-right" maxlength="7"
                       onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 999.99, 2)"/>
                <div class="input-group-append">
                    <span class="input-group-text" id="depthCmSymbolSuffix">
                        Cm
                    </span>
                </div>
            </div>
        </div>

        <div class="row mx-4 pt-4">
            <div class="col-4 my-auto font-weight-bold">Barcodes</div>
        </div>

        <div class="row mx-4 py-2">
            <div id="addBarcodesContainer" class="col-4 mr-0">
                <g:if test="${!variant.barcodez}">
                    <div id="addBarcode0" class="input-group py-1">
                        <g:render template="addBarcode" model="[index: 0, barcode: null, selector: '#addBarcodesContainer']" />
                    </div>
                </g:if>

                <g:each in="${variant.barcodez}" var="barcode" status="i">
                    <div id="addBarcode${i}" class="input-group py-1">
                        <g:render template="addBarcode" model="[index: i, barcode: barcode,  selector: '#addBarcodesContainer']" />
                    </div>
                </g:each>
            </div>
        </div>

        <div class="row mx-4 py-2">
            <div class="col-4">
                <a href="#" onclick="addBarcode('#addBarcodesContainer');" class="btn btn-wl">Add Barcode</a>
            </div>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddVariantButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <button type="button" id="saveAddVariantButton" class="btn btn-success" onclick="saveVariant(${variant?.index}); ${isNewVariant} ? saveTempLocations(${variant?.index}) : null">Ok</button>
</div>
