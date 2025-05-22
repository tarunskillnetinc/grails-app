<g:set var="pos" value="${(i != null) ? (i+1) : 0}"/>
<div class="row ml-0 mr-0 pt-2 pb-2 ${(i != null) ? 'wl-striped' + (i%2) : ''}" id="product${product?.id}">
    <g:hiddenField name="productId" value="${product.id}" />
    <g:hiddenField name="selectedStores" class="selected-stores" value="" />

    <div id="prod-${pos}-id" class="col my-auto text-truncate">${product?.sku}</div>
    <div id="prod-${pos}-sku" class="col my-auto">${product?.sku}</div>
    <div id="prod-${pos}-description" class="col my-auto">${product?.product?.description}</div>
    <div id="prod-${pos}-category" class="col my-auto">${category?.description}</div>
    <div id="prod-${pos}-quantity" class="col my-auto">20</div>
    <div id="prod-${pos}-amended" class="col my-auto"><input type="number" min="0"/></div>
    <div class="col my-auto font-weight-bold stores-info">
        <span class="stores-count">0</span>
    </div>
    <div class="col-1 my-auto">
        <a id="prod-${pos}-remove-btn" href="#" class="btn btn-sm btn-danger" onClick="removeProduct(${product?.id});">Remove</a>
    </div>
</div>