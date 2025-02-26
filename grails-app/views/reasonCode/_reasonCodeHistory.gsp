<g:if test="${!reasonCodeHistoryMap || reasonCodeHistoryMap?.size() == 0}">
    <div class="row mx-5 text-left">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No history found.</div>
    </div>
</g:if>

<g:each var="mapElement" in="${reasonCodeHistoryMap}" status="k">
    <div id="category-history-${k + 1}" class="row mx-5 pt-2 pb-2" title="Click to view reason code history."
         style='border-width: thick; border-style: groove;'>
        <div id="category-history-${k + 1}-effective-date" class="row mx-5 w-100"><h4><b>Effective ${mapElement.key}</b>
        </h4></div>
        <g:each in="${mapElement.value}" var="reasonCodeHistory" status="i">
            <div id="category-history-${k + 1}-${i + 1}-detail" class="mx-5 w-100 wl-striped${i % 2} hoverable">
                <g:categoryHistory category="${reasonCode}" categoryHistory="${reasonCodeHistory}"/>
            </div>
        </g:each>
    </div>
</g:each>