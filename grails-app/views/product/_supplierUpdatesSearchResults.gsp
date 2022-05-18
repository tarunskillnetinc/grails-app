<g:if test="${supplierPriceUpdates == null}">
    <div class="row ml-0 mr-0 text-center">
        <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${supplierPriceUpdates?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${supplierPriceUpdates}" var="supplierPriceUpdate" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-1 my-auto">
            <g:checkBox name="product-${i}" class="col-12 wl-checkbox my-auto" />
        </div>

        <div class="col-3 my-auto">${supplierPriceUpdate.sku} - ${supplierPriceUpdate.description}</div>
        <div class="col-1 my-auto text-center">${supplierPriceUpdate.quantity}</div>
        <div class="col-1 my-auto text-center">${supplierPriceUpdate.effectiveDate.toString("dd/MM/yyyy")}</div>
        <div class="col-1 my-auto text-center">${supplierPriceUpdate.priceMarked ? "Yes" : "No"}</div>
        <div class="col-1 my-auto text-center"><g:formatNumber number="${supplierPriceUpdate.oldPackPrice}" type="currency" /></div>
        <div class="col-1 my-auto text-center"><g:formatNumber number="${supplierPriceUpdate.newPackPrice}" type="currency" /></div>
        <div class="col-1 my-auto text-center"><g:formatNumber number="${supplierPriceUpdate.recommendedRetailPrice}" type="currency" /></div>
        <div class="col-1 my-auto text-center"><g:getMargin retailPrice="${supplierPriceUpdate.recommendedRetailPrice}" costPrice="${supplierPriceUpdate.newPackPrice}" quantity="${supplierPriceUpdate.quantity}" /></div>
        <div class="col-1 my-auto text-center input-group">
            <div class="input-group-prepend">
                <span class="input-group-text">&pound;</span>
            </div>

            <g:textField name="retailPrice[${i}]" value="${supplierPriceUpdate.retailPrice}" class="form-control mask-money" />
        </div>
    </div>
</g:each>

%{--<div class="my-3 text-right">--}%
%{--    <util:remotePaginate action="maintenanceSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />--}%
%{--</div>--}%