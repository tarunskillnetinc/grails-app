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
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchTransactions({max: '${max}', offset: '${offset}', sortColumn: 'transactionId', sortOrder: ${sortColumn == 'transactionId' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Transaction Id</a></div>
    <div class="col-1 font-weight-bold"><a href="#" onclick="searchTransactions({max: '${max}', offset: '${offset}', sortColumn: 'storeId', sortOrder: ${sortColumn == 'store_id' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Store Id</a></div>
    <div class="col font-weight-bold"><a href="#" onclick="searchTransactions({max: '${max}', offset: '${offset}', sortColumn: 'storeName', sortOrder: ${sortColumn == 'storeName' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Store Name</a></div>
    <div class="col-2 font-weight-bold"><a href="#" onclick="searchTransactions({max: '${max}', offset: '${offset}', sortColumn: 'transactionTotal', sortOrder: ${sortColumn == 'transactionTotal' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Spend Amount</a></div>
    <div class="col-3 font-weight-bold"><a href="#" onclick="searchTransactions({max: '${max}', offset: '${offset}', sortColumn: 'transactionTimestamp', sortOrder: ${sortColumn == 'transactionTimestamp' ? sortOrder == 'asc' ? '\'desc\'' : '\'asc\'' : '\'asc\''} })">Transaction Date</a></div>
</div>

<div class="d-flex justify-content-center">
    <div id="loading-indicator" class="spinner-border" role="status" style="display: none;">
        <span class="sr-only">Loading...</span>
    </div>
</div>

<div id="search-results">
    <g:if test="${transactions == null}">
        <div class="row ml-0 mr-0 text-center">
            <div class="col pt-2 pb-2 text-center my-auto wl-striped0">Please enter search criteria to display transactional information.</div>
        </div>
    </g:if>

    <g:if test="${transactions?.size() == 0}">
        <div class="row ml-0 mr-0 text-center">
            <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">No transactions found.</div>
        </div>
    </g:if>

    <g:each in="${transactions}" var="transaction" status="i">
        <div id="transaction-result-${i+1}" class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to view." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'transactionDetails', params:[cardNumber: cardNumber, memberId: transaction.memberId, transactionId: transaction.transactionId])}';" >
            <div id="transaction-result-${i+1}-transactionId" class="col-2 text-truncate">${transaction.transactionId}</div>
            <div id="transaction-result-${i+1}-storeId" class="col-1 text-truncate">${transaction.storeId}</div>
            <div id="transaction-result-${i+1}-storeName" class="col text-truncate">${transaction.store.name}</div>
            <div id="transaction-result-${i+1}-transactionTotal" class="col-2 text-truncate mask-money">${String.format("%.2f", transaction.transactionTotal)}</div>
            <div id="transaction-result-${i+1}-transactionTimestamp" class="col-3 text-truncate">${transaction.transactionTimestamp.toString("hh:mm:ss dd/MM/yyyy")}</div>
        </div>
    </g:each>
</div>

<div class="my-3 text-right">
    <util:remotePaginate action="ajaxMemberTransactions" total="${totalResults ?: 0}" update="results-container" offset="${offset ?: 0}" max="${max ?: 20}" params="[cardNumber: cardNumber,
                                                                                                                                                                    searchTerm: searchTerm,
                                                                                                                                                                    searchBy: searchBy,
                                                                                                                                                                    startWindow: startWindow,
                                                                                                                                                                    endWindow: endWindow,
                                                                                                                                                                    minAmount: minAmount,
                                                                                                                                                                    maxAmount: maxAmount,
                                                                                                                                                                    sortColumn: sortColumn,
                                                                                                                                                                    sortOrder: sortOrder]" />
</div>