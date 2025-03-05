<div class="modal-header">
    <h2>${isUpdate ? "Edit Charity Organisation" : "Add Charity Organisation"}</h2>

    <div>
        <button type="button" id="cancelAddSupplierButton" class="btn btn-secondary"
                data-dismiss="modal">Cancel</button>
        <g:if test="${enableSave}">
            <button type="button" id="saveSupplierButton" class="btn btn-success"
                    onclick="saveSupplier();">Save</button>
        </g:if>
        <g:else>
            <button type="button" id="saveSupplierButton" class="btn btn-success" disabled
                    onclick="saveSupplier();">Save</button>
        </g:else>
    </div>
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
            <label for="organisationName"
                   class="col-3 offset-1 col-form-label text-right">Charity / Group Description</label>

            <div class="input-group col-4">
                <g:textField name="organisationName" value="${charity?.organisationName}"
                             class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="memberNumber" class="col-3 offset-1 col-form-label text-right">Member Number</label>

            <div class="input-group col-4">
                <g:textField name="memberNumber" value="${charity?.memberNumber}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="active" class="col-5 col-form-label text-right pr-4">Active</label>
            <g:checkBox id="active" name="status" checked="${charity?.active}"/>
        </div>

        <div class="row form-group mb-4">
            <label for="defaultCharityGroup"
                   class="col-5 col-form-label text-right pr-4">Default Charity Organisation</label>
            <g:checkBox id="defaultCharityGroup" name="defaultCharityGroup" checked="${charity?.defaultCharityGroup}"/>
        </div>

        <div class="row form-group mb-4">
            <label for="specialAppeals" class="col-5 col-form-label text-right pr-4">Special Appeals</label>
            <g:checkBox id="specialAppeals" name="specialAppeals" checked="${charity?.specialAppeals}"/>
        </div>
    </g:form>
</div>