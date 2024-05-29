<!doctype html>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Member Transactions</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="money-mask.js" />

        <script type="text/javascript">
            $(document).ready(function () {
                var changingDate = false;
                $(".mask-money").maskMoney({ allowZero: true });
                $(".mask-money").maskMoney('mask');

                $('#startWindowFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#endWindowFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#endWindowFilter').change(function() {
                    if (!changingDate) {
                        changingDate = true;
                        validateAndCorrectDates();
                        changingDate = false;
                    }
                });

                // Add change event listener to offer start date to validate and correct dates
                $('#startWindowFilter').change(function() {
                    if (!changingDate) {
                        changingDate = true;
                        validateAndCorrectDates();
                        changingDate = false;
                    }
                });
            });

            function validateAndCorrectDates() {
                var startDate = $('#startWindowFilter').datepicker('getDate');
                var endDate = $('#endWindowFilter').datepicker('getDate');
                var today = new Date();

                // Reset time components to 00:00:00 to compare dates only
                today.setHours(0, 0, 0, 0);

                // Check if start date is in the future
                if (startDate > today) {
                    $('#startWindowFilter').datepicker('setDate', today);
                }

                // Re-fetch dates after potential adjustments
                startDate = $('#startWindowFilter').datepicker('getDate');

                // If endDate is selected, validate it
                if (endDate !== null) {
                    // Check if end date is in the future
                    if (endDate > today) {
                        $('#endWindowFilter').datepicker('setDate', today);
                    }

                    // Re-fetch endDate after potential adjustment
                    endDate = $('#endWindowFilter').datepicker('getDate');

                    // Check if end date is before start date
                    if (endDate < startDate) {
                        // Set end date to 1 week from start date
                        // var newEndDate = new Date(startDate);
                        // newEndDate.setDate(startDate.getDate());
                        $('#endWindowFilter').datepicker('setDate', today);
                    }
                }
            }

            function resetForm() {
                document.getElementById('memberTransactionSearchTerm').value = null;
                document.getElementById('memberTransactionSearchBy').value = 'storeId';
                document.getElementById('startWindowFilter').value = null;
                document.getElementById('endWindowFilter').value = null;
                document.getElementById('spendAmountMin').value = 0;
                document.getElementById('spendAmountMax').value = 0;

                $(".mask-money").maskMoney({ allowZero: true });
                $(".mask-money").maskMoney('mask');
            }

            function searchTransactions(sortParams) {
                $("#search-results").hide();
                $("#loading-indicator").show();

                let url = "${createLink(controller: 'loyalty', action: 'ajaxMemberTransactions')}";

                let cardNumber = $('#cardNumber').val();

                let searchTerm = $('#memberTransactionSearchTerm').val();
                let searchBy = $('#memberTransactionSearchBy').val();

                let startWindow = $('#startWindowFilter').val();
                let endWindow = $('#endWindowFilter').val();
                let minAmount = $('#spendAmountMin').val();
                let maxAmount = $('#spendAmountMax').val();

                $.ajax({
                    url: url,
                    data: {
                        cardNumber: cardNumber,
                        searchTerm: searchTerm,
                        searchBy: searchBy,
                        startWindow: startWindow,
                        endWindow: endWindow,
                        minAmount: minAmount,
                        maxAmount: maxAmount,
                        max: sortParams ? sortParams["max"] : null,
                        offset: sortParams ? sortParams.offset : null,
                        sortColumn: sortParams ? sortParams.sortColumn : null,
                        sortOrder: sortParams ? sortParams.sortOrder : null
                    },
                    success: function(resp) {
                        $('#results-container').html(resp);
                        $('#memberTransactionSearchTerm').data('prev',$('#memberTransactionSearchTerm').val());
                        $('#memberTransactionSearchBy').data('prev', $('#memberTransactionSearchBy').val());
                    }
                });
            }
        </script>
    </head>

    <body>
        <g:hiddenField name="cardNumber" id="cardNumber" value="${cardNumber}" />

        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link action="loyaltyMembers">Membership Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page"><g:link action="showMemberDetails" params="[cardNumber: cardNumber]">${cardNumber}</g:link></li>
                            <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">Member Transactions</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="member-transactions-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="page-title" class="mx-auto my-auto">Member Transactions</h2>
                </div>
            </div>

            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label for="memberTransactionSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                <div class="col-10 input-group">
                                    <g:textField id="memberTransactionSearchTerm" name="memberTransactionSearchTerm" maxlength="100" class="form-control" aria-describedby="select-addon2" />
                                    <div class="input-group-append">
                                        <g:select id="memberTransactionSearchBy" name="memberTransactionSearchBy" from="${['storeId', 'transactionId']}" value="everything" valueMessagePrefix="MemberTransactionSearchBy" class="form-control select-border" style="z-index: 0;" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="startWindowFilter" class="col-3 col-form-label-sm text-right">Transaction Start Window</label>
                                <div class="col-3">
                                    <g:textField name="startWindowFilter" onkeydown="return false" id="startWindowFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>

                                <label for="endWindowFilter" class="col-3 col-form-label-sm text-right">Transaction End Window</label>
                                <div class="col-3">
                                    <g:textField name="endWindowFilter" onkeydown="return false" id="endWindowFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="spendAmountMin" class="col-3 col-form-label-sm text-right">Spend Amount Min</label>
                                <div class="col-3 input-group">
                                    <g:textField id="spendAmountMin" name="spendAmountMin" class="form-control mask-money" aria-describedby="select-addon2" />
                                </div>
                                <label for="spendAmountMax" class="col-3 col-form-label-sm text-right">Spend Amount Max</label>
                                <div class="col-3 input-group">
                                    <g:textField id="spendAmountMax" name="spendAmountMax" class="form-control mask-money" aria-describedby="select-addon2" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-10 offset-2 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchTransactions()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="transactionSearchResults" />
            </div>
        </section>
    </body>
</html>