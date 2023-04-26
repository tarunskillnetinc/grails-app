<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold">Store ID</div>
    <div class="col-2 font-weight-bold">Till ID</div>
    <div class="col-2 font-weight-bold">Transaction Number</div>
    <div class="col-3 font-weight-bold">Transaction Amount</div>
    <div class="col-3 font-weight-bold">Date Generated</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!receipts || receipts?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No receipts found.</div>
    </g:if>

    <g:each in="${receipts}" var="receipt" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="showReceiptModal(${receipt.id});">
            <div id="store-id-${i + 1}" class="col-2 my-auto">${receipt.storeId}</div>
            <div id="till-id-${i + 1}" class="col-2 my-auto">${receipt.tillId}</div>
            <div id="transaction-id-${i + 1}" class="col-2 my-auto">${receipt.transactionId}</div>
            <div id="transaction-amount-${i + 1}" class="col-3 my-auto">
                <g:formatNumber number="${receipt.receiptLines?.find{ it.type.name() == 'TOTAL' }?.total ?: BigDecimal.ZERO}" type="currency" />
            </div>
            <div id="date-generated-${i + 1}" class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy" date="${receipt.dateGenerated.toDate()}" /></div>
        </div>
    </g:each>
</div>

<g:if test="${receipts?.totalCount > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${offset ? offset + 1 : 1} - ${((offset ?: 0) + (receipts?.size() ?: 0))} of ${receipts?.totalCount} result${receipts?.totalCount > 1 ? 's' : ''}</div>
        <div class="mt-3"><g:wlPagination totalResults="${receipts?.totalCount}" offset="${offset}" max="${max}" searchFunction="getReceipts" /></div>
    </div>
</g:if>