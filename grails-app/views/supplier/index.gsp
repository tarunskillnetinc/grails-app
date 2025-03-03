<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Supplier Maintenance</title>

    <script type='text/javascript'>
        var globalSortParams = null;
        var getSuppliersUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSearchSupplier')}";
        var addSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxAddSupplier')}";
        var editSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxEditSupplier')}";
        var saveSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxSaveSupplier')}";
        var toggleSupplierDeletedUrl = "${createLink(controller: 'supplier', action: 'ajaxToggleSupplierDeletedFlag')}";

        $(function () {searchSupplier();}); //As soon as page open call this method

        //This will call supplier search method in supplier controller
        function searchSupplier(sortParams) {
            var supplierNameTerm = $('#supplierNameTerm').val();
            var supplierReferenceTerm = $('#supplierReferenceTerm').val();
            var customerReferenceTerm = $('#customerReferenceTerm').val();
            var includeDeletedSuppliers = $('#includeDeletedSuppliers').prop('checked');

            $('#results-container').html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");

            var params = {supplierNameTerm: supplierNameTerm,
                supplierReferenceTerm: supplierReferenceTerm,customerReferenceTerm: customerReferenceTerm, includeDeletedSuppliers: includeDeletedSuppliers,
                offset: 0, max: 50};

            if(sortParams != null){globalSortParams = sortParams}
            $.extend(params, globalSortParams);
            if (globalSortParams != null && 'offset' in globalSortParams) {
                globalSortParams.offset = 0;
            }

            $.ajax({
                url: getSuppliersUrl,
                data: params,
                success: function (resp) {
                    $('#results-container').html(resp);
                }
            })
        }

        function resetForm() {
            document.getElementById('supplierNameTerm').value = null;
            document.getElementById('supplierReferenceTerm').value = null;
            document.getElementById('customerReferenceTerm').value = null;
            document.getElementById('includeDeletedSuppliers').checked = false;
        }

        //Popup supplier adding window
        function showAddSupplierModal() {
            $("#addSupplierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addSupplierModal').modal({show: true});
            $.ajax({
                url: addSupplierUrl,
                method: "GET",
                success: function (resp) {
                    $("#addSupplierContent").html(resp);
                }
            });
        }

        //Edit existing supplier
        function editSupplier(supplierId) {
            $("#addSupplierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addSupplierModal').modal({show: true});
            $.ajax({
                url: editSupplierUrl,
                method: "GET",
                data: {supplierId: supplierId},
                success: function (resp) {
                    $("#addSupplierContent").html(resp);
                }
            });
        }

        function toggleSupplierDeleted(supplierId, currentlyDeleted, sortParams) {
            let confirmationMessage = ""

            if (currentlyDeleted) {
                confirmationMessage = "Are you sure you want to reinstate the supplier?"
            } else {
                confirmationMessage = "Are you sure you want to delete the supplier?"
            }

            if (confirm(confirmationMessage)) {
                $("#addSupplierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $.ajax({
                    url: toggleSupplierDeletedUrl,
                    method: "GET",
                    data: {supplierId: supplierId},
                    success: function (resp) {
                        if (resp === "OK") {
                            $('#addSupplierModal').modal('hide')
                            searchSupplier(sortParams);
                        } else {
                            $('#addSupplierModal').modal({show: true});
                            $("#addSupplierContent").html(resp);
                        }
                    }
                });
            }
        }

        //Save newly added supplier
        function saveSupplier() {
            var formValues = $("#addSupplierForm").serialize();
            $("#addSupplierContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>");
            $.ajax({
                url: saveSupplierUrl,
                method: "POST",
                data: formValues,
                success: function (resp) {
                    if (resp === "OK") {
                        $('#addSupplierModal').modal('hide')
                        searchSupplier();
                    } else {
                        $("#addSupplierContent").html(resp);
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
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Supplier Maintenance</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="suppliers-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="suppliers-page-title" class="mx-auto my-auto">Supplier Maintenance</h2>
        </div>

        <div class="col-2 text-right ">
            <a id="add-new-supplier-btn" href="#" class="btn btn-wl" onclick="showAddSupplierModal();">Add New Supplier</a>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-12">
            <div class="card bg-light border-wl">
                <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                    <div class="row">
                        <div id="filters-header" class="col-10">Filters</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <div class="card-body collapse show" id="filterCollapse">
                    <div class="form-group row">

                        <label for="supplierNameTerm" class="col-2 col-form-label-sm text-right">Supplier Name</label>
                        <div class="col-4 input-group">
                            <g:textField id="supplierNameTerm" name="supplierNameTerm" maxlength="100" value="${session.SUPPLIER_NAME_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                        <label for="supplierReferenceTerm" class="col-2 col-form-label-sm text-right">Supplier Reference</label>
                        <div class="col-4 input-group">
                            <g:textField id="supplierReferenceTerm" name="supplierReferenceTerm" maxlength="100" value="${session.SUPPLIER_REFERENCE_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                    </div>
                    <div class="form-group row">
                        <label for="customerReferenceTerm" class="col-2 col-form-label-sm text-right">Customer Reference</label>
                        <div class="col-4 input-group">
                            <g:textField id="customerReferenceTerm" name="customerReferenceTerm" maxlength="100" value="${session.SUPPLIER_CUSTOMER_REFERENCE_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>

                        <label for="includeDeletedSuppliers" class="col-2 col-form-label-sm text-right">Include Deleted Suppliers</label>
                        <div class="col-4 input-group-append">
                            <g:checkBox id="includeDeletedSuppliers" name="includeDeletedSuppliers" checked="${session.INCLUDE_DELETED_SUPPLIERS}" class="col-1 form-check-input wl-checkbox ml-0" />
                        </div>
                    </div>
                    <div class="form-group row ">
                        <div class="col-12 text-right ">
                            <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchSupplier()">Search</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="supplierSearchResults"/>
    </div>
</section>

<section id="addSupplier-modal" class="container-fluid">
    <!-- Add supplier modal -->
    <div class="modal fade" id="addSupplierModal" tabindex="-1" role="dialog" aria-labelledby="addSupplierModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div id="addSupplierContent" class="modal-content"></div>
        </div>
    </div>
</section>
</body>
</html>