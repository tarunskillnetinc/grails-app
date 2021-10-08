<div class="row col-8 offset-2 pt-2 pb-2 ${(i != null) ? 'wl-striped' +(i%2) : ''}" id="productVariant${tagProduct.productVariantId}">
    <g:hiddenField name="sku" value="${tagProduct.sku}" />

    <div class="col-2 my-auto">${tagProduct.productVariantId}</div>
    <div class="col-3 my-auto">${tagProduct.sku}</div>
    <div class="col my-auto">${tagProduct.productDescription}</div>
    <div class="col-1 my-auto"><a href="#" class="btn btn-sm btn-danger" onClick="removeProduct(${tagProduct.productVariantId});">Remove</a></div>
</div>