<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "parentCategory" }?.enabled}">
        <div class="col-1 font-weight-bold">Parent Category</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
        <div class="col font-weight-bold">Description</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailerCategoryCode" }?.enabled}">
        <div class="col-1 font-weight-bold">Retailer Category Code</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "buyerId" }?.enabled}">
        <div class="col-1 font-weight-bold">Buyer ID Required</div>
    </g:if>
    <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "buyerAge" }?.enabled}">
        <div class="col-1 font-weight-bold">Buyer Age</div>
    </g:if>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${categories == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
        </div>
    </g:if>

    <g:if test="${categories?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </g:if>

    <g:each in="${categories}" var="category" status="i">
        <div id="category-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'show', id: category.id)}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "parentCategory" }?.enabled}">
                <div id="category-result-${i+1}-parentCategory" class="col-1 text-truncate">${category.parentCategory?.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="category-result-${i+1}-description" class="col">${category.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailerCategoryCode" }?.enabled}">
                <div id="category-result-${i+1}-retailer-Category-Code" class="col-1">${category.retailerCategoryCode}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "buyerId" }?.enabled}">
                <div id="category-result-${i+1}-buyerId" class="col-1">${category.restrictions.buyerIdRequired}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "buyerAge" }?.enabled}">
                <div id="category-result-${i+1}-buyer-age" class="col-1">${category.restrictions.buyerAgeRestriction}</div>
            </g:if>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate controller="category" action="ajaxSearchCategories" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm]" />
</div>