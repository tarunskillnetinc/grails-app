<h3 class="text-center mt-5">Data Sync Service</h3>
<p class="text-center">Controls synchronisation of data down to tills.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Data sync service is <span class="badge badge-${dataSyncServiceQueue.consumers > 0 ? 'success' : 'danger'}">${dataSyncServiceQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${dataSyncServiceQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${dataSyncServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${dataSyncServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-3">Transaction Processor</h3>
<p class="text-center">Ensures validity of all transactions, saves locally created products, posts all transactions onto the processing exchange.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Transaction processor is <span class="badge badge-${transactionProcessorQueue.consumers > 0 ? 'success' : 'danger'}">${transactionProcessorQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${transactionProcessorQueue.messages}.</div>
        <div>Latest activity:
            <g:if test="${transactionProcessorQueue?.idle_since}">
                <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${transactionProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
            </g:if>
            <g:else>
                Now.
            </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">KPI Processor</h3>
<p class="text-center">Processes transactions from tills into KPI data.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>KPI processor is <span class="badge badge-${kpiProcessorQueue.consumers > 0 ? 'success' : 'danger'}">${kpiProcessorQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${kpiProcessorQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${kpiProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${kpiProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">Reporting Processor</h3>
<p class="text-center">Processes transactions from tills into report data.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Reporting processor is <span class="badge badge-${reportingProcessorQueue.consumers > 0 ? 'success' : 'danger'}">${reportingProcessorQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${reportingProcessorQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${reportingProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${reportingProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">Shift Processor</h3>
<p class="text-center">Processes transactions from tills into shift/cash data.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Shift processor is <span class="badge badge-${shiftProcessorQueue.consumers > 0 ? 'success' : 'danger'}">${shiftProcessorQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${shiftProcessorQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${shiftProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${shiftProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">Stock Processor</h3>
<p class="text-center">Processes transactions from tills into stock movements.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Stock processor is <span class="badge badge-${stockProcessorQueue.consumers > 0 ? 'success' : 'danger'}">${stockProcessorQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${stockProcessorQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${stockProcessorQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${stockProcessorQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">Receipt Service</h3>
<p class="text-center">Populates receipt viewer.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Receipt service is <span class="badge badge-${receiptServiceQueue.consumers > 0 ? 'success' : 'danger'}">${receiptServiceQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${receiptServiceQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${receiptServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${receiptServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">Nisa Service</h3>
<p class="text-center">Controls integration into Nisa.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Nisa service is <span class="badge badge-${nisaServiceQueue.consumers > 0 ? 'success' : 'danger'}">${nisaServiceQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${nisaServiceQueue.messages}.</div>
        <div>Latest activity:
        <g:if test="${nisaServiceQueue?.idle_since}">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${nisaServiceQueue?.idle_since?.toDate() ?: new Date()}" />.
        </g:if>
        <g:else>
            Now.
        </g:else>
        </div>
    </div>
</div>

<h3 class="text-center mt-5">Raw Transaction Writer</h3>
<p class="text-center">Emergency data store of transactions.</p>

<div class="card bg-light border-wl col-6 offset-3">
    <div class="card-body text-center">
        <div>Raw transaction writer is <span class="badge badge-${rawTransactionWriterQueue.consumers > 0 ? 'success' : 'danger'}">${rawTransactionWriterQueue.consumers > 0 ? 'Online' : 'Offline'}</span></div>
        <div>Messages waiting to be processed: ${rawTransactionWriterQueue.messages}.</div>
        <div>Latest activity:
            <g:if test="${rawTransactionWriterQueue?.idle_since}">
                <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${rawTransactionWriterQueue?.idle_since?.toDate() ?: new Date()}" />.
            </g:if>
            <g:else>
                Now.
            </g:else>
        </div>
    </div>
</div>