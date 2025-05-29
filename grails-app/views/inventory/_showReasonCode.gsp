<div class="modal-header" style="display: flex; justify-content: center; align-items: center;">
    <h5>Select A Reason Code</h5>
</div>

<div class="modal-body" style="max-height: 500px; overflow-x: auto; overflow-y: auto;">

    <!-- Search Bar -->
    <div class="input-group mb-3">
        <input type="text" class="form-control" placeholder="Search for a reason code" id="reasonCodeSearchInput" onkeyup="ajaxSearchReasonCode()">
    </div>

    <!-- Initially empty container that will be populated by search -->
    <div id="supplierListView" style="overflow-y: auto;">
        <!-- Reason codes will appear here after search -->
    </div>

</div>



<script>
function ajaxSearchReasonCode() {
    var searchTerm = $('#reasonCodeSearchInput').val().trim();

    // If search term is empty, clear the results
    if (searchTerm.length === 0) {
        $('#supplierListView').html('');
        return;
    }

    // Only search if user has entered at least 1 character
    if (searchTerm.length >= 1) {
        $.ajax({
            url: "${createLink(controller: 'inventory', action: 'ajaxSearchReasonCodes')}",
            type: "GET",
            data: {
                searchTerm: searchTerm
            },
            success: function(response) {
                $('#supplierListView').html(response);
            },
            error: function(xhr) {
                $('#supplierListView').html('<div class="alert alert-danger">Error searching reason codes</div>');
            }
        });
    }
}

// Clear search results when modal is closed
function cancelReasonCodeView() {
    $('#reasonCodeSearchInput').val('');
    $('#supplierListView').html('');
}

// Also clear when modal is opened (in case it was previously used)
$('#showReasonCodeModal').on('shown.bs.modal', function () {
    $('#reasonCodeSearchInput').val('');
    $('#supplierListView').html('');
    $('#reasonCodeSearchInput').focus(); // Auto-focus the search input
});
</script>