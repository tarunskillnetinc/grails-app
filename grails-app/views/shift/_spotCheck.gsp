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
    margin-bottom: 10px;
}
.spot-check-row span:first-child {
    text-align: right;
    width: 50%;
    padding-right: 10px;
}
.spot-check-row span:last-child {
    text-align: left;
    width: 50%;
    padding-left: 10px;
}
</style>

<div class="row mt-3 mb-2" style="height: 300px;">
    <div class="col-8 pr-0 mx-auto" style="flex: 0 0 70%; max-width: 70%;">
        <div class="row text-center">
            <p class="mx-auto">
                Spot check for till ${shift?.tillId} shift ${shift?.shiftNumber} (<g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${fetchTime?.toDate() ?: new Date()}" timeZone="Europe/London"/>)
            </p>
        </div>

        <div class="spot-check-list" style="padding: 10px;">
            <div class="spot-check-row" style="font-weight: bold;">
                <span>Tender</span>
                <span>Value</span>
            </div>

            <div class="spot-check-row">
                <span>Cash</span>
                <span><g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'CASH' }?.value  ?: BigDecimal.ZERO}" type="currency" /></span>
            </div>

            <div class="spot-check-row">
                <span>Card</span>
                <span><g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'CARD' }?.value  ?: BigDecimal.ZERO}" type="currency" /></span>
            </div>

            <div class="spot-check-row">
                <span>Cashback</span>
                <span><g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'CASHBACK' }?.value  ?: BigDecimal.ZERO}" type="currency" /></span>
            </div>

            <div class="spot-check-row">
                <span>Voucher</span>
                <span><g:formatNumber number="${shift?.reconciliationTotals?.find { it.tenderType.name() == 'VOUCHER' }?.value  ?: BigDecimal.ZERO}" type="currency" /></span>
            </div>

        </div>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShiftButton" class="btn btn-secondary" data-dismiss="modal" onclick="getShifts()">Close</button>
</div>