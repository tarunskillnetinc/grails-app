<g:if test="${rangeProducts == null}">
    <div class="row ml-0 mr-0 text-center">
        <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${rangeProducts?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${rangeProducts}" var="rangeProduct" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-2 my-auto">
            <div class="row form-group form-check mb-0">
                <g:checkBox name="product-${rangeProduct.key}" class="selections col-2 form-check-input wl-checkbox" style="margin-top: 8px;" />
                <label for="product-${rangeProduct.key}" class="col-10 col-form-label pl-4">${rangeProduct.value[0].productItemCode}</label>
            </div>
        </div>
        <div class="col-6 my-auto">${rangeProduct.value[0].productDescription}</div>

        <g:each in="${ranges}" var="range">
            <div class="col form-group form-check mb-0">
                <g:checkBox name="range-${rangeProduct.key}-${range.id}" checked="${rangeProduct.value.find { it.rangeId == range.id }}" class="col form-check-input wl-checkbox" />
            </div>
        </g:each>
    </div>
</g:each>

%{--<div class="my-3 text-right">--}%
%{--    <util:remotePaginate action="maintenanceSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />--}%
%{--</div>--}%