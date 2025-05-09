<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Partner Category Management</title>

    <asset:javascript src="co-utils.js" />
    <asset:javascript src="validators/input-validator.js" />

    <script type="text/javascript">

        var getPartnerCategoriesUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxGetPartnerCategories')}"
        var addPartnerCategoryUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxAddPartnerCategory')}"

        function getPartnerCategories() {
            $('#results-container').html("");
            $('#messages-container').html("");
            $("#loading-indicator").show();

            var filterParams = {};

            $("#filtersForm input").each(function() {
                filterParams[$(this).attr("name")] = $(this).val();
            }).get();

            $("#filtersForm select").each(function() {
                filterParams[$(this).attr("name")] = $(this).find(":selected").val();
            }).get();

            $.ajax({
                url: getPartnerCategoriesUrl,
                data: filterParams,
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function clearFilters() {
            $("#partnerCategoryNameFilter").val("");
            $("#partnerSupplierIdFilter").val("");
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

</script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active"
                            aria-current="page">Partner Category Management</li>
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
                <g:link elementId="count-safe-button" type="button" class="btn btn-wl" action="addPartnerCategory" params="[isNew: true]">Add Partner Category</g:link>
            </div>
        </div>
    </section>

    <section id="filters-section" class="container-fluid">
        <div class="row mt-4">
            <div class="col-6">
                <div id="filters" class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="collapseExample">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm" >
                            <div class="form-group row">
                                <label for="partnerSupplierIdFilter" class="col-2 col-form-label-sm text-right">Partner Name</label>
                                <div class="col-4">
                                    <g:select name="partnerSupplierIdFilter"
                                              id="partnerSupplierIdFilter"
                                              from="${ecomSuppliers}"
                                              optionKey="id"
                                              optionValue="name"
                                              value="${session.PARTNER}"
                                              noSelection="['':'']"
                                              class="form-control select-border">

                                    </g:select>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="partnerCategoryNameFilter" class="col-2 col-form-label-sm text-right">Partner Category</label>
                                <div class="col-4">
                                    <g:textField name="partnerCategoryNameFilter"
                                                 id="partnerCategoryNameFilter"
                                                 class="form-control bottom-border"
                                                 value="${session.PARTNER_CATEGORY}"
                                                 autocomplete="off"/>
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
    </section>

    <section id="alerts-section" class="container-fluid">
        <div id="messages-container">
            <g:if test="${flash.message}"><div id="alerts-success-container-message" class="alert alert-success  mt-4" role="alert">${flash.message}</div></g:if>
            <g:if test="${flash.error}"><div id="alerts-success-container-message" class="alert alert-danger mt-4" role="alert">${flash.error}</div></g:if>
        </div>
    </section>

    <section id="results-section" class="container-fluid">
        <div id="results-container" class="align-content-center mt-5">
            <g:render template="partnerCategoryResults"/>
        </div>

    </section>

</body>
</html>