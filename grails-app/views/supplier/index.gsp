<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Supplier Maintenance</title>
    <asset:javascript src="jquery-ui.js"/>
    <asset:stylesheet src="jquery-ui.css"/>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="co-utils.js"/>
    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="money-mask.js"/>

    <style>
    @media (min-width: 992px) {
        .modal-xxl {
            max-width: 800px;
        }
    }

    @media (min-width: 1200px) {
        .modal-xxl {
            max-width: 1140px;
        }
    }

    @media (min-width: 1400px) {
        .modal-xxl {
            max-width: 1340px;
        }
    }
    </style>

    <script type='text/javascript'>
        var globalSortParams = null;
        var getSuppliersUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSearchSupplier')}";
        var addSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxAddSupplier')}";
        var editSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxEditSupplier')}";
        var saveSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxSaveSupplier')}";
        var setSupplierDeletedFlagUrl = "${createLink(controller: 'supplier', action: 'ajaxSetSupplierDeletedFlag')}";

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
            let supplierDeletedFlag

            if (currentlyDeleted) {
                confirmationMessage = "Are you sure you want to reinstate the supplier?"
                supplierDeletedFlag = false
            } else {
                confirmationMessage = "Are you sure you want to delete the supplier?"
                supplierDeletedFlag = true
            }

            if (confirm(confirmationMessage)) {
                $("#addSupplierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                $.ajax({
                    url: setSupplierDeletedFlagUrl,
                    method: "GET",
                    data: {supplierId: supplierId, supplierDeletedFlag: supplierDeletedFlag},
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

        function validatePhoneNumber(number) {
            const regex = /^(\d{4,12})$/;

            if (regex.test(number)) {
                return true;
            } else {
                return false;
            }
        }

        function validateEmail(email) {
            const regex = /^(?!.*\.\.)(?!.*\.$)(?!^\.)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\.[a-zA-Z]{2,}$/;

            if (regex.test(email)) {
                return true;
            } else {
                return false;
            }
        }

        function validateCaseRate(caseRate) {
            return !isNaN(caseRate) && !isNaN(parseFloat(caseRate));
        }

        function validateUpdates() {
            let error = false;
            let errorString = "";

            if ($('#suppliername').val() === "") {
                error = true;
                errorString = errorString.concat("\n<li>The Supplier Name cannot be blank.</li>");
            }

            if ($('#supplierreference').val() === "") {
                error = true;
                errorString = errorString.concat("\n<li>The Supplier Reference cannot be blank.</li>");
            }

            let email = $('#email').val();
            if (email !== "" && !validateEmail(email)) {
                error = true;
                errorString = errorString.concat("\n<li>The email address must be valid.</li>");
            }

            let telephone = $('#phoneNumber').val();
            if (telephone !== "") {
                if (!validatePhoneNumber(telephone)) {
                    error = true;
                    errorString = errorString.concat("\n<li>The telephone number must be valid.</li>");
                }
            }

            let caserate = $('#caserate').val().replaceAll(",", "");

            if (caserate !== "0.00" && validateCaseRate(caserate)) {
                if ($('#caserateeffectivedate').val() === "") {
                    error = true;
                    errorString = errorString.concat("\n<li>The Case Rate Effective Date must be set if the case rate is not zero.</li>")
                }
            } else if (caserate !== "0.00") {
                error = true;
                errorString = errorString.concat("\n<li>The Case Rate must be a valid number or zero.</li>")
            } else { // case rate is exactly zero here.
                if ($('#caserateeffectivedate').val() !== "") {
                    error = true;
                    errorString = errorString.concat("\n<li>The Case Rate must not be zero if the effective date is set.</li>")
                }
            }

            if (!error) {
                $('#validation-errors').html("");
                $('#js-errors-container').prop("hidden", true);
            } else {
                $('#validation-errors').html("<ul>" + errorString + "\n</ul>");
                $('#js-errors-container').prop("hidden", false);
            }

            return !error;
        }

        //Save newly added supplier
        function saveSupplier() {
            if (!validateUpdates()) {
                return;
            }

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

        function formatDate(date, options, separator) {
            function format(option) {
                let formatter = new Intl.DateTimeFormat('en', option);
                return formatter.format(date);
            }

            return options.map(format).join(separator);
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
        <div class="col-7">
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
                        <label for="supplierReferenceTerm" class="col-2 col-form-label-sm text-right">Supplier Ref.</label>
                        <div class="col-4 input-group">
                            <g:textField id="supplierReferenceTerm" name="supplierReferenceTerm" maxlength="100" value="${session.SUPPLIER_REFERENCE_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                    </div>
                    <div class="form-group row">
                        <label for="customerReferenceTerm" class="col-2 col-form-label-sm text-right">Customer Ref.</label>
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
    <div class="modal-dialog modal-xxl" role="document">
            <div id="addSupplierContent" class="modal-content"></div>
        </div>
    </div>
</section>
</body>
</html>