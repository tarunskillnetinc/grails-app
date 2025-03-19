<script type="application/javascript">
    $(document).ready(function() {
        $('.step').on('click', function() {
            $('html, body').animate({ scrollTop: 0 }, 'fast');
        });
    });
</script>

<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
    <div class="col-1 font-weight-bold"><a id="storeId" href="#" onclick="getReceipts('storeId', ${sort == 'storeId' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Store Name & ID</a></div>
    <div class="col-1 font-weight-bold"><a id="tillId" href="#" onclick="getReceipts('tillId', ${sort == 'tillId' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Till ID</a></div>
    <div class="col-2 font-weight-bold"><a id="transactionId" href="#" onclick="getReceipts('transactionId', ${sort == 'transactionId' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Transaction Number</a></div>
    <div class="col-2 font-weight-bold"><a id="transactionAmount" href="#" onclick="getReceipts('transactionAmount', ${sort == 'transactionAmount' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Transaction Amount</a></div>
    <div class="col-2 font-weight-bold"><a id="dateGenerated" href="#" onclick="getReceipts('dateGenerated', ${sort == 'dateGenerated' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Transaction Date</a></div>
    <div class="col-2 font-weight-bold"><a id="paymentMethod" href="#" onclick="getReceipts('paymentMethod', ${sort == 'paymentMethod' ? order == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''}, ${offset}, ${max})">Payment Method</a></div>
    <div class="col-2 font-weight-bold text-center">View</div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${!combinedResults || combinedResults?.size() == 0}">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No receipts found.</div>
    </g:if>

    <g:each in="${combinedResults}" var="item" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="showReceiptModal(${item.receipt.id});">
            <div id="store-id-${i + 1}" class="col-1 my-auto">${item.store.config.storeName} - ${item.receipt.storeId}</div>
            <div id="till-id-${i + 1}" class="col-1 my-auto">${item.receipt.tillId}</div>
            <div id="transaction-id-${i + 1}" class="col-2 my-auto">${item.receipt.transactionId}</div>
            <div id="transaction-amount-${i + 1}" class="col-2 my-auto">
                <g:formatNumber number="${item.receipt.transactionAmount ?: BigDecimal.ZERO}" type="currency" />
            </div>
            <div id="date-generated-${i + 1}" class="col-2 my-auto"><g:formatDate format="dd/MM/yyyy HH:mm" date="${item.receipt.dateGenerated?.toDate()}" timeZone="Europe/London" /></div>
            <div id="payment-method-${i + 1}" class="col-2 my-auto"><g:message code="TransactionPaymentMethodType.${item.receipt.paymentMethod}" /></div>
            <div id="view-${i + 1}" class="col-2 my-auto text-center">
                <button id="receipt-${i + 1}" class="btn btn-wl mx-2" onclick="">Receipt</button>
                <button id="details-${i + 1}" class="btn btn-info mx-2" onclick="">Details</button>
            </div>
        </div>
    </g:each>
</div>

<g:if test="${totalCount > 0}">
    <div class="my-3 text-right">
        <div>Displaying ${offset ? offset + 1 : 1} - ${((offset ?: 0) + (combinedResults?.size() ?: 0))} of ${totalCount} result${totalCount > 1 ? 's' : ''}</div>
        <div class="mt-3">
            <util:remotePaginate action="ajaxGetReceipts" total="${totalCount ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 50}" params="[startDate: startDate, endDate: endDate, tillId: tillId, transactionId: transactionId]" />
        </div>
    </div>
</g:if>