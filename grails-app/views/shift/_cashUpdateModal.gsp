<div class="modal-header">
    <h2>${isAddFloat ? 'Add Float' : 'Tender Lift'}</h2>
</div>

<g:if test="${error}">
    <section id="alerts-container" class="container-fluid">
        <div class="alert alert-danger alert-wl mx-0" role="alert">${error}</div>
    </section>
</g:if>

<div class="modal-body">
    <div class="text-center mt-4 mb-5"><p>Please complete the ${isAddFloat ? 'add float' : 'tender lift'} process.</p></div>

    <g:form name="modal-form">
        <div class="row justify-content-center">
            <g:hiddenField name="retailerId" id="retailerId" value="${retailerId}" />
            <g:hiddenField name="storeId" id="storeId" value="${storeId}" />
            <g:hiddenField name="tillId" id="tillId" value="${tillId}" />
            <g:hiddenField name="shiftId" id="shiftId" value="${shiftId}" />
            <g:hiddenField name="isAddFloat" id="isAddFloat" value="${isAddFloat}" />
            <div class="col-md-10">
                <g:if test="${safeLocations?.collect()?.size() > 1}">
                    <div class="form-group row align-items-center">
                        <label for="safeId" class="col-sm-5 col-form-label text-right">Please select ${isAddFloat ? 'source' : 'target'} safe location:</label>
                        <div class="col-sm-7">
                            <g:select name="safeId"
                                      from="${safeLocations}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${primarySafe?.id}"
                                      class="form-control select-border"
                                      style="min-width: 200px; max-width: 300px;"/>
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <div class="form-group row align-items-center">
                        <label for="safeId" class="col-sm-5 col-form-label text-right">${isAddFloat ? 'Source' : 'Target'} safe:</label>
                        <div class="col-sm-7">
                            <g:hiddenField name="safeId" value="${safeLocations?.collect()?[0].id}" />
                            <g:textField name="safeDescription"
                                         value="${safeLocations?.collect()?[0].description}"
                                         class="form-control"
                                         style="min-width: 200px; max-width: 300px;"/>
                        </div>
                    </div>
                </g:else>

                <g:if test="${isAddFloat}">
                    <div class="row form-group mb-4 mt-5 justify-content-center">
                        <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value:  cashAmount  ? cashAmount : '0.00' , labelCols: 2]" />
                        <g:render template="/shift/textField" model="[name: 'vouchersTotal', label: 'Vouchers Total', value:  voucherAmount  ? voucherAmount : '0.00' , labelCols: 2]" />
                    </div>
                </g:if>
                <g:else>
                    <div class="form-group row align-items-center">
                        <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: 0.00, labelCols: 5]" />
                    </div>
                </g:else>
            </div>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
    <g:if test="${!safeLocations?.collect()?.isEmpty()}">
        <button type="button" id="saveBankCashInButton" class="btn btn-success" onclick="saveCashUpdate();">Save</button>
    </g:if>
</div>