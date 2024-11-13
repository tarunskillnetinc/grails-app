<div class="modal-header" style="text-align: center">
    <h2>Select Supplier</h2>
</div>


<div class="modal-body" style="max-height: 500px; overflow-x: auto; overflow-y: auto;">

    <!-- Search Bar -->
    <div class="input-group mb-3">
        <input type="text" class="form-control" placeholder="Search for a supplier" id="supplierSearchInput" onkeyup="ajaxSearchSuppliers()">
    </div>

    <div id="supplierListView">
        <g:each in="${suppliers}" var="supplier" status="i">
            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Select supplier." onclick="selectSupplier(${supplier?.id})">
                <div id="supplier-name-${i + 1}" class="col-12 text-truncate-wrap">${supplier?.name}</div>
            </div>
        </g:each>
    </div>

</div>

<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-secondary" onclick="cancelSupplierView()" data-dismiss="modal">Cancel</button>
</div>
