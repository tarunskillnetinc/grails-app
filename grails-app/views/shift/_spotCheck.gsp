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
    font-size: 1.4em; /* Slightly reduced font size for total row */
}
.spot-check-value {
    font-size: 1.3em; /* Reduced font size for values */
}
.spot-check-row span {
    font-size: 1.3em; /* Reduced font size for labels (Cash, Card, etc.) */
}
</style>

<div class="row mt-3 mb-2" style="height: 400px;">
    <div class="col-8 pr-0 mx-auto" style="flex: 0 0 70%; max-width: 70%;">
        <div class="row text-center">
            <p class="mx-auto">
                <strong>Spot check for till ${shift?.tillId} shift ${shift?.shiftNumber}</strong><br />
                <span><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${fetchTime?.toDate() ?: new Date()}" timeZone="Europe/London"/></span>
            </p>
        </div>

        <div class="spot-check-list" style="padding: 10px;">
            <div class="spot-check-row" style="font-weight: bold; font-size: 1.4em;"> <!-- Slightly reduced font size for headers -->
                <span>Tender</span>
                <span>Value</span>
            </div>

            <div class="spot-check-row">
                <span>Cash</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'CASH' }?.value ?: BigDecimal.ZERO}" type="currency" />
                </span>
            </div>

            <div class="spot-check-row">
                <span>Card</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'CARD' }?.value ?: BigDecimal.ZERO}" type="currency" />
                </span>
            </div>

            <div class="spot-check-row">
                <span>Cashback</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'CASHBACK' }?.value ?: BigDecimal.ZERO}" type="currency" />
                </span>
            </div>

            <div class="spot-check-row">
                <span>Voucher</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'VOUCHER' }?.value ?: BigDecimal.ZERO}" type="currency" />
                </span>
            </div>

            <!-- Total Line -->
            <div class="spot-check-row spot-check-total">
                <span>Total</span>
                <span class="spot-check-value">
                    <g:formatNumber number="${shift?.reconciliationTotals?.sum { it?.value ?: BigDecimal.ZERO }}" type="currency" />
                </span>
            </div>
        </div>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">Close</button>
</div>
