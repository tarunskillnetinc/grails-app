<g:if test="${!promotions || promotions?.size() == 0}">
    <div class="row mx-5 text-center">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">This product is not currently on promotion.</div>
    </div>
</g:if>

<g:each in="${promotions}" var="promotion" status="i">
    <div class="row mx-5 pt-2 pb-2 wl-striped${i % 2} hoverable" title="Click to view promotion." style="cursor: pointer;" onclick="document.location.href='${createLink(controller: 'promotion', action: 'maintenance', params: [promotionId: promotion.id])}';">
        <div class="col-1 my-auto">${promotion.id}</div>
        <div class="col-2 my-auto"><g:message code="PromotionType.${promotion.type.name()}" /></div>
        <div class="col-5 my-auto">${promotion.description}</div>
        <div class="col-1 my-auto"><g:formatDate format="dd/MM/yyyy" date="${promotion.startDate}" /></div>
        <div class="col-1 my-auto">
            <g:if test="${promotion.endDate}">
                <g:formatDate format="dd/MM/yyyy" date="${promotion.endDate}" />
            </g:if>
            <g:else>
                None
            </g:else>
        </div>
        <div class="col-2 my-auto">${promotion.retailerPromotionId}</div>
    </div>
</g:each>