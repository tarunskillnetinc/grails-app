<g:set var="pos" value="${(i != null) ? (i+1) : 0}"/>
<div class="row ml-0 mr-0 pt-2 pb-2 ${(i != null) ? 'wl-striped' + (i%2) : ''}" id="productVariant${productVariant?.id}">
    <g:hiddenField name="productVariantId" value="${productVariant?.id}" />

    <div id="prod-${pos}-id" class="col-1 my-auto text-truncate">${productVariant?.id}</div>
    <div id="prod-${pos}-sku" class="col-2 my-auto">${productVariant?.sku}</div>
    <div id="prod-${pos}-description" class="col my-auto">${productVariant?.product?.description}</div>
    <div id="prod-${pos}-colour" class="col-1 my-auto">N/A</div>
    <div id="prod-${pos}-size" class="col-1 my-auto">N/A</div>
    <div class="col-1 my-auto"><a id="prod-${pos}-remove-btn" href="#" class="btn btn-sm btn-danger" onClick="removeProduct(${productVariant?.id});">Remove</a></div>
</div>