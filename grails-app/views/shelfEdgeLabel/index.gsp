<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Shelf Edge Labels</title>

        <asset:stylesheet href="radio.css" />

        <script type="text/javascript">
            $(document).ready(function () {
                getAdHocBatches();
                getScheduledBatches();
            });

            function getAdHocBatches() {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGetAdHocBatches')}";

                $("#ad-hoc-results").hide();
                $("#ad-hoc-loading-indicator").show();

                $.ajax({
                    url: url,
                    // data: { },
                    success: function(resp) {
                        $('#ad-hoc-container').html(resp);
                    }
                });
            }

            function getScheduledBatches() {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGetScheduledBatches')}";

                $("#scheduled-results").hide();
                $("#scheduled-loading-indicator").show();

                $.ajax({
                    url: url,
                    // data: { },
                    success: function(resp) {
                        $('#scheduled-container').html(resp);
                    }
                });
            }

            function adHocBatchTemplateSelected(productListId, selectedLabelTemplate) {
                if (selectedLabelTemplate.value === "0") {
                    return;
                }

                var selectedTemplateDescription = selectedLabelTemplate.options[selectedLabelTemplate.selectedIndex].text

                $("#confirmPrintModalContent").html("Are you sure you wish to generate an ad-hoc label batch using the " +selectedTemplateDescription +" template?");

                var confirmModalYesButton = $('#confirmPrintModalYesButton');
                var confirmModalNoButton = $('#confirmPrintModalNoButton');

                confirmModalYesButton.click({productListId: productListId, labelTemplateId: selectedLabelTemplate.value, type: "AD_HOC"}, confirmPrintModalYesButtonClicked);
                confirmModalYesButton.prop("disabled", false);
                confirmModalNoButton.click(confirmPrintModalNoButtonClicked);
                confirmModalNoButton.prop("disabled", false);

                $('#confirmPrintModal').modal({ show: true });
            }

            function scheduledBatchTemplateSelected(effectiveDate, selectedLabelTemplate) {
                if (selectedLabelTemplate.value === "0") {
                    return;
                }

                var selectedTemplateDescription = selectedLabelTemplate.options[selectedLabelTemplate.selectedIndex].text

                $("#confirmPrintModalContent").html("Are you sure you wish to generate a scheduled batch using the " +selectedTemplateDescription +" template?");

                var confirmModalYesButton = $('#confirmPrintModalYesButton');
                var confirmModalNoButton = $('#confirmPrintModalNoButton');

                confirmModalYesButton.click({effectiveDate: effectiveDate, labelTemplateId: selectedLabelTemplate.value, type: "SCHEDULED"}, confirmPrintModalYesButtonClicked);
                confirmModalYesButton.prop("disabled", false);
                confirmModalNoButton.click(confirmPrintModalNoButtonClicked);
                confirmModalNoButton.prop("disabled", false);

                $('#confirmPrintModal').modal({ show: true });
            }

            function confirmPrintModalYesButtonClicked(event) {
                var confirmModalYesButton = $('#confirmModalYesButton');
                var confirmModalNoButton = $('#confirmModalNoButton');

                confirmModalYesButton.off("click");
                confirmModalYesButton.prop("disabled", true);

                confirmModalNoButton.off("click");
                confirmModalNoButton.prop("disabled", true);

                if (event.data.type === "SCHEDULED") {
                    printScheduledBatch(event.data.effectiveDate, event.data.labelTemplateId);
                } else if (event.data.type === "AD_HOC") {
                    printAdHocBatch(event.data.productListId, event.data.labelTemplateId);
                }
            }

            function confirmPrintModalNoButtonClicked() {
                $('#confirmPrintModal').modal("hide");
            }

            function printAdHocBatch(productListId, labelTemplateId) {
                $("#confirmPrintModalContent").html("<div class=\"modal-body\"><div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait...</h3></div></div><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGenerateAdHocPdf')}";

                var params = { productListId: productListId, labelTemplateId: labelTemplateId, printProcess: 'SHELF_EDGE_LABEL_BATCH', printType: 'PDF' };

                var confirmSuccessModalYesButton = $('#confirmSuccessModalYesButton');

                confirmSuccessModalYesButton.click({productListId: productListId}, confirmAdHocBatchPrintSuccessful);
                confirmSuccessModalYesButton.prop("disabled", false);

                $.post({
                    url: url,
                    xhrFields: { responseType: "blob" },
                    data: params,
                    success: function(data) {
                        var blob = new Blob([data], {type: "application/pdf"});
                        var blobUrl = URL.createObjectURL(blob);

                        var link = $("<a>").attr({href: blobUrl, download: "AdHocBatch.pdf"}).click();

                        link[0].click();

                        $('#confirmPrintModal').modal("hide");
                        $('#confirmSuccessModal').modal({ show: true });
                    }
                });
            }

            function printScheduledBatch(effectiveDate, labelTemplateId) {
                $("#confirmPrintModalContent").html("<div class=\"modal-body\"><div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait...</h3></div></div><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGenerateScheduledPdf')}";

                var params = { effectiveDate: effectiveDate, labelTemplateId: labelTemplateId, printProcess: 'SHELF_EDGE_LABEL_BATCH', printType: 'PDF' };

                var confirmSuccessModalYesButton = $('#confirmSuccessModalYesButton');

                confirmSuccessModalYesButton.click({effectiveDate: effectiveDate}, confirmScheduledBatchPrintSuccessful);
                confirmSuccessModalYesButton.prop("disabled", false);

                $.post({
                    url: url,
                    xhrFields: { responseType: "blob" },
                    data: params,
                    success: function(data) {
                        var blob = new Blob([data], {type: "application/pdf"});
                        var blobUrl = URL.createObjectURL(blob);

                        var link = $("<a>").attr({href: blobUrl, download: "ScheduledBatch-" +effectiveDate +".pdf"}).click();

                        link[0].click();

                        $('#confirmPrintModal').modal("hide");
                        $('#confirmSuccessModal').modal({ show: true });
                    }
                });
            }

            function confirmAdHocBatchPrintSuccessful(event) {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxConfirmAdHocBatchPrintSuccessful')}";

                var params = { productListId: event.data.productListId };

                $.ajax({
                    url: url,
                    type: "POST",
                    data: params,
                    success: function(resp) {
                        $('#confirmSuccessModal').modal("hide");

                        getAdHocBatches();
                    }
                });
            }

            function confirmScheduledBatchPrintSuccessful(event) {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxConfirmScheduledBatchPrintSuccessful')}";

                var params = { effectiveDate: event.data.effectiveDate };

                $.ajax({
                    url: url,
                    type: "POST",
                    data: params,
                    success: function(resp) {
                        $('#confirmSuccessModal').modal("hide");

                        getScheduledBatches();
                    }
                });
            }

            function deleteProductListButtonPressed(productListId) {
                $("#confirmPrintModalContent").html("Are you sure you wish to delete this batch?");

                var confirmModalYesButton = $('#confirmPrintModalYesButton');
                var confirmModalNoButton = $('#confirmPrintModalNoButton');

                confirmModalYesButton.click({productListId: productListId}, confirmDeleteProductList);
                confirmModalYesButton.prop("disabled", false);
                confirmModalNoButton.click(confirmPrintModalNoButtonClicked);
                confirmModalNoButton.prop("disabled", false);

                $('#confirmPrintModal').modal({ show: true });
            }

            function confirmDeleteProductList(event) {
                var confirmModalYesButton = $('#confirmModalYesButton');
                var confirmModalNoButton = $('#confirmModalNoButton');

                confirmModalYesButton.off("click");
                confirmModalYesButton.prop("disabled", true);

                confirmModalNoButton.off("click");
                confirmModalNoButton.prop("disabled", true);

                $("#confirmPrintModalContent").html("<div class=\"modal-body\"><div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait...</h3></div></div><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxDeleteProductList')}";

                var params = { productListId: event.data.productListId };

                $.ajax({
                    url: url,
                    data: params,
                    success: function(data) {
                        $('#confirmPrintModal').modal("hide");

                        getAdHocBatches();
                    }
                });
            }
        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">Shelf Edge Labels</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Shelf Edge Labels</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <div class="row mt-4 ml-0 mr-0">
                <div class="col-6">
                    <div class="header-wl mt-3"><h3>Handheld batches</h3></div>

                    <div class="row col-10 offset-1 mt-5 px-0 pb-2 table-wl bottom-border">
                        <div class="col-4 font-weight-bold">Description</div>
                        <div class="col-2 font-weight-bold">Date Started</div>
                        <div class="col-2 font-weight-bold">Label Count</div>
                        <div class="col-3 font-weight-bold">Print</div>
                        <div class="col-1">&nbsp;</div>
                    </div>

                    <div id="ad-hoc-container" class="align-content-center">
                        <g:render template="adHocResults" />
                    </div>
                </div>

                <div class="col-6">
                    <div class="header-wl mt-3"><h3>Scheduled product changes</h3></div>

                    <div class="row col-10 offset-1 mt-5 px-0 pb-2 table-wl bottom-border">
                        <div class="col-6 font-weight-bold">Effective Date</div>
                        <div class="col-3 font-weight-bold">Label Count</div>
                        <div class="col-3 font-weight-bold">Print / Confirm</div>
                    </div>

                    <div id="scheduled-container" class="align-content-center">
                        <g:render template="scheduledResults" />
                    </div>
                </div>
            </div>
        </section>

        <!-- Confirm print modal -->
        <section id="confirm-print-modal" class="container-fluid">
            <div class="modal fade" id="confirmPrintModal" tabindex="-1" role="dialog" aria-labelledby="confirmPrintModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 id="confirmPrintModalHeader">Generate Labels</h2>
                        </div>

                        <div class="modal-body" id="confirmPrintModalContent">Are you sure you wish to generate an ad-hoc batch of labels using the Standard Label template?</div>

                        <div class="modal-footer">
                            <button type="button" id="confirmPrintModalNoButton" class="btn btn-wl" data-dismiss="modal">No</button>
                            <button type="button" id="confirmPrintModalYesButton" class="btn btn-success">Yes</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Confirm success modal -->
        <section id="confirm-success-modal" class="container-fluid">
            <div class="modal fade" id="confirmSuccessModal" tabindex="-1" role="dialog" aria-labelledby="confirmSuccessModalLabel" aria-hidden="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 id="confirmSuccessModalHeader">Generate Labels</h2>
                        </div>

                        <div class="modal-body" id="confirmSuccessModalContent">Did the batch print successfully?</div>

                        <div class="modal-footer">
                            <button type="button" id="confirmSuccessModalNoButton" class="btn btn-wl" data-dismiss="modal">No</button>
                            <button type="button" id="confirmSuccessModalYesButton" class="btn btn-success">Yes</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>