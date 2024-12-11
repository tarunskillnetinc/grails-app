<script type="text/javascript">

    var successMessage = "${success}";
    var errorMessage = "${error}";

    $(document).ready(function() {
        $('.date-picker').datepicker({
            format: "dd/mm/yyyy",
            weekStart: 1,
            endDate: new Date(),  // Set end date to today
            todayHighlight: true,
            autoclose: true,
            todayBtn: "linked",
            orientation: "bottom auto"
        });

        processBankReceiptActionButton();
        handleResponseMessages(successMessage, errorMessage);
    });


    function processBankReceiptActionButton(){
        // Remove any existing click handlers for #tender-lift-save
        $(document).off('click', '#bank-Receipt-save');

        // Add the click handler once
        $(document).on('click', '#bank-Receipt-save', function(e) {
            e.preventDefault(); // Prevent default button action if it's a submit button

            // Disable the button to prevent multiple clicks
            var $button = $(this);
            if ($button.prop('disabled')) return;
            $button.prop('disabled', true);

            // Call the processTenderLift function
            processBankReceipt();

            // Re-enable the button after a short delay
            setTimeout(function() {
                $button.prop('disabled', false);
            }, 1000); // Adjust the delay as needed
        });
    }

    function handleResponseMessages(successMessage, errorMessage) {
        $("#messages-container").empty();

        if (successMessage && successMessage.trim() !== '') {
            var decodedSuccessMessage = $("<textarea/>").html(successMessage).text(); // Decode escaped HTML
            var successHtml = $('<div class="alert alert-success alert-wl mx-0" role="alert"></div>');
            successHtml.html(decodedSuccessMessage); // Render decoded HTML
            $("#messages-container").append(successHtml);
        }

        if (errorMessage && errorMessage.trim() !== '') {
            var decodedErrorMessage = $("<textarea/>").html(errorMessage).text(); // Decode escaped HTML
            var errorHtml = $('<div class="alert alert-danger alert-wl mx-0" role="alert"></div>');
            errorHtml.html(decodedErrorMessage); // Render decoded HTML
            $("#messages-container").append(errorHtml);
        }
    }

</script>
<div id="bankReceipt" class="centered-content">
    <div class="form-container">
        <section id="bank-receipt-details">
            <div id="messages-container"></div>
        </section>
        <section class="mt-1">
            <g:form method="post" action="processBankReceipt" class="mt-1" name="processBankReceipt">
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="safe" class="col-form-label">Safe</label>
                            <g:select name="safeId" from="${safes}" optionKey="id" optionValue="description"
                                      value="${selectedSafe?.id}" class="form-control select-border"/>
                        </div>
                        <div class="form-group">
                            <label for="bankingDate" class="col-form-label">Date</label>
                            <g:textField name="bankingDate" value="${currentDate}" class="form-control date-picker"/>
                        </div>
                    </div>
                    <div class="form-col">
                        <div class="form-group">
                            <label for="tender" class="col-form-label">Tender</label>
                            <g:select name="tempTenderField" from="${tenders}" disabled="disabled"
                                    optionValue="${{ it.toString().toLowerCase().capitalize() }}" class="form-control select-border"/>
                            <g:hiddenField name="tender" value="${tenders.get(0).toString()}"/>
                        </div>
                        <div class="form-group">
                            <label for="bank" class="col-form-label">Bank</label>
                            <g:textField name="bank" value="${bank?.name}" class="form-control"/>
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="bagReferenceNumber" class="col-form-label">Bag Reference Number</label>
                            <g:textField name="bagReferenceNumber" value="${bagReferenceNumber}" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-col">
                        <div class="form-group">
                            <label for="amount" class="col-form-label">Amount</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:textField id="amount" name="amount" value="${amount}" min="0.01" max="999999.99" class="form-control mask-money"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="comments" class="col-form-label">Comments</label>
                            <textarea name="comments" class="form-control">${comments}</textarea>
                        </div>
                    </div>
                </div>
                <div class="buttons-container">
                    <button id="bank-Receipt-cancel" type="button" name="cancel-button" onclick="handleCancel('${createLink(action:'/home')}')" class="btn btn-wl mr-2">Cancel</button>
                    <button id="bank-Receipt-save" type="submit" name="save-button" class="btn btn-success">Save</button>
                </div>
            </g:form>
        </section>
    </div>
</div>