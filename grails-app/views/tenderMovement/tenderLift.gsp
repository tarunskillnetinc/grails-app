<script type="text/javascript">

    var successMessage = "${success}";
    var errorMessage = "${error}";

    $(document).ready(function () {
        processTenderLiftActionButton();
        handleResponseMessages("${success}", "${error}");
    });

    function processTenderLiftActionButton(){
        // Remove any existing click handlers for #tender-lift-save
        $(document).off('click', '#tender-lift-save');

        // Add the click handler once
        $(document).on('click', '#tender-lift-save', function(e) {
            e.preventDefault(); // Prevent default button action if it's a submit button

            // Disable the button to prevent multiple clicks
            var $button = $(this);
            if ($button.prop('disabled')) return;
            $button.prop('disabled', true);

            // Call the processTenderLift function
            processTenderLift();

            // Re-enable the button after a short delay
            setTimeout(function() {
                $button.prop('disabled', false);
            }, 1000); // Adjust the delay as needed
        });
    }

    function handleResponseMessages(successMessage, errorMessage){
        if(successMessage != null && successMessage !== ''){
            $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + successMessage + '</div>');
        } else if (errorMessage != null && errorMessage !== '') {
            $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
        } else {
            $("#messages-container").html('');
        }
    }

</script>

<div class="centered-content">
    <div class="form-container">
        <section id="segment-details">
            <div id="messages-container"></div>
        </section>

        <section class="mt-1">
            <g:form method="post" action="processTenderLift" class="mt-1" name="processTenderLift">
                <div class="form-row">
                    <div class="form-col">
                        <div class="form-group">
                            <label for="safe" class="col-form-label">Safe</label>
                            <g:select name="safeId"
                                      from="${safeLocations}"
                                      optionKey="id"
                                      optionValue="description"
                                      value="${primarySafe?.id}"
                                      class="form-control select-border"/>
                        </div>

                        <div class="form-group">
                            <label for="tillNo" class="col-form-label">Till No</label>
                            <g:select name="tillNo"
                                      from="${tills}"
                                      optionKey="tillId"
                                      optionValue="tillId"
                                      class="form-control select-border"/>
                        </div>
                    </div>

                    <div class="form-col">
                        <div class="form-group">
                            <label for="tenders" class="col-form-label">Tender</label>
                            <g:select name="tender"
                                      from="${tenders}"
                                      optionValue="${{ it.toString().toLowerCase().capitalize() }}"
                                      class="form-control select-border"/>
                        </div>

                        <div class="form-group">
                            <label for="amount" class="col-form-label">Amount</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text">&pound;</span>
                                </div>
                                <g:textField id="amount" name="amount" value="${0.00}" min="0.01" max="999999.99" class="form-control mask-money"/>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Buttons Row -->
                <div class="buttons-container">
                    <button id="tender-lift-cancel" type="button" name="safe-save-button" onclick="handleCancelTenderUpdate('${createLink(action:'/home')}')" class="btn btn-wl mr-2">Cancel</button>
                    <button id="tender-lift-save" type="button" name="safe-save-button" class="btn btn-success">Save</button>
                </div>
            </g:form>
        </section>
    </div>
</div>
