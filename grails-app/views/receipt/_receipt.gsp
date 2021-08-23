<div class="receipt">
    <g:each in="${receipt.receiptLines?.sort{it.id}}" var="receiptLine">
        <g:receiptLine receiptLine="${receiptLine}" containsModifiers="${containsModifiers}" duplicate="${receipt.isPrinted()}" firstHorizontalLine="${firstHorizontalLineId == receiptLine.id}" maxTotalLength="${maxTotalLength}" maxVatLength="${maxVatLength}" />
    </g:each>
</div>