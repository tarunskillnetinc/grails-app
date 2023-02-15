<div id="tag-product-${i+1}" class="row col-8 offset-2 pt-2 pb-2 ${(i != null) ? 'wl-striped' +(i%2) : ''}" id="productVariant${tagProduct.productVariantId}">
    <g:hiddenField name="sku" value="${tagProduct.sku}" />

    <div id="tag-product-${i+1}-id" class="col-2 my-auto">${tagProduct.productVariantId}</div>
    <div id="tag-product-${i+1}-sku" class="col-3 my-auto">${tagProduct.sku}</div>
    <div id="tag-product-${i+1}-description" class="col my-auto">${tagProduct.productDescription}</div>
    <div class="col-1 my-auto"><a id="tag-product-${i+1}-remove-btn" href="#" class="btn btn-sm btn-danger" onClick="removeProduct(${tagProduct.productVariantId});">Remove</a></div>
</div>