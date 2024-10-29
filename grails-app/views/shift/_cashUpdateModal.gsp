<div class="modal-header">
    <h2>${isAddFloat ? 'Add Float' : 'Cash Lift'}</h2>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5"><p>Please complete the ${isAddFloat ? 'add float' : 'cash lift'} process.</p></div>

    <g:form name="modal-form">
        <div class="row justify-content-center">
            <div class="col-md-10">
                <g:if test="${safeLocations?.collect()?.size() > 1}">
                    <div class="form-group row align-items-center">
                        <label for="fromLocation" class="col-sm-5 col-form-label text-right">Please select a safe location:</label>
                        <div class="col-sm-7">
                            <g:select name="fromLocation"
                                      from="${safeLocations}"
                                      optionKey="id"
                                      optionValue="description"
                                      class="form-control select-border"
                                      style="min-width: 200px; max-width: 300px;"/>
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <div class="form-group row align-items-center">
                        <label for="fromLocation" class="col-sm-5 col-form-label text-right">Target safe:</label>
                        <div class="col-sm-7">
                            <g:textField name="fromLocation"
                                         value="${safeLocations?.collect()?[0].id}"
                                         class="form-control"
                                         style="min-width: 200px; max-width: 300px;"/>
                        </div>
                    </div>
                </g:else>

                <g:if test="${isAddFloat}">
                    <div class="row form-group mb-4 mt-5 justify-content-center">
                        <g:render template="/shift/textField" model="[name: 'cashTotal', label: 'Cash Total', value: 0.00, labelCols: 2]" />
                        <g:render template="/shift/textField" model="[name: 'vouchersTotal', label: 'Vouchers Total', value: 0.00, labelCols: 2]" />
                    </div>
                </g:if>
                <g:else>
                    <div class="form-group row align-items-center mt-4">
                        <label for="cashTotal" class="col-sm-5 col-form-label text-right">Cash Total:</label>
                        <div class="col-sm-7">
                            <g:textField name="cashTotal"
                                         value="0.00"
                                         class="form-control"
                                         style="min-width: 200px; max-width: 300px;"/>
                        </div>
                    </div>
                </g:else>
            </div>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <g:if test="${error}">
        <section id="alerts-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${error}</div>
        </section>
    </g:if>
    <button type="button" id="cancelModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
    <g:if test="${!safeLocations?.collect()?.isEmpty()}">
        <button type="button" id="saveBankCashInButton" class="btn btn-success" onclick="saveModal('BANKING');">Save</button>
    </g:if>
</div>