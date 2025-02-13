<g:each in="${productGroupProducts}" var="productGroupProduct" status="i">
    <g:set var="pos" value="${(i != null) ? (i + 1) : 0}"/>
    <div class="row col-12 pt-2 pb-2 ${(i != null) ? 'wl-striped' + (i % 2) : ''}"
         id="productVariant${productGroupProduct.productVariantId}">
        <g:hiddenField name="sku" value="${productGroupProduct.sku}"/>

        <div id="tag-product-${pos}-id" class="col-3">${productGroupProduct.itemCode}</div>

        <div id="tag-product-${pos}-sku" class="col-3">${productGroupProduct.sku}</div>

        <div id="tag-product-${pos}-description" class="col-4">${productGroupProduct.productDescription}</div>

        <div class="col-2 my-auto"><a id="tag-product-${pos}-remove-btn" href="#" class="btn btn-sm btn-danger"
                                      role="button"
                                      onClick="removeProduct(${productGroupProduct.productVariantId});">Remove</a></div>
    </div>
</g:each>