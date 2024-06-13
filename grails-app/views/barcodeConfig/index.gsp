<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Barcode Configuration</title>
    <asset:javascript src="co-utils.js" />
    <asset:javascript src="validators/input-validator.js" />

    <script type="text/javascript">

    var getSignifiersURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxSearchForBarcodeSignifiers')}"

    var globalSortParams = null;

    $(function() {
        var sort = "${sort}";
        var order = "${order}"
        var offset= "${offset}"

        getSignifiers({max: ${max ?: 'null'}, offset: ${offset ?: 'null'}, sort: (sort !== "" ? sort : null), order: (order !== "" ? order : null)});
    });

    function getSignifiers(sortParams) {
        $('#results-container').html("");
        $("#loading-indicator").show();

        var filterParams = {};

        $("#filtersForm input").each(function() {
            filterParams[$(this).attr("name")] = $(this).val();
        }).get();

        $("#filtersForm select").each(function() {
            filterParams[$(this).attr("name")] = $(this).find(":selected").val();
        }).get();

        globalSortParams = sortParams;

        $.extend(filterParams, globalSortParams);

        $.ajax({
            url: getSignifiersURL,
            data: filterParams,
            success: function(resp) {
                $('#results-container').html(resp);
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
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Barcode Configuration</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="tillAssignment" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">Barcode Configuration</h2>
        </div>

        <div class="col-3 text-right">
            <a id="addTill" href="#" class="btn btn-wl mt-1" onclick="addSignifier();">Add Signifier</a>
            <a id="refresh" href="#" class="btn btn-wl mt-1" onclick="getSignifiers();">Refresh</a>
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
                            <label for="typeFilter" class="col-2 col-form-label-sm text-right">Type</label>
                            <div class="col-3">
                                <g:select name="typeFilter" from="${signifierTypes}" valueMessagePrefix="BarcodeSignifierType"
                                          optionKey="${{it}}"
                                          noSelection="['': 'All']"
                                          class="form-control select-border"></g:select>
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="descriptionFilter" class="col-2 col-form-label-sm text-right">Description</label>
                            <div class="col-3">
                                <g:field id="descriptionFilter" type="text" name="descriptionFilter" value="${description}" class="form-control bottom-border" oninput="validateInput(this);" />
                            </div>
                            <label for="patternFilter" class="col-2 col-form-label-sm text-right">Barcode Pattern</label>
                            <div class="col-4">
                                <g:field id="patternFilter" type="text" name="patternFilter" value="${pattern}" class="form-control bottom-border" oninput="validateInput(this);" onkeydown="acceptNumeric(event);" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-6"></div>
                            <div class="col-6 text-right">
                                <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getSignifiers();">Filter</button>
                            </div>
                        </div>
                    </g:form>
                </div>
            </div>
        </div>
    </div>
</section>

<section id="signifiers-container" class="container-fluid mb-3">
    <div id="results-container">
        <g:render template="signifiersSearchResults"/>
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