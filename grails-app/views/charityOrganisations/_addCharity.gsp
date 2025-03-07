<div class="modal-header">
    <h2>Add Supplier</h2>
</div>

<div class="modal-body">
    <div class="text-center mt-4 mb-5">Please complete the following form to add a new supplier.</div>

    <g:hasErrors bean="${supplier}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${supplier}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="addSupplierForm">
        <g:hiddenField name="id" value="${supplier?.id}" />

        <div class="row form-group mb-4">
            <label for="name" class="col-3 offset-1 col-form-label text-right">Supplier Name</label>

            <div class="input-group col-4">
                <g:textField name="name" value="${supplier?.name}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="reference" class="col-3 offset-1 col-form-label text-right">Supplier Reference</label>

            <div class="input-group col-4">
                <g:textField name="reference" value="${supplier?.reference}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="contactName" class="col-3 offset-1 col-form-label text-right">Contact Name</label>

            <div class="input-group col-4">
                <g:textField name="contactName" value="${supplier?.contactName}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="phoneNumber" class="col-3 offset-1 col-form-label text-right">Contact Telephone</label>

            <div class="input-group col-4">
                <g:textField name="phoneNumber" value="${supplier?.phoneNumber}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="email" class="col-3 offset-1 col-form-label text-right">Contact Email</label>

            <div class="input-group col-4">
                <g:textField name="email" value="${supplier?.email}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="customerReference" class="col-3 offset-1 col-form-label text-right">Customer Reference</label>

            <div class="input-group col-4">
                <g:textField name="customerReference" value="${supplier?.customerReference}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressBuildingNumberOrName" class="col-3 offset-1 col-form-label text-right">Address Building Number or Name</label>

            <div class="input-group col-4">
                <g:textField name="addressBuildingNumberOrName" value="${supplier?.addressBuildingNumberOrName}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressLine1" class="col-3 offset-1 col-form-label text-right">Address Line 1</label>

            <div class="input-group col-4">
                <g:textField name="addressLine1" value="${supplier?.addressLine1}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressLine2" class="col-3 offset-1 col-form-label text-right">Address Line 2</label>

            <div class="input-group col-4">
                <g:textField name="addressLine2" value="${supplier?.addressLine2}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressTown" class="col-3 offset-1 col-form-label text-right">Address Town</label>

            <div class="input-group col-4">
                <g:textField name="addressTown" value="${supplier?.addressTown}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressCounty" class="col-3 offset-1 col-form-label text-right">Address County</label>

            <div class="input-group col-4">
                <g:textField name="addressCounty" value="${supplier?.addressCounty}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressCountry" class="col-3 offset-1 col-form-label text-right">Address Country</label>

            <div class="input-group col-4">
                <g:textField name="addressCountry" value="${supplier?.addressCountry}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="addressPostCode" class="col-3 offset-1 col-form-label text-right">Address Postcode</label>

            <div class="input-group col-4">
                <g:textField name="addressPostCode" value="${supplier?.addressPostCode}" class="form-control bottom-border" />
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