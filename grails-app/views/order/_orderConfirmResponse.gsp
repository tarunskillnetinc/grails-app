<div class="modal-header" >
    <h2>Confirm Order Success</h2>
</div>

<g:if test="${orderResponse}">
    <div class="modal-body" style="max-height: 500px;word-break: break-all; word-wrap: break-word; margin-left: 15px">
            <div class="row"><h3><a href="${orderResponse}" target="_blank">Click Here To Find More About Order</a></h3></div>
    </div>
</g:if>

<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-secondary" onclick="cancelConfirmResponse()" data-dismiss="modal">Cancel</button>
</div>
