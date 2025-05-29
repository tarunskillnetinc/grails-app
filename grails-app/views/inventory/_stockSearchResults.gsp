<g:set var="pos" value="${(i != null) ? (i+1) : 0}"/>
<div class="row ml-0 mr-0 pt-2 pb-2 ${(i != null) ? 'wl-striped' + (i%2) : ''}" id="product${product?.id}">
    <g:hiddenField name="productId" value="${product.id}" />
    <g:hiddenField name="selectedStores" class="selected-stores" value="${productListItem?.selectedStores ? productListItem.selectedStores.encodeAsJSON() : '[]'}" />
    <div id="prod-${pos}-id" class="col-md-2 my-auto text-truncate">${product?.sku}</div> <%-- Item Code (assuming SKU is used for Item Code) --%>
    <div id="prod-${pos}-sku" class="col-md-2 my-auto">${product?.sku}</div> <%-- Barcode (assuming SKU is also used for Barcode) --%>
    <div id="prod-${pos}-description" class="col-md-2 my-auto">${product?.product?.description}</div> <%-- Description --%>
    <div id="prod-${pos}-category" class="col-md-1 my-auto">${category?.description}</div> <%-- Category --%>
    <div class="col-md-1 my-auto font-weight-bold stores-info">
        <span class="aggregated-quantity">${productListItem?.productQuantityInStock ?: 0}</span>
    </div>

    <div id="prod-${pos}-amended" class="col-md-1 my-auto">
        <input type="number" min="0" class="form-control form-control-sm amended-quantity-input" style="width: 70px;" name="amendedQuantity_${product?.id}" value="${productListItem?.fillQuantity ?: ''}"/>
    </div>

    <div class="col-md-1 my-auto font-weight-bold stores-info">
        <span class="stores-count">${productListItem?.selectedStores?.size() ?: 0}</span>
         <span class="stores-list hidden">${productListItem?.selectedStores?.collect{ it.name }?.join(', ') ?: ''}</span>
    </div>

    <div class="col-md-2 my-auto d-flex align-items-center">
        <button class="btn btn-sm btn-info view-adjustment-btn mr-1" onclick="viewAdjustments(${product?.id})" style="display: ${productListItem?.selectedStores?.size() > 0 ? 'inline-block' : 'none'};">View Adjustment</button>
        <a id="prod-${pos}-remove-btn" href="#" class="btn btn-sm btn-danger" onClick="removeProduct(${product?.id});">Remove</a>
    </div>
</div>