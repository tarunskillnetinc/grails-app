<asset:stylesheet src="multi-select-checks.css" />
<asset:javascript src="money-mask.js"/>
<asset:javascript src="multi-select-checks.js" />

<style>
    .dropdown-menu {
        display: none;
    }
    .dropdown-menu.show {
        display: block;
    }
    .dropdown {
        position: relative;
    }
    .dropdown-menu {
        position: absolute;
        top: 100%;
        left: 0;
        z-index: 1000;
        float: left;
        min-width: 10rem;
        padding: .5rem 0;
        margin: .125rem 0 0;
        font-size: 1rem;
        color: #212529;
        text-align: left;
        list-style: none;
        background-color: #fff;
        background-clip: padding-box;
        border: 1px solid rgba(0,0,0,.15);
        border-radius: .25rem;
    }
    .caret {
        display: inline-block;
        width: 0;
        height: 0;
        margin-left: 0.255em;
        vertical-align: 0.255em;
        content: "";
        border-top: 0.3em solid;
        border-right: 0.3em solid transparent;
        border-bottom: 0;
        border-left: 0.3em solid transparent;
    }

    .dropdown.show .caret {
        transform: rotate(180deg);
    }
</style>

<script type="text/javascript">

    var successMessage = "${success}";
    var errorMessage = "${error}";

    $(document).ready(function () {
        addMoneyMaskLogic();
        processIssueFloatActionButton();
        handleResponseMessages("${success}", "${error}");
        initializeMultiSelect();

    });

    function processIssueFloatActionButton(){
        // Remove any existing click handlers for #tender-lift-save
        $(document).off('click', '#issue-float-save');

        // Add the click handler once
        $(document).on('click', '#issue-float-save', function(e) {
            e.preventDefault(); // Prevent default button action if it's a submit button

            // Disable the button to prevent multiple clicks
            var $button = $(this);
            if ($button.prop('disabled')) return;
            $button.prop('disabled', true);

            // Call the processTenderLift function
            processIssueFloat();

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
            const maxValue = 9999.99;
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
                $(this).val('0').focus();
            } else if (parsedValue > 9999.99) {
                $(this).val('9999.99');
            } else {
                $(this).val(parsedValue.toFixed(2)); // Format to 2 decimal places
            }
        });

    }

    function initializeMultiSelect() {
        // Attach event listener for dynamically generated checkboxes
        $(document).on('change', 'input[name="tillNos"]', function() {
            updateSelectedTills();
        });

        // Update selected tills on page load
        updateSelectedTills();

        $('#tillNo').on('click', function () {
            $(this).parent().toggleClass('show');
            $(this).next('.dropdown-menu').toggleClass('show');
        });

        $(document).on('click', function (e) {
            if (!$(e.target).closest('.dropdown').length) {
                $('.dropdown-menu').removeClass('show');
                $('.dropdown').removeClass('show');
            }
        });

        function updateSelectedTills() {
            const selected = $('input[name="tillNos"]:checked').map(function () {
                return $(this).val();
            }).get();

            if (selected.length > 0) {
                $('#selectedTills').text(selected.join(', '));
            } else {
                $('#selectedTills').text('Select Till Numbers');
            }
        }
    }

</script>



<section id="segment-details" class="container-fluid">
    <div id="messages-container"></div>
</section>

<section class="mt-1 pt-5">
    <g:form method="post" action="processIssueFloat" class="mt-1" name="processIssueFloat">
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
                                <div class="dropdown">
                                    <div class="form-control select-border d-flex justify-content-between align-items-center" id="tillNo">
                                        <span id="selectedTills">Select Till Numbers</span>
                                        <span class="caret"></span>
                                    </div>
                                    <div class="dropdown-menu w-100">
                                        <g:each in="${tills}" var="till">
                                            <div class="dropdown-item">
                                                <label class="mb-0 w-100">
                                                    <input type="checkbox" name="tillNos" value="${till.tillId}"> ${till.tillId}
                                                </label>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
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
                    <div class="mt-5 d-flex justify-content-end" style="max-width: 20.5rem;">
                        <button id="issue-float-cancel" type="button" name="issue-float-cancel-button" onclick="handleCancelTenderLift('${createLink(action:'/home')}')" class="btn btn-wl mr-2">Cancel</button>
                        <button id="issue-float-save" type="button" name="issue-float-save-button" class="btn btn-success">Save</button>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</section>
