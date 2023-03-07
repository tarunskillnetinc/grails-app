<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemCode" }?.enabled}">
        <div class="col-1 font-weight-bold">Item Code</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col font-weight-bold">Description</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "unitSize" }?.enabled}">
        <div class="col-1 font-weight-bold">Unit Size</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatRate" }?.enabled}">
        <div class="col-1 font-weight-bold">VAT Rate</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "category" }?.enabled}">
        <div class="col-2 font-weight-bold">Category</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">
        <div class="col-1 font-weight-bold">Cost Price</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailPrice" }?.enabled}">
        <div class="col-1 font-weight-bold">Retail Price</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">
        <div class="col-1 font-weight-bold">Margin</div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${products == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
        </div>
    </g:if>

    <g:if test="${products?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${products}" var="product" status="i">
        <div id="product-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'show', id: product.id)}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "itemCode" }?.enabled}">
                <div id="product-result-${i+1}-item-code" class="col-1 text-truncate">${product.itemCode}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="product-result-${i+1}-description" class="col">${product.description}</div>
            </g:if>
%{--            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "unitSize" }?.enabled}">--}%
%{--                <div id="product-result-${i+1}-unit-size" class="col-1">${product.unitSize}</div>--}%
%{--            </g:if>--}%
%{--            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "vatRate" }?.enabled}">--}%
%{--                <div id="product-result-${i+1}-vat-rate" class="col-1">${product.vatCode?.percentage}%</div>--}%
%{--            </g:if>--}%
%{--            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "category" }?.enabled}">--}%
%{--                <div id="product-result-${i+1}-category" class="col-2">${product.category?.description}</div>--}%
%{--            </g:if>--}%
%{--            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "costPrice" }?.enabled}">--}%
%{--                <div id="product-result-${i+1}-cost-price" class="col-1">&pound;${product.costPrice}</div>--}%
%{--            </g:if>--}%
%{--            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailPrice" }?.enabled}">--}%
%{--                <div id="product-result-${i+1}-retail-price" class="col-1">&pound;${product.retailPrice}</div>--}%
%{--            </g:if>--}%
%{--            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "margin" }?.enabled}">--}%
%{--                <div id="product-result-${i+1}-margin" class="col-1">${product.margin}%</div>--}%
%{--            </g:if>--}%
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxSearchProducts" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>