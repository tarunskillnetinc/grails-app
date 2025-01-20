<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Till Assignment</title>

    <asset:javascript src="co-utils.js" />
    <asset:javascript src="validators/input-validator.js" />

    <script type="text/javascript">

        var getPartnerCategoriesUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxGetPartnerCategories')}"
        var addPartnerCategoryUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxAddPartnerCategory')}"


        var deleteTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxDeleteTill')}"

        var editTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxEditTill')}"
        var unassignSerialUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxUnassignSerial')}"
        var saveTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxSaveTill')}"
        var generatePinUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxGeneratePin')}"
        var cancelTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxCancelTill')}"
        var advancedConfigurationUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxAdvancedConfiguration')}"
        var saveAdvancedConfigurationUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxSaveAdvancedConfiguration')}"

        $(function() {
            getPartnerCategories();
            applyListeners();
        });

        function getPartnerCategories() {
            tillId = $("#tillId").val();

            $.ajax({
                url: getPartnerCategoriesUrl(),
                method: "POST",
                data: { tillId: tillId },
                success: function(resp) {
                    $("#results-container").html(resp);
                },
                error: function() {
                    $("#search-results").show();
                    const result = document.createElement('div');
                    $(result).addClass('col pt-2 pb-2 text-center my-auto wl-striped0').html('No shifts found.');
                    $("#search-results").html(result);
                },
            });
        }

        function loadPartnerCategories() {
            $('#results-container').html("");
            $("#loading-indicator").show();

            var filterParams = {};

            $("#filtersForm input").each(function() {
                filterParams[$(this).attr("name")] = $(this).val();
            }).get();

            $("#filtersForm select").each(function() {
                filterParams[$(this).attr("name")] = $(this).find(":selected").val();
            }).get();

            $.ajax({
                url: ajaxGetPartnerCategories,
                data: filterParams,
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function filter(inputName, dropDownName) {
            var keyword = document.getElementById(inputName).value.toLowerCase();
            var select = document.getElementById(dropDownName);
            for (var i = 0; i < select.length; i++) {
                var txt = select.options[i].text.toLowerCase();
                if (!txt.match(keyword)) {
                    $(select.options[i]).attr('disabled', 'disabled').hide();
                } else {
                    $(select.options[i]).removeAttr('disabled').show();
                }
            }
        }

        function addPartnerCategory() {
            $("#addTillContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addTillModal').modal({show: true, backdrop: 'static', keyboard: false});
            $.ajax({
                url: addPartnerCategoryUrl,
                method: "GET",
                success: function (resp) {
                    $("#addTillContent").html(resp);
                    applyListeners();
                }
            });
        }

        function applyListeners() {
            intListener("tillIdFilter", 10, 2147483647);
            intListener("tillId", 10, 2147483647);
        }

        function showBtns() {
            $('.modal-footer').show()
        }

        function hideBtns() {
            $('.modal-footer').hide()
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
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Partner Category Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="tillAssignment" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">Partner Category Management</h2>
        </div>

        <div class="col-3 text-right">
            <g:link elementId="count-safe-button" type="button" class="btn btn-wl p-2"
                    action="addPartnerCategory" params="[isNew: true]">Add Partner Category</g:link>
        </div>
    </div>
</section>

<section id="filters-section" class="container-fluid">
    <div class="row mt-3">
        <div class="col-6">
            <div id="filters" class="card bg-light border-wl">
                <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                    <div class="row">
                        <div class="col-10">Filters</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>
                <div class="card-body collapse" id="filterCollapse">
                    <g:form name="filtersForm" id="filtersForm">
                        <div class="form-group row">
                            <label for="partnerNameFilter" class="col-2 col-form-label-sm text-right">Partner Name</label>
                            <div class="col-3">
                                <g:select name="partnerNameFilter" from="${ecomSupplierList}"
                                          class="form-control select-border"></g:select>
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="partnerCategoryFilter" class="col-2 col-form-label-sm text-right">Partner Category</label>
                            <div class="col-4">
                                <g:textField name="partnerCategoryFilter" class="form-control bottom-border" value="${serialNumber}" autocomplete="off"/>
                            </div>

                            <div class="col-6 text-right">
                                <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getPartnerCategories();">Filter</button>
                            </div>
                        </div>
                    </g:form>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="partnerCategoryResults"/>
    </div>

</section>


<section id="addTill-modal" class="container-fluid">
    <!-- Add Till modal -->
    <div class="modal fade" id="addTillModal" tabindex="-1" role="dialog" aria-labelledby="addTillModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div id="addTillContent" class="modal-content"></div>
        </div>
    </div>
</section>

<section id="advancedConfiguration-modal" class="container-fluid">
    <!-- Advanced Configuration modal -->
    <div class="modal fade" id="advancedTillModal" tabindex="-1" role="dialog" aria-labelledby="advancedTillModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div id="advancedTillContent" class="modal-content"></div>
        </div>
    </div>
</section>
</body>
</html>