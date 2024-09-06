<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <asset:stylesheet src="multi-select-checks.css" />

    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="popper.min.js" />
    <asset:javascript src="multi-select-checks.js" />
    <asset:javascript src="money-mask.js" />

    <script type='text/javascript'>

        $(document).ready(function () {
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

            const weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
            createMultiSelectorChecks('automaticCloseDaysSelector', 'automaticCloseDays', weekdays, "Select Days");
            createMultiSelectorChecks('tillAutoSnapshotDaysSelector', 'tillAutoSnapshotDays', weekdays, "Select Days");
            createMultiSelectorChecks('safeAutoSnapshotDaysSelector', 'safeAutoSnapshotDays', weekdays, "Select Days");

            validateTimeInputs('automaticCloseTime');
            validateTimeInputs('tillAutoSnapshotTime');
            validateTimeInputs('safeAutoSnapshotTime');


            $('.mask-money').maskMoney({
                prefix: '',
                allowNegative: false,
                thousands: ',',
                decimal: '.',
                affixesStay: true,
                precision: 2
            });

            $('.mask-money').on('keydown', function(e) {
                // Allow navigation keys, backspace, delete, tab, enter keys
                if ($.inArray(e.key, ['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight']) !== -1) {
                    return;
                }

                var currentValue = $(this).val();
                currentValue = currentValue.replace(",", "").replace(".","") + e.key

                if (parseFloat(currentValue) > 150000) {
                    e.preventDefault();
                }
            });

        });

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


    </script>
    <style>
        h5 {
            margin-left: -220px;
        }
    </style>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Cash Management</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>
    <g:render template="cashManagementTemp" model='[config:config]'/>
</body>
</html>