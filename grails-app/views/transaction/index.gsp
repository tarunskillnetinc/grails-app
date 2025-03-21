<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Transaction Viewer</title>

        <asset:stylesheet src="receipt.css" />
        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="validators/input-validator.js" />

        <script type='text/javascript'>
            var getReceiptsUrl = "${createLink(controller: 'transaction', action: 'ajaxGetReceipts')}";
            var getReceiptUrl = "${createLink(controller: 'transaction', action: 'ajaxGetReceipt')}";

            $(document).ready(function () {
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
            })

            $(function() {
                $('#startDate').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    startDate: "${(new Date() - 90).format("dd/MM/yyyy")}",
                    endDate: "${new Date().format("dd/MM/yyyy")}",
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
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

                getReceipts();
            });

            function getReceipts(sort, order, offset, max) {
                $("#search-results").hide();
                $("#loading-indicator").show();

                var startDate = $("#startDate").val();
                var endDate = $("#endDate").val();
                var tillId = $("#tillId").val();
                var transactionId = $("#transactionId").val();

                $.ajax({
                    url: getReceiptsUrl,
                    method: "GET",
                    data: { startDate: startDate, endDate: endDate, sort: sort, order: order, offset: offset, max: max, tillId: tillId, transactionId: transactionId },
                    success: function(resp) {
                        $("#results-container").html(resp);
                        $("#errors-container").html('');
                    }, error: function(xhr, exception) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert"><ul>' +xhr.responseText+ '</ul></div>');
                        $("#loading-indicator").hide();
                    }
                });
            }

            function showReceiptModal(receiptId) {
                $("#receiptModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#receiptModal').modal({ show: true });

                $.ajax({
                    url: getReceiptUrl,
                    method: "GET",
                    data: { receiptId: receiptId },
                    success: function(resp) {
                        $("#receiptModalContent").html(resp);
                    }
                });
            }

            function resetForm() {
                document.getElementById('startDate').value = "${startDate.toString("dd/MM/yyyy")}";
                $('#startDate').datepicker('setStartDate', "${(new Date() - 90).format("dd/MM/yyyy")}");
                $('#startDate').datepicker('setEndDate', "${new Date().format("dd/MM/yyyy")}");

                document.getElementById("endDate").value = "${endDate.toString("dd/MM/yyyy")}";
                $('#endDate').datepicker('setStartDate', "${new Date().format("dd/MM/yyyy")}");
                $('#endDate').datepicker('setEndDate', "${new Date().format("dd/MM/yyyy")}");

                document.getElementById('tillId').value = null;
                document.getElementById('transactionId').value = null;
            }

            function limitInputLength(input, maxLength) {
                if (input.value.length > maxLength) {
                    input.value = input.value.slice(0, maxLength);
                }
            }

            function printReceipt() {
                var mywindow = window.open("", "PRINT", "height=800,width=426");

                mywindow.document.write("<html><head>");
                mywindow.document.write("<link rel='stylesheet' href='${asset.assetPath(src: "receipt.css")}' type='text/css' />");
                mywindow.document.write("<link rel='stylesheet' href='${asset.assetPath(src: "receiptprint.css")}' type='text/css' />");
                mywindow.document.write("<\/head>");
                mywindow.document.write('<body style="max-width:423px;">');
                mywindow.document.write($("#receiptModalContent").html());
                mywindow.document.write("</body></html>");

                mywindow.onafterprint = () => {  $.ajax({
                    url: "${createLink(controller: 'receipt', action: 'saveReceiptPrinted')}",
                    method: "GET"
                })

                mywindow.document.close(); // necessary for IE >= 10
                mywindow.focus(); // necessary for IE >= 10*/

                ;}
                // Running this after a short delay because I assume the CSS hasn't properly rendered before the print dialog kicks in so the printed document isn't styled correctly.
                setTimeout(() => {
                    mywindow.print();
                    mywindow.close();
                }, 300);

                return true;


            }
    </script>
    </head>

<body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Transaction Search</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="header-container" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Transaction Search</h2>
            </div>
        </section>

        <section id="errors-container" class="container-fluid">
        </section>

        <section id="shifts-container" class="container-fluid">
            <div class="row mt-4">
                <div class="col-5">
                    <div class="card bg-light border-wl">
                        <div id="filter-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
                            <div class="row">
                                <div id="filter-text" class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-body collapse show" id="filterCollapse">
                            <g:form name="filtersForm" id="filtersForm">
                                <div class="form-group row">
                                    <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                                    <div class="col-4">
                                        <g:textField name="startDate" onkeydown="return false" class="form-control bottom-border" value="${startDate.toString("dd/MM/yyyy")}" autocomplete="off" />
                                    </div>

                                    <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                                    <div class="col-4">
                                        <g:textField name="endDate" onkeydown="return false" class="form-control bottom-border" value="${endDate.toString("dd/MM/yyyy")}" autocomplete="off" />
                                    </div>
                                </div>

                                <div class="form-group row">
                                    <label for="tillId" class="col-2 col-form-label-sm text-right">Till ID</label>
                                    <div class="col-4">
                                        <g:field type="number" name="tillId" step="1" min="1" max="999999999" class="form-control bottom-border" autocomplete="off" onkeydown="acceptNumeric(event);" oninput="limitInputLength(this,9); validateInput(this);"/>
                                    </div>

                                    <label for="transactionId" class="col-2 col-form-label-sm text-right">Transaction Number</label>
                                    <div class="col-4">
                                        <g:field type="number" name="transactionId" step="1" min="1" max="999999999" class="form-control bottom-border" autocomplete="off" onkeydown="acceptNumeric(event);" oninput="limitInputLength(this,9);"/>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-12 text-right">
                                        <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm();">Reset Filters</button>
                                        <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getReceipts();">Search</button>
                                    </div>
                                </div>
                            </g:form>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="receiptViewerResults" />
            </div>
        </section>

        <section id="receipt-modal" class="container-fluid">
            <!-- Receipt modal -->
            <div class="modal fade" id="receiptModal" tabindex="-1" role="dialog" aria-labelledby="receiptModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2>Receipt Viewer</h2>
                        </div>

                        <div id="receiptModalContent"></div>

                        <div class="modal-footer">
                            <button type="button" id="printReceiptButton" class="btn btn-info mr-auto" onclick="printReceipt();">Print</button>
                            <button type="button" id="closeReceiptModalButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>