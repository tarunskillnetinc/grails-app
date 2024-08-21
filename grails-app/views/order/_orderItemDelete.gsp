<div class="modal-header badge-danger" >
    <h3>Order item delete error</h3>
</div>

<div class="modal-body" style="word-break: break-all; word-wrap: break-word; margin-left: 15px">
    <div class="row">Do you want to delete this item? </div>
</div>


<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-danger" onclick="confirmOrderItemDelete(${productItemId})" data-dismiss="modal">Delete</button>
    <button type="button" id="saveSupplierButton" class="btn btn-secondary" onclick="cancelOrderItemDelete();" style="margin-left: 15px; float: left;">Ok</button>
</div>