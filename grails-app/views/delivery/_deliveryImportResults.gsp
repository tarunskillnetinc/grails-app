<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-8 font-weight-bold">Order No. / Supplier Reference</div>
    <div class="col-2 font-weight-bold">File Status</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!deliveries || deliveries?.size() == 0}">
    </g:if>

    <g:each in="${deliveries}" var="delivery" status="i">
        <div id="delivery-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
            <div id="delivery-supplier-reference-${i + 1}" class="col-8 my-auto">${delivery.supplierReference}</div>
            <div id="delivery-valid-${i + 1}" class="col-2 my-auto">${delivery.valid}</div>
        </div>
    </g:each>
</div>