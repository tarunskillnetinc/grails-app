<%@ page import="java.util.stream.Collectors" %>

<g:if test="${categories == null}">
    <div class="row text-center">
        <div class="col-12">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${categories?.size() == 0}">
    <div class="row text-center">
        <div class="col-12">No results found.</div>
    </div>
</g:if>

<g:each in="${categories}" var="category" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
        <div class="col-2 my-auto">${category.retailerCategoryCode}</div>
        <div class="col-9 my-auto">${category.description}</div>
        <a href="#" class="col-1 btn btn-wl my-auto" onclick="addPromotionCategory(${category.id}, '${category.description}', ${category.retailerCategoryCode})" data-dismiss="modal">Select</a>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="categorySearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>