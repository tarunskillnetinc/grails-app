<div class="modal-header">
    <g:if test="${editing}">
        <h2>Edit Reason Code</h2>
    </g:if>
    <g:else>
        <h2>Add Reason Code</h2>
    </g:else>
</div>

<script>
    $(document).ready(function() {
        if (${renderErrors ?: false}) {
            const errorContainer = $('#error-container');
            const errors = JSON.parse("${errors}".replaceAll('&quot;', '"'));
            let errorListHtml = "";

            if (errors) {
                for (let i = 0; i < errors.length; ++i) {
                    errorListHtml += "<li>" + errors[i] + "</li>\n";
                }
                errorContainer.html(errorListHtml);
            }
        }
    });

    function updateAdditionalFuncSection() {
        const promptId = $('#prompt-age-section');
        const returnStock = $('#return-stock-section');
        const adjustInOut = $('#adjust-in-out-section');

        switch ($('#type').val()) {
            case "PAID_OUT":
                promptId.show();

                returnStock.hide();
                adjustInOut.hide();
                break;
            case "REFUND":
                returnStock.show();

                promptId.hide();
                adjustInOut.hide();
                break;
            case "PRODUCT_LIST":
                adjustInOut.show();

                promptId.hide();
                returnStock.hide();
                break;
            default:
                promptId.hide();
                returnStock.hide();
                adjustInOut.hide();
                break;
        }
    }
</script>

<div class="modal-body">
    <g:if test="${renderErrors}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <ul id="error-container">
                </ul>
            </div>
        </section>
    </g:if>

    <g:set var="type" value="${reasonCode?.type?.name()}"/>

    <form id="edit-code-form" name="edit-code-form">
        <div class="row form-group mb-4">
            <label for="description" class="col-3 offset-1 col-form-label-mandatory text-right" >Description:</label>
            <div class="input-group col-4">
                <g:textField name="description" value="${reasonCode?.description}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="promptForText" class="col-3 offset-1 col-form-label text-right">Prompt for Text:</label>
            <div class="input-group col-4">
                <g:checkBox name="promptForText" value="${reasonCode?.promptForText}" class="col-1 form-check-input wl-checkbox" />
            </div>
        </div>

        <div class="row form-group mb-4" id="prompt-age-section">
            <label for="promptAge" class="col-3 offset-1 col-form-label text-right">Prompt for Age:</label>
            <div class="input-group col-4 align-content-start">
                <g:checkBox name="promptAge" value="${reasonCode?.additionalFunctionality}" class="col-1 form-check-input wl-checkbox" />
            </div>
        </div>

        <div class="row form-group mb-4" id="return-stock-section">
            <label for="returnStock" class="col-3 offset-1 col-form-label text-right">Return to Stock:</label>
            <div class="input-group col-4 align-content-start">
                <g:checkBox name="returnStock" value="${reasonCode?.additionalFunctionality}" class="col-1 form-check-input wl-checkbox" />
            </div>
        </div>

        <div class="row form-group mb-4" id="adjust-in-out-section">
            <label class="col-3 offset-1 col-form-label text-right">Adjust In/Out:</label>
            <div class="input-group col-2 align-content-center">
                <label for="in">Adjust in: </label>&#160;&#160;
                <input type="radio" id="in" name="adjust" value="IN" <g:if test="${reasonCode?.additionalFunctionality}">checked</g:if>>
            </div>
            <div class="input-group col-2 align-content-center">
                <label for="out">Adjust out: </label>&#160;&#160;
                <input type="radio" id="out" name="adjust" value="OUT" <g:if test="${!reasonCode?.additionalFunctionality}">checked</g:if>>
            </div>
        </div>
        
        <div class="row form-group mb-4">
            <label for="preferredReasonCode" class="col-3 offset-1 col-form-label text-right">Preferred Reason Code:</label>
            <div class="input-group col-4">
                <g:checkBox name="preferredReasonCode" value="${reasonCode?.preferredReasonCode}" class="col-1 form-check-input wl-checkbox" />
            </div>
        </div>

        <g:if test="${!editing}">
            <div class="row form-group mb-4">
                <label for="type" class="col-3 offset-1 col-form-label-mandatory text-right" >Type:</label>
                <select name="type" id="type" class="col-4 form-control select-border" onchange="updateAdditionalFuncSection();">
                    <option value="PAID_OUT" <g:if test="${type == "PAID_OUT"}">selected</g:if>>Paid Out</option>
                    <option value="CUSTOMER_REFUSAL" <g:if test="${type == "CUSTOMER_REFUSAL"}">selected</g:if>>Customer Refusal</option>
                    <option value="LINE_VOID" <g:if test="${type == "LINE_VOID"}">selected</g:if>>Line Void</option>
                    <option value="MARKDOWN" <g:if test="${type == "MARKDOWN"}">selected</g:if>>Markdown</option>
                    <option value="REFUND" <g:if test="${type == "REFUND"}">selected</g:if>>Refund</option>
                    <option value="TENDER_RECONCILIATION_VARIANCE" <g:if test="${type == "TENDER_RECONCILIATION_VARIANCE"}">selected</g:if>>Tender Reconciliation Variance</option>
                    <option value="PRODUCT_LIST" <g:if test="${type == "PRODUCT_LIST"}">selected</g:if>>Product List</option>
                </select>
            </div>
        </g:if>
        <g:else>
            <g:hiddenField name="type" value="${reasonCode?.type}"/>
        </g:else>

        <g:hiddenField name="id" value="${reasonCode?.id}"/>
        <g:hiddenField name="code" value="${reasonCode?.code}"/>
        <g:hiddenField name="deleted" value="${reasonCode?.deleted}"/>
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
    </form>
</div>

<div class="modal-footer">
    <button type="button" id="cancel-edit-btn" class="btn btn-wl" onclick="closeModal();">Cancel</button>
    <button type="button" id="save-code-btn" class="btn btn-success" onclick="ajaxSave('${editing}');">Save</button>
</div>
