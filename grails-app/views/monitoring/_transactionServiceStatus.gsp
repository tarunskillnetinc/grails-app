<h3 id="data-sync-service-title" class="text-center mt-5">Data Sync Service</h3>
<p class="text-center">Controls synchronisation of data down to tills.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Data sync service is <span id="data-sync-service-availability" class="badge badge-${dataSyncServiceQueue?.consumers > 0 ? 'success' : 'danger'}">${dataSyncServiceQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="data-sync-service-messages">Messages waiting to be processed: ${ dataSyncServiceQueue != null ? dataSyncServiceQueue.messages : 0}.</div>
        <div id="data-sync-service-activity">Latest activity:
        <g:if test="${dataSyncServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${dataSyncServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="transaction-processor-title" class="text-center mt-3">Transaction Processor</h3>
<p class="text-center">Ensures validity of all transactions, saves locally created products, posts all transactions onto the processing exchange.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Transaction processor is <span id="transaction-processor-availability" class="badge badge-${transactionProcessorQueue?.consumers > 0 ? 'success' : 'danger'}">${transactionProcessorQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="transaction-processor-messages">Messages waiting to be processed: ${transactionProcessorQueue != null ? transactionProcessorQueue.messages : 0}.</div>
        <div id="transaction-processor-activity">Latest activity:
            <g:if test="${transactionProcessorQueue?.idle_since}">
                <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${transactionProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
            </g:if>
            <g:else>
                Now.
            </g:else>
        </div>
    </div>
</div>

<h3 id="kpi-processor-title" class="text-center mt-5">KPI Processor</h3>
<p class="text-center">Processes transactions from tills into KPI data.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>KPI processor is <span id="kpi-processor-availability" class="badge badge-${kpiProcessorQueue?.consumers > 0 ? 'success' : 'danger'}">${kpiProcessorQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="kpi-processor-messages">Messages waiting to be processed: ${kpiProcessorQueue != null ? kpiProcessorQueue.messages : 0}.</div>
        <div id="kpi-processor-activity">Latest activity:
        <g:if test="${kpiProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${kpiProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="reporting-processor-title" class="text-center mt-5">Reporting Processor</h3>
<p class="text-center">Processes transactions from tills into report data.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Reporting processor is <span id="reporting-processor-availability" class="badge badge-${reportingProcessorQueue?.consumers > 0 ? 'success' : 'danger'}">${reportingProcessorQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="reporting-processor-messages">Messages waiting to be processed: ${reportingProcessorQueue != null ? reportingProcessorQueue.messages : 0}.</div>
        <div id="reporting-processor-activity">Latest activity:
        <g:if test="${reportingProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${reportingProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="shift-processor-title" class="text-center mt-5">Shift Processor</h3>
<p class="text-center">Processes transactions from tills into shift/cash data.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Shift processor is <span id="shift-processor-availability" class="badge badge-${shiftProcessorQueue?.consumers > 0 ? 'success' : 'danger'}">${shiftProcessorQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="shift-processor-messages">Messages waiting to be processed: ${shiftProcessorQueue != null ? shiftProcessorQueue.messages : 0}.</div>
        <div id="shift-processor-activity">Latest activity:
        <g:if test="${shiftProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${shiftProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="stock-processor-title" class="text-center mt-5">Stock Processor</h3>
<p class="text-center">Processes transactions from tills into stock movements.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Stock processor is <span id="stock-processor-availability" class="badge badge-${stockProcessorQueue?.consumers > 0 ? 'success' : 'danger'}">${stockProcessorQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="stock-processor-messages">Messages waiting to be processed: ${stockProcessorQueue != null ? stockProcessorQueue.messages : 0}.</div>
        <div id="stock-processor-activity">Latest activity:
        <g:if test="${stockProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${stockProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="receipt-service-title" class="text-center mt-5">Receipt Service</h3>
<p class="text-center">Populates receipt viewer.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Receipt service is <span id="receipt-service-availability" class="badge badge-${receiptServiceQueue?.consumers > 0 ? 'success' : 'danger'}">${receiptServiceQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="receipt-service-messages">Messages waiting to be processed: ${receiptServiceQueue != null ? receiptServiceQueue.messages : 0}.</div>
        <div id="receipt-service-activity">Latest activity:
        <g:if test="${receiptServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${receiptServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="nisa-service-title" class="text-center mt-5">Nisa Service</h3>
<p class="text-center">Controls integration into Nisa.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Nisa service is <span id="nisa-service-availability" class="badge badge-${nisaServiceQueue?.consumers > 0 ? 'success' : 'danger'}">${nisaServiceQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="nisa-service-messages">Messages waiting to be processed: ${nisaServiceQueue != null ? nisaServiceQueue.messages : 0}.</div>
        <div id="nisa-service-activity">Latest activity:
        <g:if test="${nisaServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${nisaServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="raw-transaction-title" class="text-center mt-5">Raw Transaction Writer</h3>
<p class="text-center">Emergency data store of transactions.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Raw transaction writer is <span id="raw-transaction-availability" class="badge badge-${rawTransactionWriterQueue?.consumers > 0 ? 'success' : 'danger'}">${rawTransactionWriterQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="raw-transaction-messages">Messages waiting to be processed: ${rawTransactionWriterQueue != null ? rawTransactionWriterQueue.messages : 0}.</div>
        <div id="raw-transaction-activity">Latest activity:
            <g:if test="${rawTransactionWriterQueue?.idle_since}">
                <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${rawTransactionWriterQueue?.idle_since?.toDate() ?: new Date()}" />.
            </g:if>
            <g:else>
                Now.
            </g:else>
        </div>
    </div>
</div>

<h3 id="snappy-service-title" class="text-center mt-5">Snappy Service</h3>
<p class="text-center">Service handling subscriptions to Snappy Shopper, updating menus, prices etc.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Snappy service is <span id="snappy-service-availability" class="badge badge-${snappyServiceQueue?.consumers > 0 ? 'success' : 'danger'}">${snappyServiceQueue?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="snappy-service-messages">Messages waiting to be processed: ${snappyServiceQueue != null ? snappyServiceQueue.messages : 0}.</div>
        <div id="snappy-service-activity">Latest activity:
        <g:if test="${snappyServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${snappyServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="loyalty-member-service-title" class="text-center mt-5">Loyalty Member Service</h3>
<p class="text-center">Loyalty Member service is used to update member totals and points.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Loyalty Member Service is <span id="loyalty-member-service-availability" class="badge badge-${loyaltyMemberService?.consumers > 0 ? 'success' : 'danger'}">${loyaltyMemberService?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="loyalty-member-messages">Messages waiting to be processed: ${loyaltyMemberService != null ? loyaltyMemberService.messages : 0}.</div>
        <div id="loyalty-member-activity">Latest activity:
        <g:if test="${loyaltyMemberService?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${loyaltyMemberService?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="loyalty-transaction-service-title" class="text-center mt-5">Loyalty Transaction Service</h3>
<p class="text-center">Loyalty Transaction service is used to add transactions into the database.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Loyalty Transaction Service is <span id="loyalty-transaction-service-availability" class="badge badge-${loyaltyTransactionService?.consumers > 0 ? 'success' : 'danger'}">${loyaltyTransactionService?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="loyalty-transaction-messages">Messages waiting to be processed: ${loyaltyTransactionService != null ? loyaltyTransactionService.messages : 0}.</div>
        <div id="loyalty-transaction-activity">Latest activity:
        <g:if test="${loyaltyTransactionService?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${loyaltyTransactionService?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 id="offer-service-title" class="text-center mt-5">Offer Service</h3>
<p class="text-center">Offer service is used to handle offer data and add it to the database.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Offer Service is <span id="offer-service-availability" class="badge badge-${offerService?.consumers > 0 ? 'success' : 'danger'}">${offerService?.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div id="offer-messages">Messages waiting to be processed: ${offerService != null ? offerService.messages : 0}.</div>
        <div id="offer-activity">Latest activity:
        <g:if test="${offerService?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${offerService?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>