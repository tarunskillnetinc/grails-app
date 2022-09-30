<div class="modal-header" >
    <g:if test="${variantSize > 0}">
        <h2>Select SKU</h2>
    </g:if>
</div>

<div class="modal-body" style="max-height: 500px; overflow-x: auto; overflow-y: auto;">

    <g:if test="${variantSize > 0}">
        <g:each in="${variants}" var="variant" status="i">
            <g:if test="${(variant.storeId == null || variant.storeId == storeId) && product?.isCurrentProductVariant(effectiveDate, variant.id, variant.sku)}">
                <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i % 2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'productListItem', params: [supplierId: supplierId,variantId: variant?.id, productListId: productListId])}';">
                    <div class="row" style="text-align: center; margin: auto"><h3>SKU : ${variant?.sku}</h3></div>
                </div>
            </g:if>
        </g:each>
    </g:if>
    <g:else>
        <div class="row" style="text-align: center; margin-left: 5px"><h3>No Sku For Supplier</h3></div>
    </g:else>

</div>

<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
</div>





