<div class="modal-header badge-danger" >
    <h3>Confirm order deletion</h3>
</div>

<div class="modal-body" style="word-break: break-all; word-wrap: break-word; margin-left: 15px">
    <div class="row">Do you want to delete this order? </div>
</div>


<div class="modal-footer">
    <button type="button" id="cancelDeleteOrderButton" class="btn btn-secondary" onclick="cancelOrderDelete();" >No</button>
    <button type="button" id="deleteOrderButton" class="btn btn-danger" onclick="confirmOrderDelete()" style="margin-left: 15px; float: left; top: 0; right: 0" data-dismiss="modal">Yes</button>
</div>
