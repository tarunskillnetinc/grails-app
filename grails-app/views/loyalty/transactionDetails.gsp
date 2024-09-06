<%@ page import="org.joda.time.DateTimeZone; org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>Transaction Details</title>

        <asset:javascript src="jquery.js" />
        <asset:javascript src="jquery-ui.js" />
        <asset:stylesheet src="receipt.css" />

        <script type='text/javascript'>
            var getReceiptUrl = "${createLink(controller: 'receipt', action: 'ajaxGetReceiptByTransaction')}";

            function showReceiptModal(transactionId, storeId, terminalId) {
                if (transactionId > 0) {
                    $("#receiptModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                    $('#receiptModal').modal({show: true});

                    $.ajax({
                        url: getReceiptUrl,
                        method: "GET",
                        data: {transactionId: transactionId, storeId: storeId, terminalId: terminalId},
                        success: function (resp) {
                            console.log("here: ", resp);
                            $("#receiptModalContent").html(resp);
                        }
                    });
                } else {
                    $("#receiptModalContent").html("<div class=\"modal-body\"><h4 class=\"d-flex justify-content-center\">Error loading receipt</h4></div>");
                    $('#receiptModal').modal({show: true});
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

                mywindow.document.close(); // necessary for IE >= 10
                mywindow.focus(); // necessary for IE >= 10*/

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
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link action="loyaltyMembers">Membership Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page"><g:link action="showMemberDetails" params="[cardNumber: cardNumber]">${cardNumber}</g:link></li>
                            <li id="breadcrumb-4" class="breadcrumb-item" aria-current="page"><g:link action="transactions" params="[cardNumber: cardNumber]">Member Transactions</g:link></li>
                            <li id="breadcrumb-5" class="breadcrumb-item active" aria-current="page">${transaction?.id ?: "Transaction Details"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="details-header" class="container-fluid">
            <div class="row header-wl mt-3">

                <div class="col-6 offset-3">
                    <h2 id="page-title" class="mx-auto my-auto">Transaction Details</h2>
                </div>
                <div class="col-3 text-right d-inline-flex flex-row justify-content-end">
                    <div id="add-new-product-btn" onclick="showReceiptModal(${transaction.transactionId}, ${transaction.storeId}, ${transaction.terminalId});" class="btn btn-wl p-2 ml-2">View Receipt</div>
                </div>
            </div>
        </section>

        <section>
            <div class="card-body pt-5">
                <div class="row">
                    <div class="col-12 col-lg-5 offset-lg-1">
                        <div class="row form-group mb-3">
                            <label for="storeId" class="col-4 col-form-label text-right pr-4">Store Id</label>
                            <g:textField name="storeId" type="text" nullable="true" class="col-5 form-control bottom-border" value="${transaction?.storeId ?: 0}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="storeName" class="col-4 col-form-label text-right pr-4">Store Name</label>
                            <g:textField name="storeName" type="text" nullable="true" class="col-5 form-control bottom-border" value="${transaction?.store?.name ?: ''}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="offerDescription" class="col-4 col-form-label text-right pr-4">Offer Description</label>
                            <g:textField name="offerDescription" type="text" nullable="true" class="col-5 form-control bottom-border" value="${transaction?.redeemedOffer?.loyaltyOffer?.offerDescription ?: ''}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="transactionDate" class="col-4 col-form-label text-right pr-4">Transaction Date</label>
                            <g:textField name="transactionDate" class="col-5 form-control bottom-border add-product-desc" value="${transaction?.transactionTimestamp ? DateTimeFormat.forPattern('hh:mm:ss dd/MM/yyyy').withZone(DateTimeZone.UTC).print(transaction.transactionTimestamp) : ''}" readonly="true" />
                        </div>
                    </div>
                    <div class="col-12 col-lg-6">
                        <div class="row form-group mb-3">
                            <label for="transactionTotal" class="col-4 col-form-label text-right pr-4">Transaction Total</label>
                            <div class="input-group-prepend">
                                <span class="input-group-text">&pound;</span>
                            </div>
                            <g:textField name="transactionTotal" class="col-3 form-control bottom-border add-product-desc" value="${String.format("%.2f", transaction?.transactionTotal ?: 0.00)}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="totalDiscount" class="col-4 col-form-label text-right pr-4">Total Discount</label>
                            <div class="input-group-prepend">
                                <span class="input-group-text">&pound;</span>
                            </div>
                            <g:textField name="totalDiscount" class="col-3 form-control bottom-border add-product-desc" value="${String.format("%.2f", transaction?.transactionDiscount ?: 0.00)}" readonly="true" />
                        </div>
                        <div class="row form-group mb-3">
                            <label for="amountSaved" class="col-4 col-form-label text-right pr-4">Amount Saved Per Offer</label>
                            <div class="input-group-prepend">
                                <span class="input-group-text">&pound;</span>
                            </div>
                            <g:textField name="amountSaved" class="col-3 form-control bottom-border add-product-desc" value="${transaction?.redeemedOffer?.awardValue != null ? String.format("%.2f", transaction.redeemedOffer.awardValue) : ''}" readonly="true" />
                        </div>

                    </div>
                </div>
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