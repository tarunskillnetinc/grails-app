<div class="shift-report mr-4 px-3 py-5">
    <div class="row mb-3">
        <h2 class="mx-auto">Shift Report</h2>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            First transaction:
        </div>

        <div class="col-6 text-right">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${shift.firstTransactionDate.toDate()}" />
        </div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Last transaction:
        </div>

        <div class="col-6 text-right">
            <g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${shift.lastTransactionDate.toDate()}" />
        </div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Shift number:
        </div>

        <div class="col-6 text-right">
            ${shift.shiftNumber}
        </div>
    </div>

    <div class="row">
        <div class="col-12">
            <hr />
        </div>
    </div>

    <div class="row font-weight-bolder">
        <h3 class="mx-auto">VAT</h3>
    </div>

    <div class="row font-weight-bolder">
        <div class="col-2 text-right">
            Code
        </div>
        <div class="col-2">
            Rate
        </div>
        <div class="col-3">
            Net
        </div>
        <div class="col-2">
            VAT
        </div>
        <div class="col-3 text-right">
            Total
        </div>
    </div>

    <g:each in="${shift.vatTotals?.sort { it.code }}" var="vatTotal">
        <div class="row">
            <div class="col-2 text-right">
                ${vatTotal.code}
            </div>
            <div class="col-2">
                ${vatTotal.rate}%
            </div>
            <div class="col-3">
                <g:formatNumber number="${vatTotal.netTotal}" type="currency" />
            </div>
            <div class="col-2">
                <g:formatNumber number="${vatTotal.vatTotal}" type="currency" />
            </div>
            <div class="col-3 text-right">
                <g:formatNumber number="${vatTotal.grossTotal}" type="currency" />
            </div>
        </div>
    </g:each>

    <div class="row">
        <div class="col-12">
            <hr />
        </div>
    </div>

    <div class="row font-weight-bolder">
        <h3 class="mx-auto">Sales</h3>
    </div>

    <div class="row font-weight-bolder">
        <div class="col-2 text-right">
            Qty
        </div>
        <div class="col-7">
            Category
        </div>
        <div class="col-3 text-right">
            Total
        </div>
    </div>

    <g:each in="${shift.sales?.sort { it.categoryName }}" var="saleTotal">
        <div class="row">
            <div class="col-2 text-right">
                ${saleTotal.quantity}
            </div>
            <div class="col-7">
                ${saleTotal.categoryName}
            </div>
            <div class="col-3 text-right">
                <g:formatNumber number="${saleTotal.value}" type="currency" />
            </div>
        </div>
    </g:each>

    <div class="row">
        <div class="col-12">
            <hr />
        </div>
    </div>

    <div class="row font-weight-bolder">
        <h3 class="mx-auto">Refunds</h3>
    </div>

    <div class="row font-weight-bolder">
        <div class="col-2 text-right">
            Qty
        </div>
        <div class="col-7">
            Category
        </div>
        <div class="col-3 text-right">
            Total
        </div>
    </div>

    <g:each in="${shift.refunds?.sort { it.categoryName }}" var="refundTotal">
        <div class="row">
            <div class="col-2 text-right">
                ${refundTotal.quantity}
            </div>
            <div class="col-7">
                ${refundTotal.categoryName}
            </div>
            <div class="col-3 text-right">
                <g:formatNumber number="${refundTotal.value}" type="currency" />
            </div>
        </div>
    </g:each>

    <div class="row">
        <div class="col-12">
            <hr />
        </div>
    </div>

    <div class="row font-weight-bolder">
        <h3 class="mx-auto">Tenders</h3>
    </div>

    <div class="row font-weight-bolder">
        <div class="col-2 text-right">
            Qty
        </div>
        <div class="col-7">
            Type
        </div>
        <div class="col-3 text-right">
            Total
        </div>
    </div>

    <g:each in="${shift.tenderTotals?.sort { it.tenderType.name() }}" var="tenderTotal">
        <div class="row">
            <div class="col-2 text-right">
                ${tenderTotal.quantity}
            </div>
            <div class="col-7">
                <g:message code="TenderType.${tenderTotal.tenderType}" />
            </div>
            <div class="col-3 text-right">
                <g:formatNumber number="${tenderTotal.value}" type="currency" />
            </div>
        </div>
    </g:each>

    <div class="row">
        <div class="col-12">
            <hr />
        </div>
    </div>

    <div class="row font-weight-bolder">
        <h3 class="mx-auto">Till Events</h3>
    </div>

    <div class="row font-weight-bolder">
        <div class="col-2 text-right">
            Qty
        </div>
        <div class="col-7">
            Description
        </div>
        <div class="col-3 text-right">
            Total
        </div>
    </div>

    <g:each in="${shift.tillControlEvents?.sort { it.tillControlEventType.name() }}" var="tillControlEventTotal">
        <div class="row">
            <div class="col-2 text-right">
                ${tillControlEventTotal.quantity}
            </div>
            <div class="col-7">
                <g:message code="TillControlEventType.${tillControlEventTotal.tillControlEventType}" />
            </div>
            <div class="col-3 text-right">
                <g:formatNumber number="${tillControlEventTotal.value}" type="currency" />
            </div>
        </div>
    </g:each>

    <div class="row">
        <div class="col-12">
            <hr />
        </div>
    </div>

    <div class="row mb-3">
        <h2 class="mx-auto">Totals</h2>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Sales:
        </div>

        <div class="col-6 text-right">
            <g:formatNumber number="${shift.sales.sum { it.value } ?: 0}" type="currency" />
        </div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Refunds:
        </div>

        <div class="col-6 text-right">
            <g:formatNumber number="${shift.refunds.sum { it.value }?.negate() ?: 0}" type="currency" />
        </div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Net sales:
        </div>

        <div class="col-6 text-right">
            <g:formatNumber number="${(shift.sales.sum { it.value } ?: 0) - (shift.refunds.sum { it.value } ?: 0)}" type="currency" />
        </div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Cash in drawer:
        </div>

        <div class="col-6 text-right">
            <g:formatNumber number="${shift.cashInDrawer ?: 0}" type="currency" />
        </div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Customer count:
        </div>

        <div class="col-6 text-right">${shift.customerCount ?: 0}</div>
    </div>

    <div class="row">
        <div class="col-6 font-weight-bolder">
            Average spend:
        </div>

        <div class="col-6 text-right">
            <g:formatNumber number="${(shift.sales.sum{ it.value } ?: 0) / (shift.customerCount ?: 1)}" type="currency" />
        </div>
    </div>
</div>