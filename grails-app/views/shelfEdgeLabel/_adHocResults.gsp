<div class="d-flex justify-content-center">
    <div id="ad-hoc-loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="ad-hoc-results">
    <g:if test="${productLists?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No pending ad-hoc batches.</div>
        </div>
    </g:if>

    <g:each in="${productLists}" var="productList" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Click to view." onclick="document.location.href='${createLink(action:'showAdHocBatch', id: productList.id)}';">
            <div class="col-6">${productList.reasonDescription}</div>
            <div class="col-3">55 (TODO)</div>
        </div>
    </g:each>
</div>