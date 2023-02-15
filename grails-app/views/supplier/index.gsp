<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Suppliers & Affiliations</title>

    <script type='text/javascript'>
        var globalSortParams = null;
        var getSuppliersUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSearchSupplier')}";
        var addSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxAddSupplier')}";
        var editSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxEditSupplier')}";
        var saveSupplierUrl = "${createLink(controller: 'supplier', action: 'ajaxSaveSupplier')}";

        $(function () {searchSupplier();}); //As soon as page open call this method

        //This will call supplier search method in supplier controller
        function searchSupplier(sortParams) {
            var searchTerm = $('#supplierSearchTerm').val();
            var searchBy = $('#supplierSearchBy').val();
            $('#results-container').html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");
            var params = {searchTerm: searchTerm, searchBy: searchBy, offset: 0, max: 50};
            if(sortParams != null){globalSortParams = sortParams}
            $.extend(params, globalSortParams);
            $.ajax({
                url: getSuppliersUrl,
                data: params,
                success: function (resp) {
                    $('#results-container').html(resp);
                    $('#supplierSearchTerm').data('prev', $('#supplierSearchTerm').val())
                    $('#supplierSearchBy').data('prev', $('#supplierSearchBy').val())
                }
            })
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
                    <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li class="breadcrumb-item active" aria-current="page">Suppliers</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="suppliers-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="suppliers-page-title" class="mx-auto my-auto">Suppliers</h2>
        </div>

        <div class="col-2 text-right ">
            <a id="add-new-supplier-btn" href="#" class="btn btn-wl" onclick="showAddSupplierModal();">Add New Supplier</a>
        </div>
    </div>

    <div class="row mt-4 ml-0 mr-0">
        <div class="input-group offset-2 col-8">
            <g:textField id="supplierSearchTerm" name="supplierSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2"/>
            <div class="input-group-append">
                <g:select id="supplierSearchBy" name="supplierSearchBy" from="${['Name']}" value="Name" valueMessagePrefix="SupplierSearchBy" class="form-control select-border" style="z-index: 0;"/>
                <asset:image src="search.png" id="userSearchButton" name="userSearchButton" onclick="searchSupplier()" class="wl-search-button"/>
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