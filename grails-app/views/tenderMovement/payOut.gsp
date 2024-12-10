<!doctype html>
<html>
<head>
    <title>Tender Lift</title>

    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="money-mask.js"/>

    <script type="text/javascript">
        var successMessage = "${success}";
        var errorMessage = "${error}";

        $(document).ready(function () {
            addMoneyMaskLogic();
            processPayOutActionButton();
            handleResponseMessages(successMessage, errorMessage);
        });

        function addMoneyMaskLogic() {
            $('.mask-money').maskMoney({
                prefix: '',
                allowNegative: false,
                thousands: ',',
                decimal: '.',
                affixesStay: true,
                precision: 2,
            });

            $('.mask-money').on('keydown', function (e) {
                // Allow navigation keys, backspace, delete, tab, enter, and arrow keys
                if ($.inArray(e.key, ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight', 'Home', 'End']) !== -1) {
                    return;
                }

                let currentValue = $(this).val();
                currentValue = currentValue.replace(/,/g, '').replace(/[^0-9]/g, '') + e.key;

                const newValue = parseFloat(currentValue) / 100; // To handle two decimal places
                const maxValue = 9999.99;
                const minValue = 0.01;

                if (isNaN(newValue) || newValue < minValue || newValue > maxValue) {
                    e.preventDefault();
                }
            });

            // Ensure proper formatting on blur
            $('.mask-money').on('blur', function () {
                let value = $(this).val();
                value = value.replace(/,/g, ''); // Remove commas for parsing
                const parsedValue = parseFloat(value);

                if (isNaN(parsedValue) || parsedValue < 0.01) {
                    $(this).val('0.01');
                } else if (parsedValue > 9999.99) {
                    $(this).val('9999.99');
                } else {
                    $(this).val(parsedValue.toFixed(2)); // Format to 2 decimal places
                }
            });

        }

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

                // Call the processTenderLift function
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

    <style>
    .form-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        max-width: 800px;
        margin: 0 auto;
    }

    .form-row {
        display: flex;
        justify-content: space-between;
        width: 100%;
        margin-bottom: 20px;
    }

    .form-group {
        width: 48%;
    }

    .button-container {
        display: flex;
        justify-content: center;
        margin-top: 20px;
    }
    </style>
</head>

<body>
<section id="segment-details" class="container-fluid">
    <div id="messages-container"></div>
</section>

<section class="mt-1 pt-5">
    <g:form method="post" action="processPayOut" class="mt-1" name="processPayOut">
        <div class="form-container">
            <div class="form-row">
                <div class="form-group">
                    <label for="safeId">Safe</label>
                    <g:select name="safeId"
                              from="${safes}"
                              optionKey="id"
                              optionValue="description"
                              value="${primarySafe?.id}"
                              class="form-control select-border"/>
                </div>

                <div class="form-group">
                    <label for="tender">Tender</label>
                    <g:select name="tender"
                              from="${tenders}"
                              optionValue="${{ it.toString().toLowerCase().capitalize() }}"
                              class="form-control select-border"/>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="reasonCode">Reason code</label>
                    <g:select name="reasonCode"
                              from="${varianceReasons}"
                              optionKey="code"
                              optionValue="description"
                              class="form-control select-border"/>
                </div>

                <div class="form-group">
                    <label for="amount">Amount</label>

                    <div class="input-group">
                        <div class="input-group-prepend">
                            <span class="input-group-text">&pound;</span>
                        </div>
                        <g:textField id="amount" name="amount" value="${0.00}" min="0.01" max="99999.99"
                                     class="form-control mask-money"/>
                    </div>
                </div>
            </div>

            <div class="button-container">
                <button id="tender-lift-cancel" type="button" name="safe-cancel-button"
                        onclick="handleCancelTenderUpdate('${createLink(action:'/home')}')"
                        class="btn btn-wl mr-2">Cancel</button>
                <button id="pay-out-save" type="button" name="pay-out-save" class="btn btn-success">Save</button>
            </div>
        </div>
    </g:form>
</section>
</body>
</html>