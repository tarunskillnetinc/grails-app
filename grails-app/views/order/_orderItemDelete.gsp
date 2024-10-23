<div class="modal-header badge-danger" >
    <h3>Delete order item</h3>
</div>

<div class="modal-body" style="word-break: break-all; word-wrap: break-word; margin-left: 15px">
    <div class="row">Do you want to delete this item from the order?</div>
</div>


<div class="modal-footer">
    <button type="button" id="saveSupplierButton" class="btn btn-secondary" data-dismiss="modal" style="margin-left: 15px; float: left;">Cancel</button>
    <button type="button" id="cancelShowSupplierButton" class="btn btn-danger" onclick="confirmOrderItemDelete(${productItemId})" data-dismiss="modal">Delete Item</button>
</div>