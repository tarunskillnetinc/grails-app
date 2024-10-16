<%@ page import="java.math.RoundingMode" %>

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

<g:if test="${!productList?.productListItems}">
    <div id="noResultsRow" class="row col-8 offset-2 py-2 text-center my-auto wl-striped0"><div class="col text-center">No products added.</div></div>
</g:if>

<div id="search-results" style="max-height: 400px;">
    <g:each in="${productList?.productListItems}" var="productListItem" status="i">
        <div class="row col-8 offset-2 py-2 wl-striped${i%2} hoverable productListItems pointer" title="Click to edit."
             onclick="document.location.href='${createLink(action: 'productListItem', id: productListItem.id, params: [productListId: productList?.id])}';">

            <g:hiddenField name="supplierId" id="supplierId" value="${supplier?.id ?: 0}" />

            <div class="col-2 my-auto">${productListItem.getProductVariantId()}</div>
            <div class="col-6 my-auto" style='word-break: break-all; word-wrap: break-word;'>${productListItem?.productVariant?.product?.description}</div>
            <div class="col-2 my-auto">${productListItem.getQuantity().setScale((productListItem.getQuantity().remainder(BigDecimal.ONE) == BigDecimal.ZERO) ? 0 : 3, RoundingMode.HALF_UP)}</div>
            <div class="col-2 text-right">
                <button type="button" id="removeItemButton_${productListItem.getProductVariantId()}" class="btn btn-danger"
                        data-dismiss="modal" data-productItemId="${productListItem?.id}" onclick="event.stopPropagation(); deleteOrderItem(event)">Delete Item</button>
            </div>
        </div>
    </g:each>

    <div class="my-3 text-right">
        <util:remotePaginate action="ajaxSearchProducts" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
    </div>
</div>

<div class="row col-8 offset-2 mt-4 pb-2">
    <g:if test="${!productList}">
        <button type="button" id="cancelAddSupplierButton" disabled class="btn btn-danger" onclick="deleteOrder();" data-dismiss="modal" style="margin-left: -12px; float: left; top: 0; right: 0">Delete Order</button>
    </g:if>
    <g:else>
        <button type="button" id="cancelAddSupplierButton" class="btn btn-danger" onclick="deleteOrder();" data-dismiss="modal" style="margin-left: -12px; float: left; top: 0; right: 0">Delete Order</button>
    </g:else>

    <g:if test="${!productList?.productListItems}">
        <button type="button" id="saveSupplierButton" disabled class="btn btn-success" onclick="complete();" style="margin-left: 10px; float: left; top: 0; right: 0">Complete</button>
    </g:if>
    <g:else>
        <button type="button" id="saveSupplierButton" class="btn btn-success" onclick="complete();" style="margin-left: 10px; float: left; top: 0; right: 0">Complete</button>
    </g:else>
</div>
