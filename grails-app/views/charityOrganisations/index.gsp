<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Charity Organisations</title>

    <script type='text/javascript'>
        var globalSortParams = null;
        var getCharityUrl = "${createLink(controller: 'charityOrganisations', action: 'ajaxGetSearchCharity')}";
        var addCharityUrl = "${createLink(controller: 'charityOrganisations', action: 'ajaxAddCharity')}";
        var editCharityUrl = "${createLink(controller: 'charityOrganisations', action: 'ajaxEditCharity')}";
        var saveCharityUrl = "${createLink(controller: 'charityOrganisations', action: 'ajaxSaveCharity')}";
        var toggleCharityDeletedUrl = "${createLink(controller: 'charityOrganisations', action: 'ajaxToggleSupplierDeletedFlag')}";

        $(function () {searchCharity();}); //As soon as page open call this method

        //This will call supplier search method in supplier controller
        function searchCharity(sortParams) {
            var organisationTypeTerm = $('#organisationTypeTerm').val();
            var charityMemberNumberTerm = $('#charityMemberNumberTerm').val();
            var charityGroupDescriptionTerm = $('#charityGroupDescriptionTerm').val();
            var includeDeletedCharitiesTerm = $('#includeDeletedCharities').prop('checked');

            $('#results-container').html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");

            var params = {organisationTypeTerm: organisationTypeTerm,
                charityMemberNumberTerm: charityMemberNumberTerm, charityGroupDescriptionTerm: charityGroupDescriptionTerm, includeDeletedCharitiesTerm: includeDeletedCharitiesTerm,
                offset: 0, max: 50};

            if(sortParams != null){globalSortParams = sortParams}
            $.extend(params, globalSortParams);
            if (globalSortParams != null && 'offset' in globalSortParams) {
                globalSortParams.offset = 0;
            }

            $.ajax({
                url: getCharityUrl,
                data: params,
                success: function (resp) {
                    $('#results-container').html(resp);
                }
            })
        }

        function resetForm() {
            document.getElementById('organisationTypeTerm').value = null;
            document.getElementById('charityMemberNumberTerm').value = null;
            document.getElementById('charityGroupDescriptionTerm').value = null;
            document.getElementById('includeDeletedCharities').checked = false;
        }

        //Popup supplier adding window
        function showAddCharityModal() {
            $("#addCharityContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addCharityModal').modal({show: true});
            $.ajax({
                url: addCharityUrl,
                method: "GET",
                success: function (resp) {
                    $("#addCharityContent").html(resp);
                }
            });
        }

        //Edit existing charity
        function editCharity(charityId) {
            $("#addCharityContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addCharityModal').modal({show: true});
            $.ajax({
                url: editCharityUrl,
                method: "GET",
                data: {charityId: charityId},
                success: function (resp) {
                    $("#addCharityContent").html(resp);
                }
            });
        }

        //Save newly added supplier
        function saveCharity() {
            var formValues = $("#addCharityForm").serialize();
            $("#addCharityContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>");
            $.ajax({
                url: saveCharityUrl,
                method: "POST",
                data: formValues,
                success: function (resp) {
                    if (resp === "OK") {
                        $('#addCharityModal').modal('hide')
                        searchCharity();
                    } else {
                        $("#addCharityContent").html(resp);
                    }
                }
            });
        }

/*        //Popup supplier adding window
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
        }*/
    </script>
</head>

<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Charity Organisations</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="suppliers-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="charity-organisations-page-title" class="mx-auto my-auto">Charity Organisations</h2>
        </div>

        <div class="col-2 text-right ">
            <a id="add-new-charity-btn" href="#" class="btn btn-wl" onclick="showAddCharityModal();">Add Charity/Group</a>
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
                        <label for="organisationTypeTerm" class="col-2 col-form-label-sm text-right">Organisation Type:</label>
                        <div class="col-4 input-group">
                            <g:textField id="organisationTypeTerm" name="organisationTypeTerm" maxlength="100" value="${session.CHARITY_TYPE_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                        <label for="charityMemberNumberTerm" class="col-2 col-form-label-sm text-right">Charity Member Number:</label>
                        <div class="col-4 input-group">
                            <g:textField id="charityMemberNumberTerm" name="charityMemberNumberTerm" maxlength="100" value="${session.CHARITY_MEMBER_NUMBER_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                    </div>

                    <div class="form-group row">
                        <label for="charityGroupDescriptionTerm" class="col-2 col-form-label-sm text-right">Charity / Group Description</label>
                        <div class="col-4 input-group">
                            <g:textField id="charityGroupDescriptionTerm" name="charityGroupDescriptionTerm" maxlength="100" value="${session.CHARITY_DESCRIPTION_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                        </div>

                        <label for="includeDeletedCharities" class="col-2 col-form-label-sm text-right">Include Deleted Charity / Group</label>
                        <div class="col-4 input-group-append">
                            <g:checkBox id="includeDeletedCharities" name="includeDeletedCharities" checked="${session.INCLUDE_DELETED_CHARITIES}" class="col-1 form-check-input wl-checkbox ml-0" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="charitySearchResults"/>
    </div>

</section>

<section id="addCharity-modal" class="container-fluid">
    <!-- Add charity modal -->
    <div class="modal fade" id="addCharityModal" tabindex="-1" role="dialog" aria-labelledby="addCharityModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div id="addCharityContent" class="modal-content"></div>
        </div>
    </div>
</section>

</body>
</html>