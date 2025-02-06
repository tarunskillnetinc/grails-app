<div id="modal-header" class="modal-header">
    <h2>Spot Check</h2>
</div>

<style>
.spot-check-list {
    width: 80%;
    margin: 0 auto;
}
.spot-check-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px; /* Reduced margin for row spacing */
}
.spot-check-row span:first-child {
    text-align: right;
    width: 45%;
    padding-right: 15px; /* Reduced padding for better fit */
}
.spot-check-row span:last-child {
    text-align: left;
    width: 45%;
    padding-left: 15px;
}
.spot-check-total {
    font-weight: bold;
    border-top: 1px solid black;
    padding-top: 8px; /* Reduced padding for the total row */
    margin-top: 12px;
}
.spot-check-value {
    font-size: 1.3em; /* Keep the same font size as Cash, Card, etc. */
}
.spot-check-row span {
    font-size: 1.3em; /* Keep the same font size for labels (Cash, Card, etc.) */
}
</style>

<div class="row mt-3 mb-2" style="height: 400px;">
    <div class="col-8 pr-0 mx-auto" style="flex: 0 0 70%; max-width: 70%;">

        <div class="row text-center">
            <p class="mx-auto">
                <strong>Spot check for Safe</strong><br />
                <span><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${fetchTime?.toDate() ?: new Date()}" timeZone="Europe/London"/></span>
            </p>
        </div>

        <div class="spot-check-list" style="padding: 10px;">
            <div class="spot-check-row">
                <span class="font-weight-bold">Tender</span>
                <span class="font-weight-bold">Value</span>
            </div>

            <div class="spot-check-row">
                <span>Cash</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${safeSession?.tenderTotals?.find { it.cashTender }?.value ?: BigDecimal.ZERO}" type="currency" />
                </span>
            </div>

            <g:each in="${safeSession?.tenderTotals?.findAll { !it.cashTender }}" var="tenderTotal">
                <div class="spot-check-row">
                    <span>${tenderTotal.tenderTypeName}</span>
                    <span class="spot-check-value">
                        <g:formatNumber number="${tenderTotal?.value ?: BigDecimal.ZERO}" type="currency" />
                    </span>
                </div>
            </g:each>

            <!-- Total Line -->
            <div class="spot-check-row spot-check-total">
                <span class="font-weight-bold">Total</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${(safeSession?.tenderTotals?.sum { it.value } ?: BigDecimal.ZERO)}" type="currency" />
                </span>
            </div>
        </div>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getSafeSessions()">Close</button>
</div>
