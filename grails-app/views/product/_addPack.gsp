<div id="addPackTextContainer-${variantIndex}-${packIndex}" class="row mx-4 pt-2 pb-2 ${isNewPack ? 'hidden' : ''} wl-striped${packIndex % 2}">
    <div class="col-3 my-auto">${pack?.supplier?.name}</div>
    <div class="col-2 my-auto">${pack?.quantity}</div>
    <div class="col-2 my-auto"><g:formatNumber number="${pack?.price}" type="currency" /></div>
    <div class="col-2 my-auto">${pack?.orderCode}</div>
    <div class="col-2 my-auto">${pack?.status}</div>
    <div class="col-1 my-auto">
        <g:if test="${pack?.supplier?.symbolGroupId > 0}">
            <button class="btn btn-wl disabled" title="You cannot edit packs from this supplier." disabled>Edit</button>
        </g:if>
        <g:else>
            <a href="#" class="btn btn-wl" onclick="addPack(${variantIndex}, ${packIndex});">Edit</a>
        </g:else>
    </div>
</div>

<div id="addPackFieldsContainer-${variantIndex}-${packIndex}" class="${!isNewPack ? 'hidden' : ''}">
    <g:hiddenField name="addPack[${packIndex}].id" value="${pack?.id ?: ''}" />
    <g:hiddenField name="addPack[${packIndex}].effectiveDate" value="${pack?.effectiveDate}" />
    <g:hiddenField name="addPack[${packIndex}].effectiveEndDate" value="${pack?.effectiveEndDate}" />
    <g:hiddenField name="addPack[${packIndex}].allowSubstitutes" value="${pack?.allowSubstitutes}" />
    <g:hiddenField name="addPack[${packIndex}].supplier.name" value="${pack?.supplier?.name}" />
    <g:hiddenField name="addPack[${packIndex}].supplier.symbolGroupId" value="${pack?.supplier?.symbolGroupId}" />

    <div class="row mx-4 pt-3 wl-striped${packIndex % 2}">
        <div class="col-3 my-auto">
            <g:select name="addPack[${packIndex}].supplier.id" from="${suppliers}" value="${pack?.supplier?.id}" optionKey="id" optionValue="name" class="form-control select-border" noSelection="[null : 'Please select']" onchange="addPackSupplierChanged(${packIndex});" />
        </div>
        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].quantity" value="${pack?.quantity}" class="form-control bottom-border" />
        </div>
        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].price" value="${pack?.price}" class="form-control bottom-border mask-money" />
        </div>
        <div class="col-2 my-auto">
            <g:textField name="addPack[${packIndex}].orderCode" value="${pack?.orderCode}" class="form-control bottom-border" />
        </div>
        <div class="col-2 my-auto">
            <g:select name="addPack[${packIndex}].status" from="${statuses}" value="${pack?.status ?: 'ACTIVE'}" valueMessagePrefix="PackStatus" class="form-control select-border" />
        </div>
    </div>

    <div class="row mx-4 pt-4 wl-striped${packIndex % 2}">
        <div class="col-3 offset-3 my-auto font-weight-bold">Recommended Retail Price</div>
        <div class="col-3 offset-1 my-auto font-weight-bold">Maximum Order Quantity</div>
    </div>

    <div class="row mx-4 pt-2 pb-3 wl-striped${packIndex % 2}">
        <div class="col-2 offset-3 my-auto">
            <g:textField name="addPack[${packIndex}].recommendedRetailPrice" value="${pack?.recommendedRetailPrice}" class="form-control bottom-border mask-money" />
        </div>
        <div class="col-2 offset-2 my-auto">
            <g:textField name="addPack[${packIndex}].maximumOrderQuantity" value="${pack?.maximumOrderQuantity}" class="form-control bottom-border" />
        </div>
    </div>
</div>

<script type="text/javascript">
    function addPackSupplierChanged(packIndex) {
        $("#addPack\\[" +packIndex +"\\]\\.supplier\\.name").val($("#addPack\\[" +packIndex +"\\]\\.supplier\\.id option:selected").text());
    }
</script>