<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Suppliers & Affiliations</title>

        <script type='text/javascript'>
            var getSuppliersUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSuppliers')}";
            var getSymbolGroupSubscriptionsUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSymbolGroupSubscriptions')}";
            var addSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxAddSupplier')}";
            var editSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxEditSupplier')}";
            var saveSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxSaveSupplier')}";
            var addSymbolGroupSubscriptionUrl = "${createLink(controller: 'supplier', action: 'ajaxAddSymbolGroupSubscription')}";
            var editSymbolGroupSubscriptionUrl = "${createLink(controller: 'supplier', action: 'ajaxEditSymbolGroupSubscription')}";
            var saveSymbolGroupSubscriptionUrl = "${createLink(controller: 'supplier', action: 'ajaxSaveSymbolGroupSubscription')}";

            $(function() {
                getSuppliers();
                getSymbolGroupSubscriptions();
            });

            function getSuppliers() {
                $("#search-results").hide();
                $("#loading-indicator").show();

                $.ajax({
                    url: getSuppliersUrl,
                    method: "GET",
                    success: function(resp) {
                        $("#results-container").html(resp);
                    }
                });
            }

            function getSymbolGroupSubscriptions() {
                $("#subscriptions-search-results").hide();
                $("#subscriptions-loading-indicator").show();

                $.ajax({
                    url: getSymbolGroupSubscriptionsUrl,
                    method: "GET",
                    success: function(resp) {
                        $("#subscriptions-results-container").html(resp);
                    }
                });
            }

            function showAddSupplierModal() {
                $("#addSupplierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#addSupplierModal').modal({ show: true });

                $.ajax({
                    url: addSupplierUrl,
                    method: "GET",
                    success: function(resp) {
                        $("#addSupplierContent").html(resp);
                    }
                });
            }

            function editSupplier(supplierId) {
                $("#addSupplierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#addSupplierModal').modal({ show: true });

                $.ajax({
                    url: editSupplierUrl,
                    method: "GET",
                    data: { supplierId: supplierId },
                    success: function(resp) {
                        $("#addSupplierContent").html(resp);
                    }
                });
            }

            function saveSupplier() {
                var formValues = $("#addSupplierForm").serialize();

                $("#addSupplierContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>");

                $.ajax({
                    url: saveSupplierUrl,
                    method: "POST",
                    data: formValues,
                    success: function(resp) {
                        if (resp === "OK") {
                            $('#addSupplierModal').modal('hide')

                            getSuppliers();
                        } else {
                            $("#addSupplierContent").html(resp);
                        }
                    }
                });
            }

            function showAddSymbolGroupSubscriptionModal() {
                $("#addSymbolGroupSubscriptionContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#addSymbolGroupSubscriptionModal').modal({ show: true });

                $.ajax({
                    url: addSymbolGroupSubscriptionUrl,
                    method: "GET",
                    success: function(resp) {
                        $("#addSymbolGroupSubscriptionContent").html(resp);
                    }
                });
            }

            function symbolGroupSubscripionSupplierChanged(symbolGroupId) {
                var nisaForm = $("#nisaForm");

                nisaForm.hide();

                if (symbolGroupId === "1") {
                    nisaForm.show();
                }
            }

            function editSymbolGroupSubscription(symbolGroupSubscriptionId) {
                $("#addSymbolGroupSubscriptionContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $('#addSymbolGroupSubscriptionModal').modal({ show: true });

                $.ajax({
                    url: editSymbolGroupSubscriptionUrl,
                    method: "GET",
                    data: { symbolGroupSubscriptionId: symbolGroupSubscriptionId },
                    success: function(resp) {
                        $("#addSymbolGroupSubscriptionContent").html(resp);
                    }
                });
            }

            function saveSymbolGroupSubscription() {
                var formValues = $("#addSymbolGroupSubscriptionForm").serialize();

                $("#addSymbolGroupSubscriptionContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>");

                $.ajax({
                    url: saveSymbolGroupSubscriptionUrl,
                    method: "POST",
                    data: formValues,
                    success: function(resp) {
                        if (resp === "OK") {
                            $('#addSymbolGroupSubscriptionModal').modal('hide')

                            getSymbolGroupSubscriptions();
                        } else {
                            $("#addSymbolGroupSubscriptionContent").html(resp);
                        }
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
                            <li class="breadcrumb-item active" aria-current="page">Suppliers & Affiliations</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <div class="col-12 col-sm-8 offset-sm-2 mt-5">
            <ul class="nav nav-tabs nav-fill tabs-wl" role="tablist">
                <li class="nav-item">
                    <a id="suppliers-tab" data-toggle="tab" href="#suppliers" aria-selected="true" role="tab" aria-controls="suppliers" class="nav-link active">Suppliers</a>
                </li>

                <li class="nav-item">
                    <a id="symbolGroupSubscriptions-tab" data-toggle="tab" href="#symbolGroupSubscriptions" role="tab" aria-controls="symbolGroupSubscriptions" class="nav-link">Supplier Affiliations</a>
                </li>

                <li class="nav-item">
                    <a id="supplierUpdates-tab" data-toggle="tab" href="#supplierUpdates" role="tab" aria-controls="supplierUpdates" class="nav-link disabled">Supplier Price Updates</a>
                </li>
            </ul>
        </div>

        <div class="tab-content" style="margin-top: 50px;">
            <!-- Suppliers -->
            <div class="tab-pane fade show active" id="suppliers" role="tabpanel" aria-labelledby="suppliers-tab">
                <section id="suppliers-container" class="container-fluid">
                    <div class="row header-wl mt-3">
                        <h2 class="mx-auto">Suppliers</h2>
                    </div>

                    <div class="row mt-4 ml-0 mr-0">
                        <div class="col-2 offset-10 text-right">
                            <a href="#" class="btn btn-wl" onclick="showAddSupplierModal();">Add New Supplier</a>
                        </div>
                    </div>

                    <div id="results-container" class="align-content-center">
                        <g:render template="supplierSearchResults" />
                    </div>
                </section>
            </div>

            <!-- Symbol group subscriptions -->
            <div class="tab-pane fade show" id="symbolGroupSubscriptions" role="tabpanel" aria-labelledby="symbolGroupSubscriptions-tab">
                <section id="subscriptions-container" class="container-fluid">
                    <div class="row header-wl mt-3">
                        <h2 class="mx-auto">Supplier Affiliations</h2>
                    </div>

                    <div class="row mt-4 ml-0 mr-0">
                        <div class="col-2 offset-10 text-right">
                            <a href="#" class="btn btn-wl" onclick="showAddSymbolGroupSubscriptionModal();">Add New Supplier Affiliation</a>
                        </div>
                    </div>

                    <div id="subscriptions-results-container" class="align-content-center">
                        <g:render template="symbolGroupSubscriptionsSearchResults" />
                    </div>
                </section>
            </div>

            <!-- Supplier price updates -->
            <div class="tab-pane fade show" id="supplierUpdates" role="tabpanel" aria-labelledby="supplierUpdates-tab">

            </div>
        </div>

        <section id="addSupplier-modal" class="container-fluid">
            <!-- Add supplier modal -->
            <div class="modal fade" id="addSupplierModal" tabindex="-1" role="dialog" aria-labelledby="addSupplierModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg" role="document">
                    <div id="addSupplierContent" class="modal-content">

                    </div>
                </div>
            </div>
        </section>

        <section id="addSymbolGroupSubscription-modal" class="container-fluid">
            <!-- Add symbol group subscription modal -->
            <div class="modal fade" id="addSymbolGroupSubscriptionModal" tabindex="-1" role="dialog" aria-labelledby="addSymbolGroupSubscriptionModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg" role="document">
                    <div id="addSymbolGroupSubscriptionContent" class="modal-content">

                    </div>
                </div>
            </div>
        </section>
    </body>
</html>