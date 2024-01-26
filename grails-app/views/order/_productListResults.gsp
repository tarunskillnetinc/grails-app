<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
        <div class="col-2 font-weight-bold">Variant Id</div>
        <div class="col-6 font-weight-bold">Description</div>
        <div class="col-2 font-weight-bold">Quantity</div>
        <div class="col-2 font-weight-bold"></div>
</div>

<div id="search-results" style="max-height: 400px; overflow-x: auto; overflow-y: auto;">

    <g:if test="${!productListItems}">
        <div class="row col-8 offset-2 pt-2 pb-2 text-center emptyProductListItems" id="emptyProductListItems" style="margin-top: 10px">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">No products added.</div>
        </div>
    </g:if>

    <g:each in="${productListItems}" var="productListItem" status="i">
        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped${i%2} hoverable productListItems" title="Click to edit." style="cursor: pointer;"
             onclick="document.location.href='${createLink(action:'productListItem', params: [supplierId: supplier?.id, variantId: productListItem?.productVariantId, productListId: productList?.id])}';">
            <div class="col-2">${productListItem.getProductVariantId()}</div>
            <div class="col-6" style='word-break: break-all; word-wrap: break-word;'>${productListItem.getProductLongDescription()}</div>
            <div class="col-2">${productListItem.getQuantity()}</div>
            <div class="col-2">
                <button type="button" id="removeItemButton_${productListItem.getProductVariantId()}" class="btn btn-danger productItemDeleteButton"
                        data-dismiss="modal" data-productItemId="${productListItem?.id}" style="margin-left: -12px; float: left; top: 0; right: 0;" onclick="deleteOrderItem()">Delete</button>
            </div>
        </div>
    </g:each>

    <div class="my-3 text-right">
        <util:remotePaginate action="ajaxSearchProducts" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
    </div>

</div>

<div class="row col-8 offset-2 mt-5 pb-2 bottom-border" style="width: 100%; ">

    <g:if test="${!productList}">
        <button type="button" id="cancelAddSupplierButton" disabled class="btn btn-danger" onclick="deleteOrder();" data-dismiss="modal" style="margin-left: -12px; float: left; top: 0; right: 0">Delete</button>
    </g:if>
    <g:else>
        <button type="button" id="cancelAddSupplierButton" class="btn btn-danger" onclick="deleteOrder();" data-dismiss="modal" style="margin-left: -12px; float: left; top: 0; right: 0">Delete</button>
    </g:else>
    <g:if test="${!productListItems}">
        <button type="button" id="saveSupplierButton" disabled class="btn btn-success" onclick="complete();" style="margin-left: 10px; float: left; top: 0; right: 0">Complete</button>
    </g:if>
    <g:else>
        <button type="button" id="saveSupplierButton" class="btn btn-success" onclick="complete();" style="margin-left: 10px; float: left; top: 0; right: 0">Complete</button>
    </g:else>

</div>
