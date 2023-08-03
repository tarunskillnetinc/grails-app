<div class="modal-header">
    <g:if test="${enableEdit}">
        <h2>Edit Till</h2>
    </g:if>
    <g:else>
        <h2>Add Till</h2>
    </g:else>
</div>

<div class="modal-body">
    <g:if test="${enableEdit}">
        <div class="text-center mt-4 mb-5">Please complete the following form to edit an existing till. Till ID and Store are required.</div>
    </g:if>
    <g:else>
        <div class="text-center mt-4 mb-5">Please complete the following form to add a new till. Till ID and Store are required.</div>
    </g:else>

    <g:if test="${saveStoreError}">
        <g:if test="${enableEdit}">
            <div class="alert alert-danger text-center alert-wl mx-0" role="alert">Please ensure a Store is selected when editing a till.</div>
        </g:if>
        <g:else>
            <div class="alert alert-danger text-center alert-wl mx-0" role="alert">Please ensure a Store is selected when adding a till.</div>
        </g:else>
    </g:if>
    <g:elseif test="${saveTillError}">
        <g:if test="${enableEdit}">
            <div class="alert alert-danger text-center alert-wl mx-0" role="alert">Please ensure a unique numerical Till ID is provided when editing a till.</div>
        </g:if>
        <g:else>
            <div class="alert alert-danger text-center alert-wl mx-0" role="alert">Please ensure a unique numerical Till ID is provided when adding a till.</div>
        </g:else>
    </g:elseif>

    <g:hasErrors bean="${till}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${till}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="addTillForm">
        <g:hiddenField name="id" value="${till?.id}" />

        <div class="row form-group mb-4">
            <label for="tillId" class="col-3 offset-1 col-form-label-mandatory text-right" >Till ID </label>

            <div class="input-group col-4">
                <g:field type="number" min="0" max="2147483647" id="tillId" name="tillId" value="${till?.tillId}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="storeId" class="col-3 offset-1 col-form-label-mandatory text-right">Store ID</label>
            <div class="dropdown-content col-4">
                <input type="text" class="form-control bottom-border" placeholder="Search for store.." id="storeIdInput" onkeyup="filter('storeIdInput','storeId')">

                <g:select id="storeId" size="6" name="storeId" style="overflow-y: scroll; overflow-x: hidden;" from="${stores}" optionValue="${{it.config.storeName}}"
                    value="${till?.storeId}"
                    optionKey="${{it.config.storeNumber}}"
                    class="form-control select-border"
                    disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="description" class="col-3 offset-1 col-form-label text-right">Description</label>

            <div class="input-group col-4">
                <g:textField name="description" value="${till?.description}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="serialNumber" class="col-3 offset-1 col-form-label text-right">Serial Number</label>
            <div class="dropdown-content col-4">
                <input type="text" class="form-control bottom-border" placeholder="Search for serial number.." id="serialNumberInput" onkeyup="filter('serialNumberInput','serialNumber')">

                <g:select name="serialNumber" size="6" style="overflow-y: scroll; overflow-x: hidden;" from="${serialNumbers}" optionValue="serialNumber"
                    value="${till?.serialNumber}"
                    optionKey="serialNumber"
                    class="form-control select-border"
                    disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}" />
            </div>
        </div>

        <div id="registration-code-holder" class="border-top" style="display: none;">
            <div id="registration-code" class="text-center mt-4 mb-5">Registration Code</div>
            <div id="registration-code-value" class="text-center mt-4 mb-5"></div>
        </div>

    </g:form>
</div>

<div class="modal-footer">
    <g:if test="${enableEdit}">
        <button type="button" id="saveAddSupplierButton" class="btn btn-info" onclick="generatePin();">Generate PIN</button>
    </g:if>
    <button type="button" id="cancelAddTillButton" class="btn btn-wl" onclick="cancelTill();">Cancel</button>
    <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveTill();">Save</button>
</div>