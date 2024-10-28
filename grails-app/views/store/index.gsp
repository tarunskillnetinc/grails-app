<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Store Management</title>

    <asset:javascript src="validators/input-validator.js" />

    <script type="text/javascript">
        var getStoresUrl = "${createLink(controller: 'store', action: 'ajaxGetStores')}"
        var deleteStoreUrl = "${createLink(controller: 'store', action: 'ajaxDeleteStore')}"

        var globalSortParams = null;

        $(function() {
            var sort = "${sort}";
            var order = "${order}"

            getStores({max: ${max ?: 'null'}, offset: ${offset ?: 'null'}, sort: (sort !== "" ? sort : null), order: (order !== "" ? order : null)});
        });

        function getStores(sortParams) {
            $('#search-results').html("");
            $("#loading-indicator").show();

            var filterParams = {};

            $("#filtersForm input").each(function() {
                filterParams[$(this).attr("name")] = $(this).val();
            }).get();

            $("#filtersForm :checkbox:checked").each(function() {
                filterParams[$(this).attr("name")] = true;
            }).get();

            globalSortParams = sortParams;

            $.extend(filterParams, globalSortParams);

            $.ajax({
                url: getStoresUrl,
                data: filterParams,
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function deleteStore(storeId, storeNumber, deleted) {
            if (confirm("This will " +(deleted === true ? "delete" : "reinstate") +" store " + storeNumber +".")) {
                $.ajax({
                    url: deleteStoreUrl,
                    method: "DELETE",
                    data: { storeId: storeId, deleted: deleted },
                    success: function (data, textStatus, resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        getStores();
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        getStores();
                    }
                });
            }
        }

        function clearFilters() {
            $("#filtersForm input").each(function() {
                $(this).val("");
            }).get();

            $("#filtersForm :checkbox:checked").each(function() {
                $(this).prop("checked", false);
            }).get();

            getStores();
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Store Management</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="store-management" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Store Management</h2>
            </div>

            <div class="col-3 text-right">
                <button id="addStoreButton" type="button" class="btn btn-wl mt-1" onclick="window.location.href='/store/add/addStoreButton'">Add Store</button>
            </div>
        </div>
    </section>

    <section id="errors-container" class="container-fluid"></section>

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
                                <label for="storeNumberFilter" class="col-2 col-form-label-sm text-right">Store Number</label>
                                <div class="col-4">
                                        <g:field id="storeNumberFilter" type="number" min="0" max="2147483647" name="storeNumberFilter" value="${storeNumberFilter}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                                </div>

                                <label for="storeNameFilter" class="col-2 col-form-label-sm text-right">Store Name</label>
                                <div class="col-4">
                                    <g:textField id="storeNameFilter" name="storeNameFilter" value="${storeNameFilter}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row mb-0 mt-4">
                                <div class="col-4 offset-2">
                                    <g:checkBox id="showDeletedFilter" name="showDeletedFilter" class="form-check-input wl-checkbox ml-0 pointer" checked="${showDeletedFilter}" />
                                    <label for="showDeletedFilter" class="col-form-label-sm wl-label-right pointer">Show deleted stores</label>
                                </div>

                                <div class="col-6 text-right">
                                    <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getStores();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="tills-container" class="container-fluid mb-3">
        <div id="results-container">
            <g:render template="storeSearchResults" />
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