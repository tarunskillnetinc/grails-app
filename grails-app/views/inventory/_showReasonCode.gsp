<div class="modal-header" style="text-align: center">
    <h2>Select A Reason Code</h2>
</div>


<div class="modal-body" style="max-height: 500px; overflow-x: auto; overflow-y: auto;">

    <!-- Search Bar -->
    <div class="input-group mb-3">
        <input type="text" class="form-control" placeholder="Search for a reason code" id="reasonCodeSearchInput" onkeyup="ajaxSearchReasonCode()">
    </div>

    <div id="supplierListView">
        <g:each in="${reasonCodes}" var="reasonCode" status="i">
            <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Select Reason Code." onclick="selectReasonCode(${reasonCode.code})">
                <div id="reasonCode-name-${i + 1}" class="col-12 text-truncate-wrap">${reasonCode.description}</div>
            </div>
        </g:each>
    </div>

</div>

<div class="modal-footer">
    <button type="button" id="cancelShowReasonCodeButton" class="btn btn-secondary" onclick="cancelReasonCodeView()" data-dismiss="modal">Cancel</button>
</div>
