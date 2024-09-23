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
        <div class="text-center mt-4">Please complete the following form to edit an existing till. Till Number and Store are required.</div>
    </g:if>
    <g:else>
        <div class="text-center mt-4">Please complete the following form to add a new till. Till Number and Store are required.</div>
    </g:else>

    <g:hasErrors bean="${till}">
        <section id="errors-container" class="container-fluid mt-4">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${till}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <g:form name="addTillForm" class="mt-5">
        <g:hiddenField name="id" value="${till?.id ?: 0}" />

        <div class="row form-group mb-4">
            <label for="tillId" class="col-3 offset-1 col-form-label-mandatory text-right" >Till Number</label>

            <div class="input-group col-4">
                <g:field type="number" min="0" max="2147483647" id="tillId" name="tillId" value="${till?.tillId}" class="form-control bottom-border" />
            </div>
        </div>

        <div class="row form-group mb-4">
            <label for="storeId" class="col-3 offset-1 col-form-label-mandatory text-right">Store</label>
            <div class="dropdown-content col-4">
                <input type="text" class="form-control bottom-border" placeholder="Search for store.." id="storeIdInput" onkeyup="filter('storeIdInput','storeId')">

                <g:select id="storeId" size="6" name="storeId" style="overflow-y: scroll; overflow-x: hidden;" from="${stores}" optionValue="${{it.config.storeNumber +' - ' +it.config.storeName}}"
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

        <div class="row form-group mb-4">
            <label for="enableCashManagement" class="col-6 offset-1 col-form-label text-right">Enable Cash Management</label>
            <div class="input-group col-1">
                <input type="checkbox" class="form-control form-check-input wl-checkbox" name="enableCashManagement" id="enableCashManagement" ${enableCashManagement == null || enableCashManagement ? 'checked' : ''}/>
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
        <g:if test="${till.serialNumber}">
            <button type="button" id="generatePinButton" class="btn btn-info" onclick="generatePin();">Generate PIN</button>
        </g:if>
        <g:else>
            <button type="button" id="generatePinButton" class="btn btn-info" onclick="generatePin();" disabled title="No Serial Assigned">Generate PIN</button>
        </g:else>
    </g:if>
    <button type="button" id="cancelAddTillButton" class="btn btn-wl" onclick="cancelTill();">Cancel</button>
    <button type="button" id="saveAddSupplierButton" class="btn btn-success" onclick="saveTill();">Save</button>
</div>