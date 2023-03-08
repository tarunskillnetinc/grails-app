<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="row col-11 mt-5">
        <div class="col-3 font-weight-bold">Supplier Name</div>

        <div class="col-2 font-weight-bold">Status</div>

        <div class="col-3 font-weight-bold">Last Product Download</div>

        <div class="col-3 font-weight-bold">Last Promotions Download</div>
    </div>

    <div class="mt-5 table-wl">
        <div class="col-1 font-weight-bold">Action</div>
    </div>
</div>

<div class="d-flex justify-content-center">
    <div id="subscriptions-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="subscriptions-search-results">
    <g:if test="${!symbolGroupSubscriptions || symbolGroupSubscriptions?.size() == 0}">
        <div id="noResultsRow"
             class="col pt-2 pb-2 text-center my-auto wl-striped0">You do not have any supplier affiliations.</div>
    </g:if>

    <g:each in="${symbolGroupSubscriptions}" var="symbolGroupSubscription" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;">
            <div id="supplier-affiliation-${i+1}" class="row col-11" onclick="editSymbolGroupSubscription(${symbolGroupSubscription.id});">
                <div id="supplier-affiliation-${i+1}-name" class="col-3 my-auto">${symbolGroupSubscription.symbolGroup.name}</div>

                <div id="supplier-affiliation-${i+1}-status" class="col-2 my-auto"><g:message
                        code="SymbolGroupSubscriptionStatus.${symbolGroupSubscription.status}"/></div>

                <div id="supplier-affiliation-${i+1}-last-prod-download" class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy HH:mm:ss"
                                                         date="${symbolGroupSubscription.lastProductDownload?.toDate()}"/></div>

                <div id="supplier-affiliation-${i+1}-last-promo-download" class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy HH:mm:ss"
                                                         date="${symbolGroupSubscription.lastPromotionDownload?.toDate()}"/></div>
            </div>

            <g:if test="${symbolGroupSubscription.getSymbolGroup().getId() == 4}">
                <div class="col-1 my-auto">
                    <button class="btn btn-primary" onclick="doSymbolGroupAction(${symbolGroupSubscription.getSymbolGroup().getId()})">Sync</button>
                </div>
            </g:if>
        </div>
    </g:each>
</div>