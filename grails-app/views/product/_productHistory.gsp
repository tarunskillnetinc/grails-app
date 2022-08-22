<g:if test="${!productHistoryList || productHistoryList?.size() == 0}">
    <div class="row mx-5 text-left">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No history found for this product.</div>
    </div>
</g:if>

<g:each in="${productHistoryList}" var="productHistory" status="i">
    <div class="row mx-5 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view product history.">
        <div class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${productHistory?.effectiveDate?.toDate()}"/></div>
        <div class="col-9 my-auto">
            <g:productHistory productHistory="${productHistory}"/>
        </div>
    </div>
</g:each>
