<g:if test="${!promotions || promotions?.size() == 0}">
    <div class="row mx-5 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">
            <g:if test="${promotionsError}">
                There was an error fetching promotions.
            </g:if>
            <g:else>
                This product is not currently on promotion.
            </g:else>
        </div>
    </div>
</g:if>

<g:each in="${promotions}" var="promotion" status="i">
    <div id="promotion-${i+1}" class="row mx-5 pt-2 pb-2 wl-striped${i % 2} hoverable" title="Click to view promotion." style="cursor: pointer;" onclick="document.location.href='${createLink(controller: 'promotion', action: 'maintenance', params: [promotionId: promotion.id])}';">
        <div id="promotion-${i+1}-id" class="col-1 my-auto">${promotion.id}</div>
        <div id="promotion-${i+1}-type" class="col-2 my-auto"><g:message code="PromotionType.${promotion.type.name()}" /></div>
        <div id="promotion-${i+1}-description" class="col-5 my-auto">${promotion.description}</div>
        <div id="promotion-${i+1}-start-date" class="col-1 my-auto"><g:formatDate format="dd/MM/yyyy" date="${promotion.startDate.toDate()}" /></div>
        <div id="promotion-${i+1}-end-date" class="col-1 my-auto">
            <g:if test="${promotion.endDate}">
                <g:formatDate format="dd/MM/yyyy" date="${promotion.endDate.toDate()}" />
            </g:if>
            <g:else>
                None
            </g:else>
        </div>
        <div id="promotion-${i+1}-retailer-reference" class="col-2 my-auto">${promotion.retailerPromotionId}</div>
    </div>
</g:each>