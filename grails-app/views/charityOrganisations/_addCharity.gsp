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
            <label for="name" class="col-3 offset-1 col-form-label text-right">Charity / Group Description</label>

            <div class="input-group col-4">
                <g:textField name="name" value="${charity?.name}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="reference" class="col-3 offset-1 col-form-label text-right">Supplier Reference</label>

            <div class="input-group col-4">
                <g:textField name="reference" value="${charity?.reference}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="contactName" class="col-3 offset-1 col-form-label text-right">Contact Name</label>

            <div class="input-group col-4">
                <g:textField name="contactName" value="${charity?.contactName}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="phoneNumber" class="col-3 offset-1 col-form-label text-right">Contact Telephone</label>

            <div class="input-group col-4">
                <g:textField name="phoneNumber" value="${charity?.phoneNumber}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="email" class="col-3 offset-1 col-form-label text-right">Contact Email</label>

            <div class="input-group col-4">
                <g:textField name="email" value="${charity?.email}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="customerReference" class="col-3 offset-1 col-form-label text-right">Customer Reference</label>

            <div class="input-group col-4">
                <g:textField name="customerReference" value="${charity?.customerReference}"
                             class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressBuildingNumberOrName" class="col-3 offset-1 col-form-label text-right">Address Building Number or Name</label>

            <div class="input-group col-4">
                <g:textField name="addressBuildingNumberOrName" value="${charity?.addressBuildingNumberOrName}"
                             class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressLine1" class="col-3 offset-1 col-form-label text-right">Address Line 1</label>

            <div class="input-group col-4">
                <g:textField name="addressLine1" value="${charity?.addressLine1}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressLine2" class="col-3 offset-1 col-form-label text-right">Address Line 2</label>

            <div class="input-group col-4">
                <g:textField name="addressLine2" value="${charity?.addressLine2}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressTown" class="col-3 offset-1 col-form-label text-right">Address Town</label>

            <div class="input-group col-4">
                <g:textField name="addressTown" value="${charity?.addressTown}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressCounty" class="col-3 offset-1 col-form-label text-right">Address County</label>

            <div class="input-group col-4">
                <g:textField name="addressCounty" value="${charity?.addressCounty}" class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressCountry" class="col-3 offset-1 col-form-label text-right">Address Country</label>

            <div class="input-group col-4">
                <g:textField name="addressCountry" value="${charity?.addressCountry}"
                             class="form-control bottom-border"/>
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressPostCode" class="col-3 offset-1 col-form-label text-right">Address Postcode</label>

            <div class="input-group col-4">
                <g:textField name="addressPostCode" value="${charity?.addressPostCode}"
                             class="form-control bottom-border"/>
            </div>
        </div>
    </g:form>
</div>

<div class="modal-footer">
    <button type="button" id="cancelAddSupplierButton" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
    <g:if test="${enableSave}">
        <button type="button" id="saveSupplierButton" class="btn btn-success" onclick="saveSupplier();">Save</button>
    </g:if>
    <g:else>
        <button type="button" id="saveSupplierButton" class="btn btn-success" disabled onclick="saveSupplier();">Save</button>
    </g:else>
</div>