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
            var getSymbolGroupFormUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSymbolGroupForm')}";
            var symbolGroupActionUrl = "${createLink(controller: 'supplier', action: 'ajaxSymbolGroupAction')}"

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

            function doSymbolGroupAction(symbolGroupId) {
                $.ajax({
                    url: symbolGroupActionUrl,
                    method: "GET",
                    data: { symbolGroupId: symbolGroupId },
                    success: function(resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp + '</div>');
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp + '</div>');
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
                let form = $("#affiliationForm");

                form.hide();

                $.ajax({
                    url: getSymbolGroupFormUrl,
                    method: "GET",
                    data: {
                        symbolGroupId: symbolGroupId,
                    },
                    success: function(resp) {
                        form.html(resp);
                        form.show();
                    }
                });
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

        <section id="suppliers-container" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Suppliers</h2>
            </div>

            <div class="row mt-4 ml-0 mr-0">
                <div class="col-2 offset-10 text-right px-0">
                    <a href="#" class="btn btn-wl" onclick="showAddSupplierModal();">Add New Supplier</a>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="supplierSearchResults" />
            </div>
        </section>

        <section id="subscriptions-container" class="container-fluid">
            <div class="header-wl mt-5">
                <h2 class="mx-auto">Supplier Affiliations</h2>
            </div>

            <div class="row mt-4 ml-0 mr-0">
                <div class="col-2 offset-10 text-right px-0">
                    <a href="#" class="btn btn-wl" onclick="showAddSymbolGroupSubscriptionModal();">Add New Supplier Affiliation</a>
                </div>
            </div>

            <section id="errors-container" class="container-fluid"></section>

            <div id="subscriptions-results-container" class="align-content-center">
                <g:render template="symbolGroupSubscriptionsSearchResults" />
            </div>
        </section>

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