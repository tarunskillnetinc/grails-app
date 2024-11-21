<asset:javascript src="validators/input-validator.js" />

<div id="addPackTextContainer-${variantIndex}-${packIndex}" class="row mx-4 pt-2 pb-2 ${isNewPack ? 'hidden' : ''} wl-striped${packIndex % 2}">
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-supplier" class="col-3 my-auto text-truncate">${pack?.supplier?.name}</div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-quantity" class="col-2 my-auto">${pack?.quantity}</div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-price" class="col-2 my-auto"><g:formatNumber number="${pack?.price}" type="currency" /></div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-order-code" class="col-2 my-auto text-truncate">${pack?.orderCode}</div>
    <div class="col-2 my-auto">
        <g:if test="${pack?.supplier?.symbolGroupId > 0}">
            <button id="add-pack-${variantIndex+1}-${packIndex+1}-edit-btn" class="btn btn-wl disabled" title="You cannot edit packs from this supplier." disabled>Edit</button>
        </g:if>
        <g:else>
            <a id="add-pack-${variantIndex+1}-${packIndex+1}-edit-btn" href="#" class="btn btn-wl" onclick="addPack(${variantIndex}, ${packIndex});">Edit</a>
        </g:else>
    </div>
</div>

<div id="addPackFieldsContainer-${variantIndex}-${packIndex}" class="${!isNewPack ? 'hidden' : ''}">
    <g:hiddenField name="addPack[${packIndex}].id" value="${pack?.id ?: ''}" />
    <g:hiddenField name="addPack[${packIndex}].productVariantId" value="${productVariantId}" />
    <g:hiddenField name="addPack[${packIndex}].effectiveDate" value="${pack?.effectiveDate}" />
    <g:hiddenField name="addPack[${packIndex}].effectiveEndDate" value="${pack?.effectiveEndDate}" />
    <g:hiddenField name="addPack[${packIndex}].allowSubstitutes" value="${pack?.allowSubstitutes}" />
    <g:hiddenField name="addPack[${packIndex}].supplier.name" value="${pack?.supplier?.name}" />
    <g:hiddenField name="addPack[${packIndex}].supplier.symbolGroupId" value="${pack?.supplier?.symbolGroupId}" />
    <g:hiddenField name="addPack[${packIndex}].barcodes" value="${barcodes}"/>
    <g:hiddenField name="addPack[${packIndex}].primaryCase" value="${pack?.primaryCase}"/>


    <g:if test="${pack?.supplier?.symbolGroupId}">
        <g:hiddenField name="addPack[${packIndex}].supplier.id" value="${pack?.supplier?.id}" />
    </g:if>

    <div class="row mx-4 pt-2 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto">
            <g:if test="${!pack?.supplier?.symbolGroupId}">
                <g:select name="addPack[${packIndex}].supplier.id" from="${suppliers}" value="${pack?.supplier?.id}" optionKey="id" optionValue="name" class="form-control select-border" noSelection="${['' : 'Please select']}" onchange="addPackSupplierChanged(${packIndex});" />
            </g:if>
        </div>
        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].quantity" value="${pack?.quantity}" class="form-control bottom-border" maxlength="10" onkeydown="acceptQuantity(event, isWeightedItem())" oninput="validateQuantity(this, 0, Math.pow(2, 31) -1, isWeightedItem())" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;"/>
        </div>
        <div class="input-group col-2 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].price" value="${pack?.price}" class="form-control mask-money" maxlength="7" />
        </div>
        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].orderCode" value="${pack?.orderCode}" class="form-control bottom-border" maxlength="20" onkeypress="return preventNegativeInteger(event);" />
        </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto font-weight-bold">Status</div>
        <div class="col-2 my-auto font-weight-bold">Recommended Retail Price</div>
        <div class="col-2 my-auto font-weight-bold">Maximum Order Quantity</div>
        <div class="col-2 my-auto font-weight-bold">Min Alcohol Unit Price</div>
        <div class="col-3 my-auto font-weight-bold">Weighted Average Cost</div>
    </div>

    <div class="row mx-4 pt-4 pb-2 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto">
            <g:select name="addPack[${packIndex}].status" from="${statuses}" value="${pack?.status ?: 'ACTIVE'}"
                      valueMessagePrefix="PackStatus" class="form-control select-border"/>
        </div>

        <div class="input-group col-2 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].recommendedRetailPrice" value="${pack?.recommendedRetailPrice}"
                         class="form-control mask-money" maxlength="7"/>
        </div>

        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].maximumOrderQuantity" maxlength="5"
                         value="${pack?.maximumOrderQuantity}" class="form-control select-border" min="0"
                         onkeypress="return preventNegativeInteger(event);" ondrop="return false;"
                         onpaste="return false;" oncontextmenu="return false;"/>
        </div>
        <div class="input-group col-2 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].minAlcoholUnitPrice" value="${pack?.minAlcoholUnitPrice}"
                         class="form-control mask-money" maxlength="10"/>
        </div>

        <div class="input-group col-2 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].weightedAverageCost" value="${pack?.weightedAverageCost}"
                         class="form-control mask-money" maxlength="10"/>
        </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto font-weight-bold">Barcodes</div>
        <div class="col-2 my-auto font-weight-bold">Preferred Pack</div>
        <div class="col-2 my-auto font-weight-bold">Price Marked Pack</div>
        <div class="col-2 my-auto font-weight-bold">Price Marked Type</div>
        <div class="col-3 my-auto font-weight-bold">Price Marked Value</div>
    </div>

    <div class="row mx-4 py-2 wl-striped${packIndex % 2}">
        <div id="addBarcodesContainer${packIndex}" class="col-3 mr-0">
            <g:if test="${pack?.barcodez?.empty}">
                <div id="addBarcode0" class="input-group py-1">
                    <g:render template="addBarcode" model="[index: 0, barcode: null, selector: '#addBarcodesContainer' + packIndex]"/>
                </div>
            </g:if>

            <g:each in="${pack?.barcodez}" var="barcode" status="i">
                <div id="addBarcode${i}" class="input-group py-1">
                    <g:render template="addBarcode" model="[index: i, barcode: barcode, selector: '#addBarcodesContainer' + packIndex]"/>
                </div>
            </g:each>
        </div>

        <div id="setPreferred${packIndex}" class="col-2 mr-0">
            <g:checkBox name="addPack[${packIndex}].primaryCaseValue" class="col-1 form-check-input wl-checkbox" checked="${pack?.primaryCase}" />
        </div>
        <div id="setPriceMarked${packIndex}" class="col-2 mr-0">
            <g:checkBox name="addPack[${packIndex}].priceMarked" class="col-1 form-check-input wl-checkbox" checked="${pack?.priceMarked}" />
        </div>
        <div class="col-2 mr-0">
            <g:hiddenField name="addPack[${packIndex}].priceMarkedType" class="form-control" id="addPack[${packIndex}].priceMarkedType"  value="${pack?.priceMarkedType?.name() ?: 'VALUE'}" valueMessagePrefix="PriceMarkedType"/>
            <div class="d-flex justify-content-start">
                <div class="form-check d-flex align-items-center mr-3">
                    <g:radio class="form-check-input wl-radio" type="radio" value="VALUE" checked="${pack?.priceMarkedType == null || pack?.priceMarkedType?.name() == 'VALUE'}" onchange="updatePriceMarkedType(${packIndex}, 'VALUE')" />
                    <label class="form-check-label mb-0 ml-2">Value</label>
                </div>
                <div class="form-check d-flex align-items-center">
                    <g:radio class="form-check-input wl-radio" type="radio" value="PERCENTAGE" checked="${pack?.priceMarkedType?.name() == 'PERCENTAGE'}" onchange="updatePriceMarkedType(${packIndex}, 'PERCENTAGE')" />
                    <label class="form-check-label mb-0 ml-2">Percentage</label>
                </div>
            </div>
        </div>
        <div class="input-group col-3 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].priceMarkedValue" value="${pack?.priceMarkedValue}"
                         class="form-control mask-money" maxlength="10"/>
        </div>
    </div>

    <div class="row mx-4 py-2 wl-striped${packIndex % 2}">
        <div class="col-4">
            <a href="#" onclick="addBarcode('#addBarcodesContainer${packIndex}');" class="btn btn-wl">Add Barcode</a>
        </div>
    </div>

    <div class="${existingPackIds?.contains(pack?.id) == true ? 'hidden' : ''}">
        <div class="row mx-4 pt-2 pb-2 wl-striped${packIndex % 2}">
            <div class="col text-right">
                <a href="#" class="btn btn-wl red" onclick="removePack(${variantIndex}, ${packIndex});">Remove Pack</a>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function addPackSupplierChanged(packIndex) {
        $("#addPack\\[" +packIndex +"\\]\\.supplier\\.name").val($("#addPack\\[" +packIndex +"\\]\\.supplier\\.id option:selected").text());
    }

    $(".mask-money").maskMoney({ allowZero: false });
</script>