<g:form name="cashUpForm">
    <g:hiddenField name="cashUpBy" value="TOTALS" />

    <div class="row form-group mb-4">
        <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: values?.cashTotal ?: 0.00, labelCols: 5]" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="/shift/textField" model="[name: 'vouchersTotal', label: 'Vouchers Total', value: values?.vouchersTotal ?: 0.00, labelCols: 5]" />
    </div>
</g:form>