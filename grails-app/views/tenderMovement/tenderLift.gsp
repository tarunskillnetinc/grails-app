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

            $('.mask-money').maskMoney({
                prefix: '',
                allowNegative: false,
                thousands: ',',
                decimal: '.',
                affixesStay: true,
                precision: 2,
            });

            $('.mask-money').on('keydown', function(e) {
                // Allow navigation keys, backspace, delete, tab, enter keys
                if ($.inArray(e.key, ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight']) !== -1) {
                    return;
                }

                var currentValue = $(this).val();
                currentValue = currentValue.replace(",", "").replace(".","") + e.key

                if (
                    ((this.id +'') === "rollingFloatValue" ||
                        (this.id +'') === "tillShiftVarianceLimit" ||
                        (this.id +'') === "safeVarianceLimit"
                    )
                    && parseFloat(currentValue) > 99999) {
                    e.preventDefault();
                } else if( (this.id +'') === 'tillCashHoldingLimit' && parseFloat(currentValue) > 999999) {
                    e.preventDefault();
                } else if (parseFloat(currentValue) > 150000) {
                    e.preventDefault();
                }
            });

            $(document).on('click', '#tender-lift-save', function() {
                processTenderLift();
            });

            if(successMessage != null && successMessage !== ''){
                $("#messages-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + successMessage + '</div>');
            } else if (errorMessage != null && errorMessage !== '') {
                $("#messages-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + errorMessage + '</div>');
            } else {
                $("#messages-container").html('');
            }

        });


    </script>
</head>

<body>

    <section id="segment-details" class="container-fluid">
        <div id="messages-container"></div>
    </section>

    <section class="mt-1 pt-5">
        <g:form method="post" action="processTenderLift" class="mt-5" name="processTenderLift">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-md-6 col-lg-5 offset-md-1">
                        <div class="form-group mb-5">  <!-- Increased margin-bottom -->
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

                        <div class="form-group mb-5">  <!-- Increased margin-bottom -->
                            <div class="d-flex align-items-center">
                                <label for="tillNo" class="col-form-label mb-0 mr-2" style="width: 5rem;">Till No</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
                                    <g:select name="tillNo"
                                              from="${tills}"
                                              optionKey="id"
                                              optionValue="tillId"
                                              class="form-control select-border"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group mb-5">  <!-- Increased margin-bottom -->
                            <div class="d-flex align-items-center">
                                <label for="tenders" class="col-form-label mb-0 mr-2" style="width: 5rem;">Tender</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
                                    <g:select name="tender"
                                              from="${tenders}"
                                              class="form-control select-border"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group mb-5">  <!-- Increased margin-bottom -->
                            <div class="d-flex align-items-center">
                                <label for="amount" class="col-form-label mb-0 mr-2" style="width: 5rem;">Amount</label>
                                <div class="flex-grow-1" style="max-width: 15rem;">
                                    <div class="input-group">
                                        <div class="input-group-prepend">
                                            <span class="input-group-text">&pound;</span>
                                        </div>
                                        <g:textField id="amount" name="amount" value="${0.01}" min="0.01" max="999999.99" class="form-control mask-money"/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Buttons Row -->
                        <div class="mt-5 d-flex justify-content-end" style="max-width: 20.5rem;">  <!-- Increased margin-top -->
                            <button id="tender-lift-cancel" type="button" name="safe-save-button" onclick="handleCancelAddSafe('${createLink(action:'closeSafeAdd')}')" class="btn btn-wl mr-2">Cancel</button>
                            <button id="tender-lift-save" type="button" name="safe-save-button" class="btn btn-success">Save</button>
                        </div>
                    </div>
                </div>
            </div>
        </g:form>
    </section>
</body>
</html>