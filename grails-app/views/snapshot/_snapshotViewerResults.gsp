<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-3 font-weight-bold">Count Date</div>
    <div class="col-3 font-weight-bold">Total</div>
    <div class="col-3 font-weight-bold">Variance</div>
    <div class="col-3 font-weight-bold">Counted By</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!snapshots || snapshots?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No snapshots found.</div>
    </g:if>
    <g:each in="${snapshots}" var="snapshot" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="showSnapshotModal(${snapshot.id});">
            <div id="count-date-${i + 1}" class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy" date="${snapshot.countDate.toDate()}" /></div>
            <div id="total-${i + 1}" class="col-3 my-auto"><g:formatNumber number="${snapshot.totals.sum { it.value } ?: BigDecimal.ZERO}" type="currency" /></div>
            <div id="variance-${i + 1}" class="col-3 my-auto"><g:formatNumber number="${snapshot.variance ?: BigDecimal.ZERO}" type="currency" /></div>
            <div id="counted-by-${i + 1}" class="col-3 my-auto">${snapshot.countedByUsersName}</div>
        </div>
    </g:each>
</div>