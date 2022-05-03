<g:form name="cashUpForm">
    <g:hiddenField name="cashUpBy" value="DENOMINATION" />

    <div class="row form-group mb-4">
        <label for="fiftyPounds" class="col-3 col-form-label text-right">£50</label>
        <div class="col-3">
            <g:field type="number" name="fiftyPounds" value="${values?.fiftyPounds?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>

        <label for="twentyPounds" class="col-2 col-form-label text-right">£20</label>
        <div class="col-3">
            <g:field type="number" name="twentyPounds" value="${values?.twentyPounds?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>
    </div>

    <div class="row form-group mb-4">
        <label for="tenPounds" class="col-3 col-form-label text-right">£10</label>
        <div class="col-3">
            <g:field type="number" name="tenPounds" value="${values?.tenPounds?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>

        <label for="fivePounds" class="col-2 col-form-label text-right">£5</label>
        <div class="col-3">
            <g:field type="number" name="fivePounds" value="${values?.fivePounds?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>
    </div>

    <div class="row form-group mb-4">
        <label for="twoPounds" class="col-3 col-form-label text-right">£2</label>
        <div class="col-3">
            <g:field type="number" name="twoPounds" value="${values?.twoPounds?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>

        <label for="onePounds" class="col-2 col-form-label text-right">£1</label>
        <div class="col-3">
            <g:field type="number" name="onePounds" value="${values?.onePounds?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>
    </div>

    <div class="row form-group mb-4">
        <label for="fiftyPences" class="col-3 col-form-label text-right">50p</label>
        <div class="col-3">
            <g:field type="number" name="fiftyPences" value="${values?.fiftyPences?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>

        <label for="twentyPences" class="col-2 col-form-label text-right">20p</label>
        <div class="col-3">
            <g:field type="number" name="twentyPences" value="${values?.twentyPences?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>
    </div>

    <div class="row form-group mb-4">
        <label for="tenPences" class="col-3 col-form-label text-right">10p</label>
        <div class="col-3">
            <g:field type="number" name="tenPences" value="${values?.tenPences?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>

        <label for="fivePences" class="col-2 col-form-label text-right">5p</label>
        <div class="col-3">
            <g:field type="number" name="fivePences" value="${values?.fivePences?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>
    </div>

    <div class="row form-group mb-4">
        <label for="twoPences" class="col-3 col-form-label text-right">2p</label>
        <div class="col-3">
            <g:field type="number" name="twoPences" value="${values?.twoPences?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>

        <label for="onePences" class="col-2 col-form-label text-right">1p</label>
        <div class="col-3">
            <g:field type="number" name="onePences" value="${values?.onePences?.intValue() ?: '0'}" step="1" min="0" class="form-control denomination" />
        </div>
    </div>

    <div class="row form-group mb-4">
        <g:render template="/shift/textField" model="[name: 'vouchersTotal', label: 'Vouchers', value: values?.vouchersTotal ?: 0.00]" />
    </div>
</g:form>