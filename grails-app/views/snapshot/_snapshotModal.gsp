<div class="row mt-3 mb-2">
    <div class="col-8 pr-0" style="-ms-flex: 0 0 63%; flex: 0 0 63%; max-width: 63%;">
        <g:if test="${snapshot?.countDate == null}">
            <div class="row cash-up-by">
                <p class="mx-auto">Cash up by <a id="cashUpByValueLink" href="#" class="disabled">value</a>, <a id="cashUpByDenominationLink" href="#" onclick="changeCashUpType('DENOMINATION');">denomination</a> or <a id="cashUpByTotalsLink" href="#" onclick="changeCashUpType('TOTALS');">totals</a></p>
            </div>

            <div id="cashUpContainer" class="mt-3 mr-4">
                <g:render template="/shift/cashUpByValue" />
            </div>
        </g:if>
        <g:else>
            <g:render template="snapshotSummaryModal" model="[snapshot: snapshot]" />
        </g:else>
    </div>
    <div class="col-4 pl-0" style="-ms-flex: 0 0 37%; flex: 0 0 37%; max-width: 37%;">
        <g:render template="safeReport" model="[snapshot: snapshot]" />
    </div>
</div>