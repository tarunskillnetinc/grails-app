<g:if test="${categories == null}">
    <div class="row text-center">
        <div class="col-12">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${categories?.size() == 0}">
    <div class="col-12 pt-2 pb-2 text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${categories}" var="category" status="i">
    <div id="category-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div id="category-result-${i+1}-category-code" class="col-2 my-auto">${category.retailerCategoryCode}</div>
        <div id="category-result-${i+1}-description" class="col-9 my-auto">${category.description}</div>
        <a id="category-result-${i+1}-select-button" href="#" class="col-1 btn btn-wl my-auto" onclick="addPromotionGroupCategory(${category.id})" data-dismiss="modal">Select</a>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="categorySearch" total="${totalResults ?: 0}" update="category-search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>