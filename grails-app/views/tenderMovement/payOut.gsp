<script type="text/javascript">
    var successMessage = "${success}";
    var errorMessage = "${error}";

    $(document).ready(function () {
        addMoneyMaskLogic();
        processPayOutActionButton();
        handleResponseMessages(successMessage, errorMessage);
    });

    function processPayOutActionButton() {
        // Remove any existing click handlers for #tender-lift-save
        $(document).off('click', '#pay-out-save');

        // Add the click handler once
        $(document).on('click', '#pay-out-save', function (e) {
            e.preventDefault(); // Prevent default button action if it's a submit button

            // Disable the button to prevent multiple clicks
            var $button = $(this);
            if ($button.prop('disabled')) return;
            $button.prop('disabled', true);
            
            processPayOut()

            // Re-enable the button after a short delay
            setTimeout(function () {
                $button.prop('disabled', false);
            }, 1000); // Adjust the delay as needed
        });
    }

    function handleResponseMessages(successMessage, errorMessage) {
        if (successMessage != null && successMessage !== '') {
            $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + successMessage + '</div>');
        } else if (errorMessage != null && errorMessage !== '') {
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
        } else {
            $("#messages-container").html('');
        }
    }

</script>

<div id="payOut" class="centered-content">
    <div class="form-container">
        <section id="segment-details" class="container-fluid px-0">
            <div id="messages-container"></div>
        </section>

        <section class="mt-1">
            <g:form method="post" action="processPayOut" class="mt-1" name="processPayOut">
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="safeId" class="col-form-label">Safe</label>
                            <g:select name="safeId"
                                      from="${safes}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${primarySafe?.id}"
                                      class="form-control select-border"/>
                        </div>
                        <div class="form-group">
                            <label for="reasonCode" class="col-form-label">Reason Code</label>
                            <g:select name="reasonCode"
                                      from="${reasonCodes}"
                                      optionKey="description"
                                      optionValue="description"
                                      class="form-control select-border"/>
                        </div>
                    </div>

                    <div class="form-col">
                        <div class="form-group">
                            <label for="tenderTypeId" class="col-form-label">Tender</label>
                            <g:select name="tenderTypeId" from="${tenders}" optionKey="id" optionValue="name" class="form-control select-border" />
                        </div>
                        <div class="form-group">
                            <label for="amount" class="col-form-label">Amount</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:textField id="amount" name="amount" value="${0.00}" min="0.01" max="9999.99" class="form-control mask-money" />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="buttons-container">
                    <button id="pay-out-cancel" type="button" name="safe-cancel-button"
                            onclick="handleCancelTenderUpdate('${createLink(action:'/home')}')"
                            class="btn btn-wl mr-2">Cancel</button>
                    <button id="pay-out-save" type="button" name="pay-out-save"
                            class="btn btn-success">Save</button>
                </div>
            </g:form>
        </section>
    </div>
</div>