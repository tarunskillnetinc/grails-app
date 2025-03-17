<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Trust Retail - Banking Report</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="reporting.js"/>

    <script type="application/javascript">
        var reportType = "${reportType}";
        var getDataUrl = "${createLink(controller: 'cashReporting', action: 'ajaxGetBankingReport')}";

        $(document).ready(function () {
            <g:if test="${stores.size() > 1}">
                $('#startDate').prop("disabled", true);
                $('#endDate').prop("disabled", true);
                $('#bankingTypeId').prop("disabled", true);
                $('#banking-report-button').prop("disabled", true);
            </g:if>

            $('#storeFilter').on("change", function () {
                var selectedValue = $(this).val();

                if (selectedValue !== '') {
                    $('#startDate').prop("disabled", false);
                    $('#endDate').prop("disabled", false);
                    $('#bankingTypeId').prop("disabled", false);
                    $('#banking-report-button').prop("disabled", false);
                } else {
                    $('#startDate').prop("disabled", true);
                    $('#endDate').prop("disabled", true);
                    $('#bankingTypeId').prop("disabled", true);
                    $('#banking-report-button').prop("disabled", true);
                }
            });

            $('#startDate').on("change", function () {
                $('#startDate').val(this.value);
                $('#startDate').removeClass('is-invalid');
                $('#endDate').datepicker('setStartDate', this.value);
            });

            $('#endDate').on("change", function () {
                $('#endDate').val(this.value);
                $('#endDate').removeClass('is-invalid');
                $('#startDate').datepicker('setEndDate', this.value);
            });
        });

        $(function() {
            $('#startDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                /* Allow up to a year in the past */
                startDate: "${(new Date() - 366).format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function(e) {
                let startDate = e.date;
                let endDate = new Date(startDate);
                let currentDate = new Date();
                currentDate.setHours(0,0,0,0);

                $('#endDate').datepicker('setStartDate', startDate);
                $('#endDate').datepicker('setEndDate', currentDate);
                $('#endDate').datepicker('setDate', currentDate);
            });

            $('#endDate').datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                startDate: "${new Date().format("dd/MM/yyyy")}",
                endDate: "${new Date().format("dd/MM/yyyy")}",
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            });
        });
    </script>
</head>

<body>
    <section id="reporting-container" class="container-fluid">
        <g:reportBreadcrumb reportType="${reportType}"/>

        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Banking Report</h2>
        </div>

        <div class="row mt-4">
            <div class="col-12">
                <div class="card bg-light border-wl">
                    <div id="reportParams" class="card-body">
                        <g:form name="filtersForm" id="filtersForm">
                            <div class="form-group row">

                                <div class="row col-xl-5 col-12 mb-4">
                                    <label for="storeIdSelect" class="col-2 col-form-label-sm text-right">Store ID:</label>
                                    <div class="col-4">

                                        <g:select name="storeFilter" from="${stores}" optionValue="${{it.config.storeNumber}}"
                                                  optionKey="id"
                                                  noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'Please Select']}"
                                                  class="form-control select-border"
                                                  disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                                    </div>

                                    <label for="startDate" class="col-2 col-form-label-sm text-right">From Date:</label>
                                    <div class="col-4">
                                        <g:textField id="startDate" name="startDate" onkeydown="return false" class="form-control bottom-border text-center" value="${startDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                                    </div>
                                </div>

                                <div class="row col-xl-5 col-12 mb-4">
                                    <label for="endDate" class="col-2 col-form-label-sm text-right">To Date:</label>
                                    <div class="col-4">
                                        <g:textField id="endDate" name="endDate" onkeydown="return false" class="form-control bottom-border text-center" value="${endDate?.toString("dd/MM/yyyy")}" autocomplete="off" />
                                    </div>

                                    <label for="storeFilter2" class="col-3 col-form-label-sm text-right">Banking Type:</label>
                                    <div class="col-3">
                                        <g:select id="bankingTypeId" name="bankingType" from="${bankingType}" noSelection="${['': 'All']}" value="everything" class="form-control select-border" />
                                    </div>
                                </div>

                            </div>
                            <div class="form-group row">
                                <div class="col-12 text-right">
                                    <button id="banking-report-button" type="button" class="col-xl-3 col-5 btn btn-wl text-center" onclick="filterReport()">Get Report</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
        
        <div id="results-container" class="align-content-center"></div>

        <div class="col mt-4 mb-4 text-right" >
            <g:link elementId="cancel-btn" class="btn btn-wl text-center" uri="/" role="button" >Cancel</g:link>
        </div>
    </section>
</body>
</html>