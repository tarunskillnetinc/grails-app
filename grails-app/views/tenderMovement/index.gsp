<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Tender Movement</title>

    <asset:stylesheet src="tenderMovements.css" />
    <asset:stylesheet src="bootstrap-datepicker3.min.css" />

    <asset:javascript src="money-mask.js"/>
    <asset:javascript src="tenderMovementUrls.js"/>
    <asset:javascript src="tenderMovement.js"/>
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <script type="text/javascript">

        $(document).ready(function () {

            TenderMovementUrls.init(
                "${createLink(controller: 'TenderMovement', action: 'processTenderLift')}",
                "${createLink(controller: 'TenderMovement', action: 'getTillAvailableBalance')}"
            );

            // Select the first tab by default if none are active
            if (!$('.nav-link.active').length) {
                $('#issue-float-tab').addClass('active');
                $('#issue-float-container').addClass('active');
            }

            // Initial load of the first tab's content
            var firstTab = $('a[data-toggle="tab"]').first();
            var initialAction = getControllerLinkForTabId(firstTab.attr('id'));
            var initialTabName = $('a[data-toggle="tab"].active').data('tab-name');
            updateBreadcrumb(initialTabName);
            initialiseContentTab(initialAction)


            $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                var selectedTab = $(e.target).attr('id');  // Get the ID of the selected tab
                var action = getControllerLinkForTabId(selectedTab);  // Get the action based on the tab ID
                var tabName = $(e.target).data('tab-name');

                // Update the breadcrumb
                updateBreadcrumb(tabName);

                // Make AJAX call to load content for the selected tab
                initialiseContentTab(action);

            });


            // Function to determine the action based on the tab ID
            function getControllerLinkForTabId(tabId) {
                switch (tabId) {
                    case 'issue-float-tab':
                        return "${createLink(controller: 'tenderMovement' , action: 'issueFloat')}";
                    case 'tender-lift-tab':
                        return "${createLink(controller: 'tenderMovement' , action: 'tenderLift')}";
                    case 'pay-in-tab':
                        return "${createLink(controller: 'tenderMovement' , action: 'payIn')}";
                    case 'pay-out-tab':
                        return "${createLink(controller: 'tenderMovement' , action: 'payOut')}";
                    case 'bank-deposit-tab':
                        return "${createLink(controller: 'tenderMovement' , action: 'bankDeposit')}";
                    case 'bank-receipt-tab':
                        return "${createLink(controller: 'tenderMovement' , action: 'bankReceipt')}";
                    default:
                        return 'index';  // Default action if tab ID is not recognized
                }
            }
        });


        function updateBreadcrumb(tabName) {
            $('#current-page-name').text(tabName);
        }

        function initialiseContentTab(action){
            $.get(action, function (data) {
                $('#tender-movement-container').html(data);

                //Re initiate money mask function after tab load
                addMoneyMaskLogic();
            });
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
                    $(this).val('0.00');
                } else if (parsedValue > 9999.99) {
                    $(this).val('9999.99');
                } else {
                    $(this).val(parsedValue.toFixed(2)); // Format to 2 decimal places
                }
            });
        }

    </script>

    <style>

    #tab-ul {
        border-bottom-color: white !important;
        display: flex;
        flex-wrap: nowrap;
        overflow-x: auto;
        width: 100%;
    }

    #tab-ul.tabs-wl li {
        flex: 1;
        display: flex;
    }

    #tab-ul.tabs-wl li a {
        text-align: center;
        white-space: nowrap;
        padding: 0.5rem 1rem;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    #tab-ul.tabs-wl li a.active {
        border-style: solid;
        border-width: 0.125rem 0.125rem 0 0.125rem;
        border-color: #575756 !important;
        border-bottom-color: white !important;
        color: #575756 !important;
        background-color: white !important;
    }

    @media (max-width: 48rem) {
        /* 768px / 16px = 48rem */
        #tab-ul.tabs-wl li {
            flex: 0 0 auto;
        }

        #tab-ul.tabs-wl li a {
            min-width: 7.5rem; /* 120px / 16px = 7.5rem */
            font-size: 0.9rem;
        }
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><span id="current-page-name"></span></li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>
    <section id="tab-container" class="container-fluid m-0">
        <div class="row m-0">
            <div class="col-12 pl-0">
                <ul id="tab-ul" class="nav nav-tabs tabs-wl d-flex m-0 flex-nowrap overflow-auto" role="tablist">
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="issue-float-tab" data-toggle="tab" href="#issue-float-container" data-action="issueFloat"
                           aria-selected="true" role="tab" aria-controls="issue-float-container" class="nav-link active" data-tab-name="Issue Float">Issue Float</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="tender-lift-tab" data-toggle="tab" href="#tender-lift-container" data-action="tenderLift"
                           role="tab" aria-controls="tender-lift-container" class="nav-link" data-tab-name="Tender Lift">Tender Lift</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="pay-in-tab" data-toggle="tab" href="#pay-in-container" data-action="payIn" role="tab"
                           aria-controls="pay-in-container" class="nav-link" data-tab-name="Pay In">Pay In</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="pay-out-tab" data-toggle="tab" href="#pay-out-container" data-action="payOut" role="tab"
                           aria-controls="pay-out-container" class="nav-link" data-tab-name="Pay Out">Pay Out</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="bank-deposit-tab" data-toggle="tab" href="#bank-deposit-container" data-action="bankDeposit"
                           role="tab" aria-controls="bank-deposit-container" class="nav-link" data-tab-name="Bank Deposit">Bank Deposit</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="bank-receipt-tab" data-toggle="tab" href="#bank-receipt-container" data-action="bankReceipt"
                           role="tab" aria-controls="bank-receipt-container" class="nav-link" data-tab-name="Bank Receipt">Bank Receipt</a>
                    </li>
                </ul>
            </div>
        </div>

        <div class="tab-content mt-3">
            <div id="tender-movement-container" class="tab-pane active"></div>
        </div>
    </section>
</body>
</html>