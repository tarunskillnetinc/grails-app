<script type="application/javascript">
    $(document).ready(function() {
        // Assuming your page buttons have a specific class (e.g., "page-button")
        $('.step').on('click', function() {
            // Scroll to the top of the page with a smooth animation
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-2 font-weight-bold"><a id="storeId" href="#" onclick="getReceipts('storeId', ${sort == 'storeId' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Store ID</a></div>
    <div class="col-2 font-weight-bold"><a id="tillId" href="#" onclick="getReceipts('tillId', ${sort == 'tillId' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Till ID</a></div>
    <div class="col-2 font-weight-bold"><a id="transactionId" href="#" onclick="getReceipts('transactionId', ${sort == 'transactionId' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Transaction Number</a></div>
    <div class="col-3 font-weight-bold"><a id="transactionAmount" href="#" onclick="getReceipts('transactionAmount', ${sort == 'transactionAmount' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Transaction Amount</a></div>
    <div class="col-3 font-weight-bold"><a id="dateGenerated" href="#" onclick="getReceipts('dateGenerated', ${sort == 'dateGenerated' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Transaction Date</a></div>
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
            <div id="date-generated-${i + 1}" class="col-3 my-auto"><g:formatDate format="dd/MM/yyyy HH:mm" date="${receipt.dateGenerated.toDate()}" /></div>
        </div>
    </g:each>
</div>

<g:if test="${totalCount > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${offset ? offset + 1 : 1} - ${((offset ?: 0) + (receipts?.size() ?: 0))} of ${totalCount} result${totalCount > 1 ? 's' : ''}</div>
        <div class="mt-3">
            <util:remotePaginate action="ajaxGetReceipts" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[startDate: startDate, endDate: endDate]" />
        </div>
    </div>
</g:if>