<div class="modal-header badge-success" >
    <h2>Order Confirmed</h2>
</div>

<div class="modal-body" style="max-height: 500px;word-break: break-all; word-wrap: break-word; margin-left: 15px">
    <div class="row">
        <g:if test="${orderResponse}">
            <h3>Order confirmed, please visit the Nisa website to finish placing the order: <a href="${orderResponse}" target="_blank">${orderResponse}</a></h3>
        </g:if>
        <g:else>
            Order confirmed.
        </g:else>
    </div>
</div>

<div class="modal-footer">
    <button type="button" id="cancelShowSupplierButton" class="btn btn-secondary" onclick="cancelConfirmResponse()" data-dismiss="modal">Ok</button>
</div>
