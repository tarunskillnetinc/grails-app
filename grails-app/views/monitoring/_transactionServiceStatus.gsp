<h3 class="text-center mt-3">Transaction Processor</h3>
<p class="text-center">Populates reporting data, KPIs, stock updates, saves locally created products.</p>

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