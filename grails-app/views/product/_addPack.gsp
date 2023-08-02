<asset:javascript src="validators/input-validator.js" />

<div id="addPackTextContainer-${variantIndex}-${packIndex}" class="row mx-4 pt-2 pb-2 ${isNewPack ? 'hidden' : ''} wl-striped${packIndex % 2}">
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-supplier" class="col-3 my-auto">${pack?.supplier?.name}</div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-quantity" class="col-2 my-auto">${pack?.quantity}</div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-price" class="col-2 my-auto"><g:formatNumber number="${pack?.price}" type="currency" /></div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-order-code" class="col-2 my-auto text-truncate">${pack?.orderCode}</div>
    <div id="add-pack-${variantIndex+1}-${packIndex+1}-barcode" class="col-2 my-auto text-truncate">${pack?.barcode}</div>
    <div class="col-1 my-auto">
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
            <g:textField name="addPack[${packIndex}].quantity" value="${pack?.quantity}" class="form-control bottom-border" maxlength="10" onkeypress="return preventNegativeInteger(event);" ondrop="return false;" onpaste="return false;" oncontextmenu="return false;" onkeyup="preventOverflowValue(this)"/>
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
        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].barcode" value="${pack?.barcode}" maxlength="20"
                          onkeypress="return preventNegativeInteger(event)"
                          class="form-control bottom-border"/>
         </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto font-weight-bold">Status</div>
        <div class="col-3 offset-2 my-auto font-weight-bold">Recommended Retail Price</div>
        <div class="col-3 offset-1 my-auto font-weight-bold">Maximum Order Quantity</div>
    </div>

    <div class="row mx-4 pt-2 pb-2 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto">
            <g:select name="addPack[${packIndex}].status" from="${statuses}" value="${pack?.status ?: 'ACTIVE'}"
                      valueMessagePrefix="PackStatus" class="form-control select-border"/>
        </div>

        <div class="input-group col-2 offset-2 my-auto">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>
            <g:textField name="addPack[${packIndex}].recommendedRetailPrice" value="${pack?.recommendedRetailPrice}"
                         class="form-control mask-money" maxlength="7"/>
        </div>

        <div class="col-2 offset-2 my-auto">
            <g:textField name="addPack[${packIndex}].maximumOrderQuantity" maxlength="5"
                         value="${pack?.maximumOrderQuantity}" class="form-control select-border" min="0"
                         onkeypress="return preventNegativeInteger(event);" ondrop="return false;"
                         onpaste="return false;" oncontextmenu="return false;"/>
        </div>
    </div>
</div>

<script type="text/javascript">
    function addPackSupplierChanged(packIndex) {
        $("#addPack\\[" +packIndex +"\\]\\.supplier\\.name").val($("#addPack\\[" +packIndex +"\\]\\.supplier\\.id option:selected").text());
    }

    $(".mask-money").maskMoney({ allowZero: false });
</script>