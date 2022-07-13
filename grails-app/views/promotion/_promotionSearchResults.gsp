<%@ page import="java.util.stream.Collectors" %>

<g:if test="${promotions == null}">
    <div class="row ml-0 mr-0 text-center">
        <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please apply some filters.</div>
    </div>
</g:if>

<g:if test="${promotions?.size() == 0}">
    <div class="row ml-0 mr-0 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
    </div>
</g:if>

<g:each in="${promotions}" var="promotion" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'maintenance', params:[promotionId: promotion.id])}';">
        <div class="col-1">${promotion.retailerPromotionId}</div>
        <div class="col-3">${promotion.description}</div>
        <div class="col-1 font-weight-bold">${promotion.updateDatetime.toString("dd/MM/yyyy")}</div>
        <div class="col-1 font-weight-bold">${promotion.startDate.toString("dd/MM/yyyy")}</div>
        <div class="col-1 font-weight-bold">${promotion.endDate.toString("dd/MM/yyyy")}</div>
        <div class="col-1">
            <g:checkBox name="promo-${i}-active" value="${promotion.active}" disabled="true"/>
        </div>
        <div class="col-2"><g:message code="PromotionType.${promotion.type}" /></div>
        <div class="col-1">${promotion.amount}</div>
        <div class="col-1">${promotion.symbolGroupPromotion?.symbolGroup?.name} ${promotion.symbolGroupPromotion?.leafletPromotion ? " - Leaflet" : ""}</div>
    </div>
</g:each>

<div class="my-3 text-right">
    <util:remotePaginate action="promotionSearch" total="${totalResults ?: 0}" update="search-results" offset="${offset ?: 0}" max="${max ?: 50}" params="[searchTerm: searchTerm,
                                                                                                                                                           searchBy: searchBy,
                                                                                                                                                           startDate: startDate,
                                                                                                                                                           endDate: endDate,
                                                                                                                                                           updatedSince: updatedSince,
                                                                                                                                                           type: type,
                                                                                                                                                           supplier: supplier]" />
</div>