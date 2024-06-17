<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Barcode Configuration</title>
    <asset:javascript src="co-utils.js" />
    <asset:javascript src="validators/input-validator.js" />

    <style>
        .custom-checkbox-align .form-check-input {
            width: 1.5em;
            height: 1.5em;
            margin-left: 0;
        }
        .field-error {
            font-size: 0.7em; /* Adjust the size as needed */
        }
    </style>

    <script type="text/javascript">

    var getSignifiersURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxSearchForBarcodeSignifiers')}"
    var addSignifierURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxAddSignifier')}"
    var saveSignifierURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxSaveSignifier')}"
    var deleteSignifierURL = "${createLink(controller: 'barcodeConfig', action: 'ajaxDeleteSignifier')}"

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

    function addSignifier() {
        $("#addSignifierContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
        $('#addSignifierModal').modal({show: true, backdrop: 'static', keyboard: false});
        $.ajax({
            url: addSignifierURL,
            method: "GET",
            success: function (resp) {
                $("#addSignifierContent").html(resp);
            }
        });
    }

    function cancelSignifier() {
        if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
            $('#addSignifierModal').modal('hide')
        }
    }

    function saveSignifier() {
        var formValues = $("#addSignifierForm").serialize();
        $("#addSignifierContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>")
        hideBtns();
        $.ajax({
            url: saveSignifierURL,
            method: "POST",
            data: formValues,
            success: function (resp) {
                if (resp === "OK") {
                    $('#addSignifierModal').modal('hide')
                    getSignifiers()
                } else {
                    showBtns();
                    $("#addSignifierContent").html(resp);
                }
            }
        });
    }

    function deleteSignifier(signifierId) {
        if (confirm("This will delete the selected signifier.")) {
            $.ajax({
                url: deleteSignifierURL,
                method: "DELETE",
                data: {signifierId: signifierId},
                success: function (data, textStatus, resp) {
                    $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                    getSignifiers()
                },
                error: function (resp) {
                    $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                    getSignifiers()
                }
            });
        }
    }

    function hideBtns() {
        $('.modal-footer').hide()
    }

    function showBtns() {
        $('.modal-footer').show()
    }

    function clearFilters() {
        $("#typeFilter").val("");
        $("#descriptionFilter").val("");
        $("#patternFilter").val("");
        getSignifiers()
    }

    function loadEditSignifier(signifierId) {
        window.location.href = '<g:createLink controller="barcodeConfig" action="editBarcodeSignifier"/>?signifierId=' + signifierId;
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

<section id="barcodeConfiguration" class="container-fluid">
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
                                <g:select id="typeFilter" name="typeFilter" from="${signifierTypes}" valueMessagePrefix="BarcodeSignifierType"
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

<section id="addSignifier-modal" class="container-fluid">
    <!-- Add Signifier modal -->
    <div class="modal fade" id="addSignifierModal" tabindex="-1" role="dialog" aria-labelledby="addSignifierModalLabel"
         aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div id="addSignifierContent" class="modal-content"></div>
        </div>
    </div>
</section>
</body>
</html>