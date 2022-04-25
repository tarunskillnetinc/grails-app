<g:if test="${supplierUpdates == null}">
    <div class="row ml-0 mr-0 text-center">
        <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${supplierUpdates?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${supplierUpdates}" var="supplierUpdate" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-1 my-auto">
            <g:checkBox name="product-${i}" class="col-12 form-check-input wl-checkbox my-auto" />
        </div>

        <div class="col-2 my-auto">${supplierUpdate.productVariant?.sku} - ${supplierUpdate.productVariant?.product?.description}</div>
        <div class="col-1 my-auto">${supplierUpdate.quantity}</div>
        <div class="col-1 my-auto">${supplierUpdate.effectiveDate.toString("dd/MM/yyyy HH:mm")}</div>
        <div class="col-1 my-auto">${supplierUpdate.priceMarked ? "Yes" : "No"}</div>
        <div class="col-1 my-auto">Old Pack Cost Price</div>
        <div class="col-1 my-auto"><g:formatNumber number="${supplierUpdate.price}" type="currency" /></div>
        <div class="col-1 my-auto"><g:formatNumber number="${supplierUpdate.recommendedRetailPrice}" type="currency" /></div>
        <div class="col-1 my-auto">Margin</div>
        <div class="col-1 my-auto">Retail Price</div>
        <div class="col-1 my-auto">Difference</div>
    </div>
</g:each>

%{--<div class="my-3 text-right">--}%
%{--    <util:remotePaginate action="maintenanceSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />--}%
%{--</div>--}%