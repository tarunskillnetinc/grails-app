%@ page contentType="text/html;charset=UTF-8" %>
<div class="card bg-light border-wl">
    <div class="card-header">
        Reset Inventory for Store ${storeNumber} - ${storeName}
    </div>
    <div class="card-body">
        <form id="resetInventoryForm">
            <input type="hidden" name="storeNumber" value="${storeNumber}" />

            <div class="form-group row">
                <label class="col-sm-4 col-form-label">Reset all inventory to zero?</label>
                <div class="col-sm-8">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="resetType" id="resetToZero" value="zero" checked>
                        <label class="form-check-label" for="resetToZero">
                            Yes, reset all inventory figures to zero
                        </label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="resetType" id="resetToCustom" value="custom">
                        <label class="form-check-label" for="resetToCustom">
                            No, I want to set a specific value
                        </label>
                    </div>
                </div>
            </div>

            <div class="form-group row" id="customValueGroup" style="display: none;">
                <label for="customValue" class="col-sm-4 col-form-label">Custom inventory value:</label>
                <div class="col-sm-8">
                    <input type="number" class="form-control" id="customValue" name="customValue" min="0" value="0">
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-12 text-right">
                    <button type="button" id="backButton" class="btn btn-secondary" onclick="backToStoreSearch()">Back</button>
                    <button type="button" id="resetInventoryButton" class="btn btn-danger">Reset Inventory</button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
$(document).ready(function() {
    // Show/hide custom value field based on radio selection
    $('input[name="resetType"]').change(function() {
        if ($(this).val() === 'custom') {
            $('#customValueGroup').show();
        } else {
            $('#customValueGroup').hide();
        }
    });

    // Handle reset inventory button click
    $('#resetInventoryButton').click(function() {
        if (confirm('Are you sure you want to reset inventory for this store? This action cannot be undone.')) {
            var resetType = $('input[name="resetType"]:checked').val();
            var resetValue = resetType === 'zero' ? 0 : $('#customValue').val();

            // This is where you would call an AJAX endpoint to perform the actual reset
            alert('This would reset inventory for store ' + ${storeNumber} + ' to ' + resetValue);

            // Add AJAX call here when endpoint is ready
        }
    });
});

function backToStoreSearch() {
    $('#resetInventoryContainer').hide();
    $('#results-container').show();
}
</script>