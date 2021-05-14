<g:form name="cashUpForm">
    <g:hiddenField name="cashUpBy" value="VALUE" />

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'fiftyPounds', label: '£50', value: values?.fiftyPounds ?: 0.00]" />
        <g:render template="textField" model="[name: 'twentyPounds', label: '£20', value: values?.twentyPounds ?: 0.00, labelCols: '2']" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'tenPounds', label: '£10', value: values?.tenPounds ?: 0.00]" />
        <g:render template="textField" model="[name: 'fivePounds', label: '£5', value: values?.fivePounds ?: 0.00, labelCols: '2']" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'twoPounds', label: '£2', value: values?.twoPounds ?: 0.00]" />
        <g:render template="textField" model="[name: 'onePounds', label: '£1', value: values?.onePounds ?: 0.00, labelCols: '2']" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'fiftyPences', label: '50p', value: values?.fiftyPences ?: 0.00]" />
        <g:render template="textField" model="[name: 'twentyPences', label: '20p', value: values?.twentyPences ?: 0.00, labelCols: '2']" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'tenPences', label: '10p', value: values?.tenPences ?: 0.00]" />
        <g:render template="textField" model="[name: 'fivePences', label: '5p', value: values?.fivePences ?: 0.00, labelCols: '2']" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'twoPences', label: '2p', value: values?.twoPences ?: 0.00]" />
        <g:render template="textField" model="[name: 'onePences', label: '1p', value: values?.onePences ?: 0.00, labelCols: '2']" />
    </div>

    <div class="row form-group mb-4">
        <g:render template="textField" model="[name: 'vouchersTotal', label: 'Vouchers', value: values?.vouchersTotal ?: 0.00]" />
    </div>
</g:form>