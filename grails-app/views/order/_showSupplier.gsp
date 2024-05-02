<div class="modal-header" style="text-align: center">
    <h2>Select Supplier</h2>
</div>


<div class="modal-body" style="max-height: 500px; overflow-x: auto; overflow-y: auto; overflow-wrap: break-word;">

    <!-- Search Bar -->
    <div class="input-group mb-3">
        <input type="text" class="form-control" placeholder="Search for a supplier" id="supplierSearchInput" onkeyup="ajaxSearchSuppliers()">
    </div>

    <div id="supplierListView">
        <g:render template="supplierListView" model="[suppliers: suppliers]" />
    </div>

</div>

<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-secondary" onclick="cancelSupplierView()" data-dismiss="modal">Cancel</button>
</div>
