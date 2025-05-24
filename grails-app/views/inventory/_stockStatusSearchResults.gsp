<div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border align-content-center">
    <div class="col-1 font-weight-bold">ID</div>
    <div class="col-2 font-weight-bold">Status</div>
    <div class="col-3 font-weight-bold">Total Number of Stores</div>
    <div class="col-3 font-weight-bold">Total Number of Products</div>
    <div class="col-2 font-weight-bold">Completed</div>
</div>

<g:if test="${stockAdjustmentResults && !stockAdjustmentResults.isEmpty()}">
    <g:each in="${stockAdjustmentResults}" var="result" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 table-wl bottom-border align-content-center wl-striped${i%2}">
            <div class="col-1">${result.id}</div>
            <div class="col-2">${result.status}</div>
            <div class="col-3">${result.totalStores}</div>
            <div class="col-3">${result.totalProducts}</div>
            <div class="col-2">
                <g:if test="${result.dateActioned}">
                    <g:formatDate date="${result.dateActioned}" format="dd/MM/yyyy HH:mm:ss" />
                </g:if>
                <g:else>
                    -
                </g:else>
            </div>
        </div>
    </g:each>
</g:if>
<g:else>
    <div class="row ml-0 mr-0 pt-4 pb-4 table-wl bottom-border text-center align-content-center">
        <div class="col-12">No stock adjustments found</div>
    </div>
</g:else>