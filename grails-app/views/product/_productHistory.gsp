<g:if test="${!productHistoryMap || productHistoryMap?.size() == 0}">
    <div class="row mx-5 text-left">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No history found for this product.</div>
    </div>
</g:if>

<g:each var="mapElement" in="${productHistoryMap}" status="k">
    <div id="prod-history-${k+1}" class="row mx-5 pt-2 pb-2" title="Click to view product history." style='border-width: thick; border-style: groove;' >
        <div id="prod-history-${k+1}-effective-date" class="row mx-5 w-100"><h4><b>Effective ${mapElement.key}</b></h4></div>
        <g:each in="${mapElement.value}" var="productHistory" status="i">
            <div id="prod-history-${k+1}-${i+1}-detail" class="mx-5 w-100 wl-striped${i%2} hoverable"><g:productHistory productHistory="${productHistory}"/></div>
        </g:each>
    </div>
</g:each>
