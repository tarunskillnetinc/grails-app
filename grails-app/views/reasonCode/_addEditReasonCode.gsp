<div class="modal-header">
    <g:if test="${editing}">
        <h2>Edit Reason Code</h2>
    </g:if>
    <g:else>
        <h2>Add Reason Code</h2>
    </g:else>
</div>

<div class="modal-body">
    <g:hasErrors bean="${reasonCode}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${reasonCode}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="edit-code-form">
        <div class="row form-group mb-4">
            <label for="description" class="col-3 offset-1 col-form-label-mandatory text-right" >Description:</label>
            <div class="input-group col-4">
                <g:textField id="description" name="description" value="${reasonCode?.description}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="additionalFunctionality" class="col-3 offset-1 col-form-label text-right">Additional Functionality:</label>
            <div class="input-group col-4 align-content-start">
                <g:checkBox name="additionalFunctionality" value="${reasonCode?.additionalFunctionality}" class="col-1 form-check-input wl-checkbox" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="promptForText" class="col-3 offset-1 col-form-label text-right">Prompt for Text:</label>
            <div class="input-group col-4">
                <g:checkBox name="promptForText" value="${reasonCode?.promptForText}" class="col-1 form-check-input wl-checkbox" />
            </div>
        </div>

        <g:if test="${!editing}">
            <div class="row form-group mb-4">
                <label for="type" class="col-3 offset-1 col-form-label-mandatory text-right" >Type:</label>
                <select name="type" id="type" class="col-4 form-control select-border">
                    <option value="PAID_OUT">Paid Out</option>
                    <option value="CUSTOMER_REFUSAL">Customer Refusal</option>
                    <option value="LINE_VOID">Line Void</option>
                    <option value="MARKDOWN">Markdown</option>
                    <option value="REFUND">Refund</option>
                    <option value="TENDER_RECONCILIATION_VARIANCE">Tender Reconciliation Variance</option>
                    <option value="PRODUCT_LIST">Product List</option>
                </select>
            </div>
        </g:if>
        <g:else>
            <g:hiddenField name="type" value="${reasonCode?.type}"/>
        </g:else>

        <g:hiddenField name="id" value="${reasonCode?.id}"/>
        <g:hiddenField name="code" value="${reasonCode?.code}"/>
        <g:hiddenField name="deleted" value="${reasonCode?.deleted}"/>
        <g:hiddenField name="preferredReasonCode" value="${reasonCode?.preferredReasonCode}"/>
        <g:hiddenField name="secret" value="${reasonCode?.secret}"/>

        <g:if test="${reasonCode?.retailerId}">
            <g:hiddenField name="retailerId" value="${reasonCode?.retailerId}"/>
        </g:if>
        <g:elseif test="${retailerId}">
            <g:hiddenField name="retailerId" value="${retailerId}"/>
        </g:elseif>
        <g:else>
            <g:hiddenField name="retailerId" value="${sec.loggedInUserInfo(field: 'retailerId')}"/>
        </g:else>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancel-edit-btn" class="btn btn-wl" onclick="closeModal();">Cancel</button>
    <button type="button" id="save-code-btn" class="btn btn-success" onclick="console.log('saving')">Save</button>
</div>