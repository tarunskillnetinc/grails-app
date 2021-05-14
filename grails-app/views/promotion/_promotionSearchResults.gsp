<%@ page import="java.util.stream.Collectors" %>

<g:if test="${promotions == null}">
    <div class="row text-center">
        <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter a search term.</div>
    </div>
</g:if>

<g:if test="${promotions?.size() == 0}">
    <div class="row text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${promotions}" var="promotion" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="document.location.href='${createLink(action:'maintenance', params:[promotionId: promotion.id])}';">
        <div class="col-2">${promotion.retailerPromotionId}</div>
        <div class="col-5">${promotion.description}</div>
        <div class="col-1">
            <g:checkBox name="promo-${i}-active" value="${promotion.active}" disabled="true"/>
        </div>
        <div class="col-2">${promotion.type}</div>
        <div class="col-2">${promotion.amount}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="promotionSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm, searchBy: searchBy]" />
</div>