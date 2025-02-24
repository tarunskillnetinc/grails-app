<g:form name="cashUpForm">
    <g:hiddenField name="cashUpBy" value="TOTALS" />

    <div class="row form-group mb-4">
        <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: values?.cashTotal ?: 0.00, labelCols: 5]" />
    </div>

    <g:each in="${tenderTypes}" var="tenderType" status="i">
        <div class="row form-group mb-4">
            <g:hiddenField name="totals[${i}].tenderTypeId" value="${tenderType.id}" />
            <g:render template="/shift/textField" model="[name: 'totals[' +i +'].value', label: tenderType.name + ' Total', value: values?.totals?.find { it.tenderTypeId == tenderType.id }?.value ?: 0.00, labelCols: 5]" />
        </div>
    </g:each>
</g:form>