<!doctype html>
<html>
<head>

    <title>Tender Lift</title>

    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="money-mask.js" />

    <script type="text/javascript">

        var successMessage = "${success}";
        var errorMessage = "${error}";

        $(document).ready(function () {
            addMoneyMaskLogic()
            processTenderLiftActionButton()
            handleResponseMessages(successMessage, errorMessage)
        });

        function addMoneyMaskLogic(){
            $('.mask-money').maskMoney({
                prefix: '',
                allowNegative: false,
                thousands: ',',
                decimal: '.',
                affixesStay: true,
                precision: 2,
            });

            $('.mask-money').on('keydown', function(e) {
                // Allow navigation keys, backspace, delete, tab, enter, and arrow keys
                if ($.inArray(e.key, ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight', 'Home', 'End']) !== -1) {
                    return;
                }

                let currentValue = $(this).val();
                currentValue = currentValue.replace(/,/g, '').replace(/[^0-9]/g, '') + e.key;

                const newValue = parseFloat(currentValue) / 100; // To handle two decimal places
                const maxValue = 999999.99;
                const minValue = 0.01;

                if (isNaN(newValue) || newValue < minValue || newValue > maxValue) {
                    e.preventDefault();
                }
            });

            // Ensure proper formatting on blur
            $('.mask-money').on('blur', function() {
                let value = $(this).val();
                value = value.replace(/,/g, ''); // Remove commas for parsing
                const parsedValue = parseFloat(value);

                if (isNaN(parsedValue) || parsedValue < 0.01) {
                    $(this).val('0.01');
                } else if (parsedValue > 999999.99) {
                    $(this).val('999999.99');
                } else {
                    $(this).val(parsedValue.toFixed(2)); // Format to 2 decimal places
                }
            });

        }

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
</head>

<body>

    <section id="segment-details" class="container-fluid">
        <div id="messages-container"></div>
    </section>

    <section class="mt-1 pt-5">
        <g:form method="post" action="processTenderLift" class="mt-1" name="processTenderLift">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-md-6 col-lg-5 offset-md-1">
                        <div class="form-group mb-5">
                            <div class="d-flex align-items-center">
                                <label for="safe" class="col-form-label mb-0 mr-2" style="width: 5rem;">Safe</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
                                    <g:select name="safeId"
                                              from="${safeLocations}"
                                              optionKey="id"
                                              optionValue="description"
                                              value="${primarySafe?.id}"
                                              class="form-control select-border"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group mb-5">
                            <div class="d-flex align-items-center">
                                <label for="tillNo" class="col-form-label mb-0 mr-2" style="width: 5rem;">Till No</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
                                    <g:select name="tillNo"
                                              from="${tills}"
                                              optionKey="tillId"
                                              optionValue="tillId"
                                              class="form-control select-border"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group mb-5">
                            <div class="d-flex align-items-center">
                                <label for="tenders" class="col-form-label mb-0 mr-2" style="width: 5rem;">Tender</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
                                    <g:select name="tender"
                                              from="${tenders}"
                                              optionValue="${{ it.toString().toLowerCase().capitalize() }}"
                                              class="form-control select-border"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group mb-5">
                            <div class="d-flex align-items-center">
                                <label for="amount" class="col-form-label mb-0 mr-2" style="width: 5rem;">Amount</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
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
                        <div class="mt-5 d-flex justify-content-end" style="max-width: 20.5rem;">  <!-- Increased margin-top -->
                            <button id="tender-lift-cancel" type="button" name="safe-save-button" onclick="handleCancelTenderLift('${createLink(action:'/home')}')" class="btn btn-wl mr-2">Cancel</button>
                            <button id="tender-lift-save" type="button" name="safe-save-button" class="btn btn-success">Save</button> <!-- Event Delegation button action added for this in function-processTenderLiftActionButton->
                        </div>
                    </div>
                </div>
            </div>
        </g:form>
    </section>
</body>
</html>