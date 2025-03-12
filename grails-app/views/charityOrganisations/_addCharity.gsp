<script>
    $('#isDefault').on('click', function (event) {
        event.preventDefault(); // Prevent the checkbox from being checked immediately

        if (confirm('Are you sure you want to set this Charity Organisation as the default? This action will replace the existing default Charity Organisation.')) {
            $(this).prop('checked', !$(this).prop('checked')); // Toggle the checkbox state
        }
    });

    $('#specialAppeals').on('click', function (event) {
        event.preventDefault(); // Prevent the checkbox from being checked immediately

        if (confirm('Are you sure you want to set this Charity Organisation as the special appeal? This action will replace the existing special appeal Charity Organisation.')) {
            $(this).prop('checked', !$(this).prop('checked')); // Toggle the checkbox state
        }
    });
</script>

<div class="modal-header">
    <h2>${isUpdate ? "Edit Charity Organisation" : "Add Charity Organisation"}</h2>
</div>

<div class="modal-body">
    <g:hasErrors bean="${charity}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${charity}" as="list"/>
            </div>
        </section>
    </g:hasErrors>

    <g:form name="addCharityForm">
        <g:hiddenField name="id" value="${charity?.id}"/>

        <div class="row form-group mb-4">
            <label for="type"
                   class="col-5 offset-1 col-form-label text-right">Organisation Type</label>

            <div class="input-group col-4">
                <g:select id="organisationType" name="type"
                          valueMessagePrefix="CharityType"
                          from="${typeOptions}"
                          value="${charity?.type}"
                          noSelection="['': 'Select Charity Organisation']"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="organisationName"
                   class="col-5 offset-1 col-form-label text-right">Charity / Group Description</label>

            <div class="input-group col-4">
                <g:textField name="organisationName" value="${charity?.organisationName}"
                             class="form-control bottom-border" maxlength="60"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="memberNumber" class="col-5 offset-1 col-form-label text-right">Charity Member Number</label>

            <div class="input-group col-4">
                <g:textField name="memberNumber" value="${charity?.memberNumber}" class="form-control bottom-border"
                             maxlength="20"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="active" class="col-5 offset-1 col-form-label text-right pr-4">Active</label>
            <g:checkBox id="active" name="active" checked="${isUpdate ? charity?.active : true}"
                        disabled="${charity?.isDefault || charity?.specialAppeals}"
                        title="Active cannot be disabled unless Default and Special Appeals are both cleared. "/>
        </div>

        <div class="row form-group mb-4">
            <label for="isDefault"
                   class="col-5 offset-1 col-form-label text-right pr-4">Default Charity Organisation</label>
            <g:checkBox name="isDefault" id="isDefault" checked="${charity?.isDefault}"
                        disabled="${charity?.isDefault}"
                        title="Default cannot be disabled unless another Organisation is made default."/>
        </div>

        <div class="row form-group mb-4">
            <label for="specialAppeals" class="col-5 offset-1 col-form-label text-right pr-4">Special Appeals</label>
            <g:checkBox name="specialAppeals" id="specialAppeals" checked="${charity?.specialAppeals}"
                        disabled="${charity?.specialAppeals}"
                        title="Special Appeals cannot be disabled unless another Organisation is made Special Appeal."/>
        </div>

    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddSupplierButton" class="btn btn-secondary"
            data-dismiss="modal">Cancel</button>
    <g:if test="${enableSave}">
        <button type="button" id="saveSupplierButton" class="btn btn-success"
                onclick="saveCharity();">Save</button>
    </g:if>
    <g:else>
        <button type="button" id="saveSupplierButton" class="btn btn-success" disabled
                onclick="saveCharity();">Save</button>
    </g:else>
</div>