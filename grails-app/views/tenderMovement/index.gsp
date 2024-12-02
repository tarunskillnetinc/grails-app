<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Store Configuration</title>

    <asset:stylesheet src="multi-select-checks.css"/>

    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="popper.min.js"/>
    <asset:javascript src="multi-select-checks.js"/>
    <asset:javascript src="money-mask.js"/>

    <script type="text/javascript">

        $(document).ready(function () {
            // Select the first tab by default if none are active
            if (!$('.nav-link.active').length) {
                $('#issue-float-tab').addClass('active');
                $('#issue-float-container').addClass('active');
            }

            // Initial load of the first tab's content
            var firstTab = $('a[data-toggle="tab"]').first();
            var initialAction = getActionFromTabId(firstTab.attr('id'));
            $.get(initialAction, function (data) {
                $('#tender-movement-container').html(data);
            });


            $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                var selectedTab = $(e.target).attr('id');  // Get the ID of the selected tab
                var action = getActionFromTabId(selectedTab);  // Get the action based on the tab ID

                // Make AJAX call to load content for the selected tab
                $.get(action, function (data) {
                    console.log(data)
                    $('#tender-movement-container').html(data);
                });
            });


            // Function to determine the action based on the tab ID
            function getActionFromTabId(tabId) {
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
                        <sec:ifAnyGranted roles='ROLE_ENGINEER, ROLE_HEAD_OFFICE'>
                            <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link
                                        controller="store" action="index"
                                        params="[storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter, max: max, offset: offset, sort: sort, order: order]">Store Management</g:link></li>
                            </g:if>
                        </sec:ifAnyGranted>
                        <li id="breadcrumb-3" class="breadcrumb-item active"
                            aria-current="page">Store ${storeSettings?.config?.storeNumber} Configuration</li>
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
                           aria-selected="true" role="tab" aria-controls="issue-float-container"
                           class="nav-link active">Issue Float</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="tender-lift-tab" data-toggle="tab" href="#tender-lift-container" data-action="tenderLift"
                           role="tab" aria-controls="tender-lift-container" class="nav-link">Tender Lift</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="pay-in-tab" data-toggle="tab" href="#pay-in-container" data-action="payIn" role="tab"
                           aria-controls="pay-in-container" class="nav-link">Pay In</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="pay-out-tab" data-toggle="tab" href="#pay-out-container" data-action="payOut" role="tab"
                           aria-controls="pay-out-container" class="nav-link">Pay Out</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="bank-deposit-tab" data-toggle="tab" href="#bank-deposit-container" data-action="bankDeposit"
                           role="tab" aria-controls="bank-deposit-container" class="nav-link">Bank Deposit</a>
                    </li>
                    <li class="nav-item m-0 flex-shrink-0">
                        <a id="bank-receipt-tab" data-toggle="tab" href="#bank-receipt-container" data-action="bankReceipt"
                           role="tab" aria-controls="bank-receipt-container" class="nav-link">Bank Receipt</a>
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