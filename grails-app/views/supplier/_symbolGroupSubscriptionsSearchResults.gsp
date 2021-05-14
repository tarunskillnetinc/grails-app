<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-3 font-weight-bold">Supplier Name</div>
    <div class="col-3 font-weight-bold">Status</div>
    <div class="col-3 font-weight-bold">Last Product Download</div>
    <div class="col-3 font-weight-bold">Last Promotions Download</div>
</div>

<div class="d-flex justify-content-center">
    <div id="subscriptions-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="subscriptions-search-results">
    <g:if test="${!symbolGroupSubscriptions || symbolGroupSubscriptions?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">You do not have any supplier affiliations.</div>
    </g:if>

    <g:each in="${symbolGroupSubscriptions}" var="symbolGroupSubscription" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="showSubscriptionModal(${symbolGroupSubscription.id});">
            <div class="col-3 my-auto">${symbolGroupSubscription.symbolGroup.name}</div>
            <div class="col-3 my-auto"><g:message code="SymbolGroupSubscriptionStatus.${symbolGroupSubscription.status}" /></div>
            <div class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy" date="${symbolGroupSubscription.lastProductDownload}" /></div>
            <div class="col-3 my-auto">${symbolGroupSubscription.contactName}</div>
        </div>
    </g:each>
</div>