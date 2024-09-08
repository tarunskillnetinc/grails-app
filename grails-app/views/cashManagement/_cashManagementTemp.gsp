<asset:stylesheet src="multi-select-checks.css" />

<asset:javascript src="validators/input-validator.js" />
<asset:javascript src="popper.min.js" />
<asset:javascript src="multi-select-checks.js" />
<asset:javascript src="money-mask.js" />

<script type='text/javascript'>

    function initializePage() {

        $('#selectedItemsDisplay').click(function() {
            $('#weekdayDropdown').toggleClass('show');
        });

        $('.weekday-checkbox').change(function() {
            updateSelectedItems();
        });

        function updateSelectedItems() {
            var selectedItems = [];
            $('.weekday-checkbox:checked').each(function() {
                selectedItems.push($(this).val());
            });
            $('#selectedItemsDisplay > span:first-child').html(selectedItems.join(', ') || 'Select Weekdays');
        }

        $(document).click(function(event) {
            if(!$(event.target).closest('.multiselect-container').length) {
                if($('#weekdayDropdown').hasClass('show')) {
                    $('#weekdayDropdown').removeClass('show');
                }
            }
        });

        let isDisabledInputs = false;
        if ($('#manualOpen').prop('disabled')) {
            isDisabledInputs = true;
        }
        const onlyRetailerLevel = ${onlyRetailerLevel};
        const isStoreLevelLogin = ${isStoreLevelLogin?isStoreLevelLogin:false};

        const weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        createMultiSelectorChecks('automaticCloseDaysSelector', 'automaticCloseDays', weekdays, "Select Days", isDisabledInputs);
        createMultiSelectorChecks('tillAutoSnapshotDaysSelector', 'tillAutoSnapshotDays', weekdays, "Select Days", isDisabledInputs);
        createMultiSelectorChecks('safeAutoSnapshotDaysSelector', 'safeAutoSnapshotDays', weekdays, "Select Days", isDisabledInputs);

        validateTimeInputs('automaticCloseTime');
        validateTimeInputs('tillAutoSnapshotTime');
        validateTimeInputs('safeAutoSnapshotTime');

        boundTimeInputToDaysSelection('automaticCloseDays', 'automaticCloseTime');
        boundTimeInputToDaysSelection('tillAutoSnapshotDays', 'tillAutoSnapshotTime');
        boundTimeInputToDaysSelection('safeAutoSnapshotDays', 'safeAutoSnapshotTime');

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
                && parseFloat(currentValue) > 99900) {
                e.preventDefault();
            } else if( (this.id +'') === 'tillCashHoldingLimit' && parseFloat(currentValue) > 999900) {
                e.preventDefault();
            } else if (parseFloat(currentValue) > 150000) {
                e.preventDefault();
            }
        });
        $('#save-form').on('submit', function(e) {
            e.preventDefault(); // Prevent default form submission

            if (isDisabledInputs) {
                // Get the form element
                var $form = $(this);

                // Temporarily enable all disabled fields
                $form.find(':disabled').each(function() {
                    $(this).data('disabled', true); // Store that this field was disabled
                    $(this).prop('disabled', false); // Enable it
                });
            }

            const formData = new FormData(this); // Create a FormData object for file upload

            $.ajax({
                url: $(this).attr('action'), // Get the action URL from the form's action attribute
                type: 'POST',
                data: formData,
                processData: false,  // Prevent jQuery from converting the data into a query string
                contentType: false,  // Required for file uploads
                success: function(response) {
                    if (onlyRetailerLevel || isStoreLevelLogin) {
                        setTimeout(function() {
                            initializePage(); // Manually trigger the initialization after a short delay
                        }, 0);//0 is not a problem to initiate all page initiation
                        $('html').html(response);
                    } else {
                        $('#cash-container').html(response);
                    }
                },
                error: function(xhr, status, error) {
                    // Handle error
                    alert('An error occurred: ' + error);
                }
            });
        });

        $('input, select, textarea').on('input change', function() {
            $('#errors-container1').remove();
            $('#errors-container2').remove();
        });
    }

    function validateTimeInputs(inputId) {
        $('#' + inputId).on('keydown', function(event) {
            const key = event.key;
            const value = $(this).val();
            const position = this.selectionStart;

            // Allow control keys: backspace, delete, arrow keys, etc.
            const controlKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Delete', 'Tab'];
            if (controlKeys.includes(key)) {
                return true;
            }

            // Prevent entering non-numeric or multiple colons
            if (!/\d/.test(key) && key !== ':') {
                event.preventDefault();
                return;
            }

            // Prevent entering more than one colon
            if (key === ':' && value.includes(':')) {
                event.preventDefault();
                return;
            }

            // Automatically insert the colon if the user tries to type it manually
            if (key === ':' && (position === 0)) {
                $(this).val('00:');
                event.preventDefault();
            } else if(key === ':' && (position === 1)) {
                $(this).val('0'+value.slice(0,1)+':');
                event.preventDefault();
            } else if (key === ':' && position === 2) {
                $(this).val(value.slice(0, 2) + ':' + value.slice(2));
                event.preventDefault();
            }

            // Prevent entering invalid numbers based on position
            if (/\d/.test(key)) {
                if (position === 0 && !/[0-9]/.test(key)) {
                    event.preventDefault();
                } if(position === 1 && parseInt(value[0]) > 2) {
                    event.preventDefault();
                } else if (position === 1 && value[0] === '2' && !/[0-3]/.test(key)) {
                    event.preventDefault();
                } else if (position === 3 && !/[0-5]/.test(key)) {
                    event.preventDefault();
                }
            }
        });

        $('#' + inputId).on('input', function(event) {
            let value = $(this).val();
            let formattedValue = value.replace(/[^0-9:]/g, ''); // Allow only digits and colon

            // Automatically add colon if typed at first or second position
            if (formattedValue.length === 1 && formattedValue[0] === ':') {
                formattedValue = '00:';
            } else if (formattedValue.length === 2 && formattedValue[1] === ':') {
                formattedValue = '0' + formattedValue[0] + ':';
            }

            // Automatically insert colon at the correct position
            if (formattedValue.length > 2 && formattedValue[2] !== ':') {
                formattedValue = formattedValue.slice(0, 2) + ':' + formattedValue.slice(2, 4);
            }

            // Limit length to 5 characters (HH:mm)
            if (formattedValue.length > 5) {
                formattedValue = formattedValue.slice(0, 5);
            }
            $(this).val(formattedValue);

        });

    }

    function boundTimeInputToDaysSelection(daysSelectionId, timeSelectionId) {
        if ($('#' + daysSelectionId).val() === "") {
            $('#'+timeSelectionId).prop('disabled', true);
            $('#'+timeSelectionId).data('cachedValue', $('#'+timeSelectionId).val())
        }
        $('#' + daysSelectionId).on('change', function(){
            if ($('#' + daysSelectionId).val() !== "") {
                $('#'+timeSelectionId).prop('disabled', false);
                $('#'+timeSelectionId).val($('#'+timeSelectionId).data('cachedValue'));
            } else {
                $('#'+timeSelectionId).prop('disabled', true);
                $('#'+timeSelectionId).data('cachedValue', $('#'+timeSelectionId).val());
                $('#'+timeSelectionId).val('');
            }
        });
        $('#'+timeSelectionId).on('change', function (){
            $(this).data('cachedValue', $(this).val());
        });
    }

</script>

<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto my-auto">Cash Management</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel-btn" controller="cashManagement" action="index" tabindex="-1" role="button" class="btn btn-wl">Cancel</g:link>
            <button id="save-btn" class="btn btn-success" name="save" onclick="$('#save-form').submit();">Save</button>
        </div>
    </div>
</section>

<g:if test="${flash.error}">
    <section id="errors-container2" class="container-fluid">
        <div class="alert alert-danger alert-wl mx-0" role="alert">
            <ul>
                <g:each in="${flash.error}" var="error" status="i">
                    <li>${error}</li>
                </g:each>
            </ul>
        </div>
    </section>
</g:if>

<g:if test="${flash.message}">
    <section id="errors-container2" class="container-fluid">
        <div class="alert alert-success alert-wl mx-0" role="alert">
            <g:each in="${flash.message}" var="message" status="i">
                ${message}<br/>
            </g:each>
        </div>
    </section>
</g:if>


<section id="addProduct-section" class="container-fluid mt-4">
    <g:uploadForm id="save-form" name="save-form" action="save" method="POST" enctype="multipart/form-data">
        <input type="hidden" name="modelOnlyRetailerLevel" value="${onlyRetailerLevel}">
        <input type="hidden" name="modelStoreLevelExist" value="${storeLevelExist}">
        <input type="hidden" name="storeId" value="${storeId}">
        <input type="hidden" name="modelIsStoreLevelLogin" value="${isStoreLevelLogin}">
        <div id="accordion">
            <!-- General information. -->
            <div class="card bg-light border-wl accordion-card col-12 col-lg-8 offset-lg-2 px-0"> <!-- Center the card -->
                <div class="card-header pointer" id="generalDetails" data-toggle="collapse" data-target="#collapseGeneralDetails" aria-expanded="true" aria-controls="collapseGeneralDetails">
                    <div class="row">
                        <div class="col-10 font-weight-bold">Cash Management</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div id="collapseGeneralDetails" class="collapse show" aria-labelledby="generalDetails" data-parent="#accordion">
                    <div class="card-body py-5">
                        <div class="col-12">

                            <h5 class="text-center">Till Shifts</h5>

                            <!-- Open Type Field -->
                            <div class="form-group row">
                                <label for="isManualOpen" class="col-12 col-lg-4 text-right align-self-center">Open Type</label>
                                <div class="col-12 col-lg-6">
                                    <div class="form-check form-check-inline">
                                        <input type="radio" class="form-check-input" name="manualOrAutoOpen" id="manualOpen" value="manual" ${config?.tillShiftsManualOpen ? 'checked' : ''} ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                        <label class="form-check-label" for="manualOpen">Manual</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input type="radio" class="form-check-input" name="manualOrAutoOpen" id="autoOpen" value="auto" ${!config?.tillShiftsManualOpen ? 'checked' : ''} ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                        <label class="form-check-label" for="autoOpen">Auto</label>
                                    </div>
                                </div>
                            </div>

                            <!-- Close Type Field -->
                            <div class="form-group row">
                                <label for="isManualClose" class="col-12 col-lg-4 text-right align-self-center">Close Type</label>
                                <div class="col-12 col-lg-6">
                                    <div class="form-check form-check-inline">
                                        <input type="radio" class="form-check-input" name="manualOrAutoClose" id="manualClose" value="manual" ${config ? config?.tillShiftsManualClose ? 'checked' : '':'checked'} ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                        <label class="form-check-label" for="manualClose">Manual</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input type="radio" class="form-check-input" name="manualOrAutoClose" id="autoClose" value="auto" ${config ? !config?.tillShiftsManualClose ? 'checked' : '' : ''} ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                        <label class="form-check-label" for="autoClose">Auto</label>
                                    </div>
                                </div>
                            </div>

                            <!-- Automatic Close Days Field -->
                            <div class="form-group row">
                                <label for="automaticCloseDays" class="col-12 col-lg-4 text-right align-self-center">Automatic Close Days</label>
                                <div class="col-12 col-lg-6">
                                    <input type="hidden" id="automaticCloseDays" name="automaticCloseDays" value="${config?.automaticCloseDaysFormatted ? config?.automaticCloseDaysFormatted : ''}" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                    <div id="automaticCloseDaysSelector"></div>
                                </div>
                            </div>

                            <!-- Automatic Close Time Field -->
                            <div class="form-group row">
                                <label for="automaticCloseTime" class="col-12 col-lg-4 text-right align-self-center">Automatic Close Time</label>
                                <div class="col-12 col-lg-6">
                                    <input type="text" class="col-5 form-control bottom-border" name="automaticCloseTime" id="automaticCloseTime" value="${config?.tillShiftsAutoCloseTime}" placeholder="HH:mm" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Rolling Float</h5>

                            <!-- Rolling Float Enable Field -->
                            <div class="form-group row">
                                <label for="isRollingFloatEnable" class="col-12 col-lg-4 text-right align-self-center">Rolling Float Enable</label>
                                <div class="col-12 col-lg-6">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox ml-0" name="isRollingFloatEnable" id="isRollingFloatEnable" ${config?.rollingFloatEnabled ? 'checked' : ''} ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <!-- Rolling Float Value Field -->
                            <div class="form-group row">
                                <label for="rollingFloatValue" class="col-12 col-lg-4 text-right align-self-center">Rolling Float Value</label>
                                <div class="col-12 col-lg-6 pl-0">
                                    <g:render template="priceView" model='[inputId:"rollingFloatValue", inputName:"rollingFloatValue", fieldValue:config?.rollingFloatValue, onlyRetailerLevel:onlyRetailerLevel,storeLevelExist:storeLevelExist]'/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Reconciliation</h5>

                            <!-- Till Shift Recount Limit Field -->
                            <div class="form-group row">
                                <label for="tillShiftRecountLimit" class="col-12 col-lg-4 text-right align-self-center">Till Shift Recount Limit</label>
                                <div class="col-12 col-lg-6">
                                    <input type="number" class="col-5 form-control bottom-border" name="tillShiftRecountLimit" id="tillShiftRecountLimit"
                                           value="${config?config.tillShiftRecountLimit? config.tillShiftRecountLimit:'' : 3}" oninput="validateInput(this);"
                                           onkeydown="acceptMinMaxNumberValue(event, 0, 99);" min="0" max="99" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <!-- Till Shift Variance Limit Field -->
                            <div class="form-group row">
                                <label for="tillShiftVarianceLimit" class="col-12 col-lg-4 text-right align-self-center">Till Shift Variance Limit</label>
                                <div class="col-12 col-lg-6 pl-0">
                                    <g:render template="priceView" model='[inputId:"tillShiftVarianceLimit", inputName:"tillShiftVarianceLimit", fieldValue:config?config.tillShiftVarianceLimit?config.tillShiftVarianceLimit:0:500]'/>
                                </div>
                            </div>

                            <!-- Safe Recount Limit Field -->
                            <div class="form-group row">
                                <label for="safeRecountLimit" class="col-12 col-lg-4 text-right align-self-center">Safe Recount Limit</label>
                                <div class="col-12 col-lg-6">
                                    <input type="number" class="col-5 form-control bottom-border" name="safeRecountLimit" id="safeRecountLimit"
                                           value="${config?config.tillShiftRecountLimit?config.tillShiftRecountLimit:'':3}" onkeydown="acceptMinMaxNumberValue(event, 0, 99);" min="0" max="99" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <!-- Safe Variance Limit Field -->
                            <div class="form-group row">
                                <label for="safeVarianceLimit" class="col-12 col-lg-4 text-right align-self-center">Safe Variance Limit</label>
                                <div class="col-12 col-lg-6 pl-0">
                                    <g:render template="priceView" model='[inputId:"safeVarianceLimit", inputName:"safeVarianceLimit", fieldValue:config?config.safeVarianceLimit?config.safeVarianceLimit:0:500]'/>
                                </div>
                            </div>

                            <!-- Open Shift Without Float Field -->
                            <div class="form-group row">
                                <label for="isOpenShiftWithoutFloat" class="col-12 col-lg-4 text-right align-self-center">Open Shift Without Float</label>
                                <div class="col-12 col-lg-6">
                                    <input type="checkbox" class="col-1 form-check-input wl-checkbox ml-0" name="isOpenShiftWithoutFloat" id="isOpenShiftWithoutFloat" ${config?config.openShiftWithoutFloat ? 'checked' : '':'checked'} ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Snapshots</h5>

                            <!-- Till Auto Snapshot Days Field -->
                            <div class="form-group row">
                                <label for="tillAutoSnapshotDays" class="col-12 col-lg-4 text-right align-self-center">Till Auto Snapshot Days</label>
                                <div class="col-12 col-lg-6">
                                    <input type="hidden" id="tillAutoSnapshotDays" name="tillAutoSnapshotDays" value="${config?config.tillAutoSnapshotDaysFormatted?config.tillAutoSnapshotDaysFormatted:'':'1234567'}" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                    <div id="tillAutoSnapshotDaysSelector"></div>
                                </div>
                            </div>

                            <!-- Till Auto Snapshot Time Field -->
                            <div class="form-group row">
                                <label for="tillAutoSnapshotTime" class="col-12 col-lg-4 text-right align-self-center">Till Auto Snapshot Time</label>
                                <div class="col-12 col-lg-6">
                                    <input type="text" class="col-5 form-control bottom-border" name="tillAutoSnapshotTime" id="tillAutoSnapshotTime" value="${config?config.tillAutoSnapshotTime?config.tillAutoSnapshotTime:'':'10:00'}" placeholder="HH:mm" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <!-- Safe Auto Snapshot Days Field -->
                            <div class="form-group row">
                                <label for="safeAutoSnapshotDays" class="col-12 col-lg-4 text-right align-self-center">Safe Auto Snapshot Days</label>
                                <div class="col-12 col-lg-6">
                                    <input type="hidden" id="safeAutoSnapshotDays" name="safeAutoSnapshotDays" value="${config?config.safeAutoSnapshotDaysFormatted?config.safeAutoSnapshotDaysFormatted:'':'1234567'}" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                    <div id="safeAutoSnapshotDaysSelector"></div>
                                </div>
                            </div>

                            <!-- Safe Auto Snapshot Time Field -->
                            <div class="form-group row">
                                <label for="safeAutoSnapshotTime" class="col-12 col-lg-4 text-right align-self-center">Safe Auto Snapshot Time</label>
                                <div class="col-12 col-lg-6">
                                    <input type="text" class="col-5 form-control bottom-border" name="safeAutoSnapshotTime" id="safeAutoSnapshotTime" value="${config?config.safeAutoSnapshotTime?config.safeAutoSnapshotTime:'':'10:00'}" placeholder="HH:mm" ${(!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist? "disabled" : ""}/>
                                </div>
                            </div>

                            <h5 class="text-center mt-5">Holding Limit</h5>

                            <!-- Till Cash Holding Limit Field -->
                            <div class="form-group row">
                                <label for="tillCashHoldingLimit" class="col-12 col-lg-4 text-right align-self-center">Till Cash Holding Limit</label>
                                <div class="col-12 col-lg-6 pl-0">
                                    <g:render template="priceView" model='[inputId:"tillCashHoldingLimit", inputName:"tillCashHoldingLimit", fieldValue:config?config.tillsCashHoldingLimit?config.tillsCashHoldingLimit:0:150000]'/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:uploadForm>
</section>
<script>
    initializePage();
</script>