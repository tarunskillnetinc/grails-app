<asset:javascript src="validators/input-validator.js" />

<div id="addPackTextContainer-${variantIndex}-${packIndex}" class="row mx-4 pt-2 pb-2 ${isNewPack ? 'hidden' : ''} wl-striped${packIndex % 2}">
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-supplier" class="col-3 my-auto text-truncate">${pack?.supplier?.name}</div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-quantity-weighted" class="col-2 my-auto"><g:formatNumber number="${pack?.quantity}" type="number" minFractionDigits="3" /></div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-quantity-nonweighted" class="col-2 my-auto"><g:formatNumber number="${pack?.quantity}" type="number" maxFractionDigits="0" /></div>
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

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto font-weight-bold">Supplier</div>
        <div class="col-4 my-auto font-weight-bold">Pack Quantity</div>
        <div class="col-4 my-auto font-weight-bold">Maximum Order Quantity</div>
    </div>

    <div class="row mx-4 pt-2 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto">
            <g:if test="${!pack?.supplier?.symbolGroupId}">
                <g:select name="addPack[${packIndex}].supplier.id" from="${suppliers}" value="${pack?.supplier?.id}" optionKey="id" optionValue="name" class="form-control select-border" noSelection="${['' : 'Please select']}" onchange="addPackSupplierChanged(${packIndex});" />
            </g:if>
        </div>
        <div class="col-4 my-auto">
            <g:textField name="addPack[${packIndex}].quantity" value="${pack?.quantity}" class="form-control select-border" maxlength="10" onkeydown="acceptQuantity(event, isWeightedItem())" oninput="validateQuantity(this, 0, Math.pow(2, 31) -1, isWeightedItem())" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;"/>
        </div>
        <div class="col-4 my-auto">
            <g:textField name="addPack[${packIndex}].maximumOrderQuantity" maxlength="5"
                         value="${pack?.maximumOrderQuantity}" class="form-control select-border" min="0"
                         onkeypress="return preventNegativeInteger(event);" ondrop="return false;"
                         onpaste="return false;" oncontextmenu="return false;"/>
        </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto font-weight-bold">Order Code</div>
        <div class="col-4 my-auto font-weight-bold">Recommended Retail Price</div>
        <div class="col-4 my-auto font-weight-bold">Cost Price</div>
    </div>

    <div class="row mx-4 pt-4 pb-2 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto">
            <g:textField name="addPack[${packIndex}].orderCode" value="${pack?.orderCode}" class="form-control select-border" maxlength="20" onkeypress="return preventNegativeInteger(event);" />
        </div>
        <div class="input-group col-4 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].recommendedRetailPrice" value="${pack?.recommendedRetailPrice}"
                         class="form-control mask-money" maxlength="7"/>
        </div>
        <div class="input-group col-4 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].price" value="${pack?.price}" class="form-control mask-money" maxlength="7" />
        </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto font-weight-bold">&nbsp;</div>
        <div class="col-4 my-auto font-weight-bold">Height</div>
        <div class="col-4 my-auto font-weight-bold">Width</div>
    </div>

    <div class="row mx-4 pt-4 pb-2 wl-striped${packIndex % 2}">
        <div id="setPreferred${packIndex}" class="col-4 mr-0 d-flex align-items-center">
            <div class="form-check form-check-inline">
                <g:checkBox name="addPack[${packIndex}].primaryCaseValue" id="addPack[${packIndex}].primaryCaseValue" class="form-check-input wl-checkbox" checked="${pack?.primaryCase}" />
                <label class="form-check-label font-weight-bold">Preferred Pack</label>
            </div>
        </div>
        <div class="input-group col-4 my-auto">
            <input id="addPack[${packIndex}].heightCm" value="${pack?.heightCm}" type="text"
                   class="form-control text-right" maxlength="7"
                   onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 999.99, 2)"/>
            <div class="input-group-append">
                <span class="input-group-text" id="heightCmSymbolSuffix${packIndex}">
                    Cm
                </span>
            </div>
        </div>
        <div class="input-group col-4 my-auto">
            <input id="addPack[${packIndex}].widthCm" value="${pack?.widthCm}" type="text"
                   class="form-control text-right" maxlength="7"
                   onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 999.99, 2)"/>
            <div class="input-group-append">
                <span class="input-group-text" id="widthCmSymbolSuffix${packIndex}">
                    Cm
                </span>
            </div>
        </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto font-weight-bold">Length</div>
        <div class="col-4 my-auto font-weight-bold">Weight</div>
        <div class="col-4 my-auto font-weight-bold">Pack Status</div>
    </div>

    <div class="row mx-4 py-2 wl-striped${packIndex % 2}">
        <div class="input-group col-4 my-auto">
            <input id="addPack[${packIndex}].lengthCm" value="${pack?.lengthCm}" type="text"
                         class="form-control text-right" maxlength="7"
                   onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 999.99, 2)"/>
            <div class="input-group-append">
                <span class="input-group-text" id="lengthCmSymbolSuffix${packIndex}">
                    Cm
                </span>
            </div>
        </div>
        <div class="input-group col-4 my-auto">
            <input id="addPack[${packIndex}].weightKg" value="${pack?.weightKg}" type="text"
                   class="form-control text-right" maxlength="7"
                   onkeydown="acceptFloat(event)" onkeyup="validateFloatQuantity(this, 0, 9999.999, 3)"/>
            <div class="input-group-append">
                <span class="input-group-text" id="weightKgSymbolSuffix${packIndex}">
                    Kg
                </span>
            </div>
        </div>
        <div class="input-group col-4 my-auto">
            <g:select name="addPack[${packIndex}].status" from="${statuses}" value="${pack?.status ?: 'ACTIVE'}"
                      valueMessagePrefix="PackStatus" class="form-control select-border"/>
        </div>
    </div>


    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-4 my-auto font-weight-bold">Barcodes</div>
        <div class="col-4 my-auto font-weight-bold"></div>
    </div>

    <div class="row mx-4 py-2 wl-striped${packIndex % 2}">
        <div id="addBarcodesContainer${packIndex}" class="col-4 mr-0">
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
        <div class="col-4"></div>
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

    if(isWeightedItem()) {
        $('div[id$="-nonweighted"]').hide();
    } else {
        $('div[id$="-weighted"]').hide();
    }

</script>