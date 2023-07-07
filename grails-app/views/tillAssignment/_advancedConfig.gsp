<div class="modal-header">
    <h2>Advanced Configuration</h2>
</div>

<div class="modal-body">
    <g:hasErrors bean="${config}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${config}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="advancedConfigForm">
        <g:hiddenField name="id" value="${config?.id}" />

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">SCP Transaction End Indicator</label>

            <div class="input-group col-4">
                <g:field type="text" maxlength="100" name="scpTxnIndicator" value="${config?.scpTxnEndIndicator}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">Baud Rate</label>

            <div class="input-group col-4">
                <g:field type="number" min="0" name="baudRate" value="${config?.baudRate}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">PPOS Control Bar</label>

            <div class="input-group col-4">
                <g:select name="pposControlBar" from="${['BELOW_WINDOW', 'RIGHT_OF_WINDOW', 'NO_CONTROL_BAR']}" value="${config?.pposControlBar}" class="form-control select-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">PPOS Admin Status</label>

            <div class="col-1 form-check form-check-inline">
                <div class="col-1 form-check form-check-inline">
                    <g:radio class="form-check-input ml-1 wl-radio" type="radio" name="pposAdmin" id="pposAdminEnabled" value="true" checked="${config?.pposAdmin}"/>
                    <label class="form-check-label" for="pposAdmin">Enabled</label>
                </div>
                <div class="col-5 form-check form-check-inline">
                    <g:radio class="form-check-input ml-6 wl-radio" type="radio" name="pposAdmin" id="pposAdminDisabled" value="false" checked="${!config?.pposAdmin}"/>
                    <label class="form-check-label" for="pposAdmin">Disabled</label>
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">PPOS Refund Status</label>

            <div class="col-1 form-check form-check-inline">
                <div class="col-1 form-check form-check-inline">
                    <g:radio class="form-check-input ml-1 wl-radio" type="radio" name="pposRefund" id="pposRefundEnabled" value="true" checked="${config?.pposRefund}"/>
                    <label class="form-check-label" for="pposRefund">Enabled</label>
                </div>
                <div class="col-5 form-check form-check-inline">
                    <g:radio class="form-check-input ml-6 wl-radio" type="radio" name="pposRefund" id="pposRefundDisabled" value="false" checked="${!config?.pposRefund}"/>
                    <label class="form-check-label" for="pposRefund">Disabled</label>
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">PPOS Smart Token Status</label>

            <div class="col-1 form-check form-check-inline">
                <div class="col-1 form-check form-check-inline">
                    <g:radio class="form-check-input ml-1 wl-radio" type="radio" name="pposSmartToken" id="pposSmartTokenEnabled" value="true" checked="${config?.pposSmartToken}"/>
                    <label class="form-check-label" for="pposSmartToken">Enabled</label>
                </div>
                <div class="col-5 form-check form-check-inline">
                    <g:radio class="form-check-input ml-6 wl-radio" type="radio" name="pposSmartToken" id="pposSmartTokenDisabled" value="false" checked="${!config?.pposSmartToken}"/>
                    <label class="form-check-label" for="pposSmartToken">Disabled</label>
                </div>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label class="col-3 offset-1 col-form-label text-right">Print Card Receipts</label>

            <div class="col-1 form-check form-check-inline">
                <div class="col-1 form-check form-check-inline">
                    <g:radio class="form-check-input ml-1 wl-radio" type="radio" name="printCardReceipts" id="printCardReceiptsEnabled" value="true" checked="${config?.printCardReceipts}"/>
                    <label class="form-check-label" for="printCardReceipts">Enabled</label>
                </div>
                <div class="col-5 form-check form-check-inline">
                    <g:radio class="form-check-input ml-6 wl-radio" type="radio" name="printCardReceipts" id="printCardReceiptsDisabled" value="false" checked="${!config?.printCardReceipts}"/>
                    <label class="form-check-label" for="printCardReceipts">Disabled</label>
                </div>
            </div>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddTillButton" class="btn btn-secondary" onclick="cancelTill();">Cancel</button>
    <button type="button" id="saveAdvancedConfigurationButton" class="btn btn-success" onclick="saveAdvancedConfiguration();">Save</button>
</div>