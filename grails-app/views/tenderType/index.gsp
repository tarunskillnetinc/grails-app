<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Tender Type Maintenance</title>

    <asset:javascript src="validators/input-validator.js" />

    <script type="text/javascript">
        var getTenderTypesUrl = "${createLink(controller: 'tenderType', action: 'ajaxSearch')}"
        var addTenderTypeUrl = "${createLink(controller: 'tenderType', action: 'ajaxAddTenderType')}"
        var editTenderTypeUrl = "${createLink(controller: 'tenderType', action: 'ajaxEditTenderType')}"
        var saveTenderTypeUrl = "${createLink(controller: 'tenderType', action: 'ajaxSaveTenderType')}"
        var deleteTenderTypeUrl = "${createLink(controller: 'tenderType', action: 'ajaxDeleteTenderType')}"

        var globalSortParams = null;

        $(function() {
            var sort = "${sort}";
            var order = "${order}"

            getTenderTypes({max: ${max ?: 'null'}, offset: ${offset ?: 'null'}, sort: (sort !== "" ? sort : null), order: (order !== "" ? order : null)});
        });

        function getTenderTypes(sortParams) {
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
                url: getTenderTypesUrl,
                data: filterParams,
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function deleteTenderType(tenderTypeId, tenderType, deleted) {
            if (confirm("This will " +(deleted === true ? "delete " : "reinstate ") + tenderType +".")) {
                $.ajax({
                    url: deleteTenderTypeUrl,
                    method: "DELETE",
                    data: { id: tenderTypeId, deleted: deleted },
                    success: function (data, textStatus, resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        getTenderTypes();
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        getTenderTypes();
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

            getTenderTypes();
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

        function addTenderType() {
            showAddTenderTypeModal(addTenderTypeUrl, null);
        }

        function editTenderType(id) {
            showAddTenderTypeModal(editTenderTypeUrl, {id: id});
        }

        function showAddTenderTypeModal(url, data) {
            $("#addTenderTypeModalContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addTenderTypeModal').modal({ show: true });

            $.ajax({
                url: url,
                method: "GET",
                data: data,
                success: function(resp) {
                    $("#addTenderTypeModalContent").html(resp);
                }
            });
        }

        function saveTenderType() {
            var formValues = $("#edit-tender-type-form").serialize();

            $.ajax({
                url: saveTenderTypeUrl,
                method: "POST",
                data: formValues,
                success: function(resp) {
                    $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp + '</div>');
                    getTenderTypes();
                    closeAddTenderTypeModal();
                },
                error: function (resp) {
                    $("#addTenderTypeModalContent").html(resp.responseText);
                }
            });
        }

        function closeAddTenderTypeModal() {
            $('#addTenderTypeModal').modal('hide');
        }

        function cashTenderChanged(checkbox) {
            if (checkbox.checked === true) {
                $("#cardPayment").prop("checked", false);
                $("#cardPayment").attr("disabled", true);
                $("#voucherType").val("");
                $("#voucherType").attr("disabled", true);
            } else {
                $("#cardPayment").attr("disabled", false);
                $("#voucherType").attr("disabled", false);
            }
        }

        function cardPaymentChanged(checkbox) {
            if (checkbox.checked === true) {
                $("#cashTender").prop("checked", false);
                $("#cashTender").attr("disabled", true);
                $("#voucherType").val("");
                $("#voucherType").attr("disabled", true);
            } else {
                $("#cashTender").attr("disabled", false);
                $("#voucherType").attr("disabled", false);
            }
        }

        function voucherTypeChanged(selectBox) {
            if (selectBox.value === "") {
                $("#cashTender").attr("disabled", false);
                $("#cardPayment").attr("disabled", false);
            } else {
                $("#cashTender").prop("checked", false);
                $("#cashTender").attr("disabled", true);
                $("#cardPayment").prop("checked", false);
                $("#cardPayment").attr("disabled", true);
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Tender Type Maintenance</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="tender-type-management" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Tender Type Maintenance</h2>
            </div>

            <div class="col-3 text-right">
                <button id="addTenderTypeButton" type="button" class="btn btn-wl mt-1" onclick="addTenderType();">Add Tender Type</button>
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
                                <label for="tenderTypeFilter" class="col-2 col-form-label-sm text-right">Tender Type</label>
                                <div class="col-4">
                                    <g:textField id="tenderTypeFilter" name="tenderTypeFilter" value="${tenderTypeFilter}" class="form-control bottom-border" />
                                </div>

                                <div class="col-4 offset-2">
                                    <g:checkBox id="showDeletedFilter" name="showDeletedFilter" class="form-check-input wl-checkbox ml-0 pointer" checked="${showDeletedFilter}" />
                                    <label for="showDeletedFilter" class="col-form-label-sm wl-label-right pointer">Show deleted tender types</label>
                                </div>
                            </div>

                            <div class="form-group row mb-0 mt-4">
                                <div class="col-12 text-right">
                                    <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getTenderTypes();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="tender-types-container" class="container-fluid mb-3">
        <div id="results-container">
            <g:render template="tenderTypeSearchResults" />
        </div>
    </section>

    <section id="addTenderType-modal" class="container-fluid">
        <!-- Add tender type modal -->
        <div class="modal fade" id="addTenderTypeModal" tabindex="-1" role="dialog" aria-labelledby="addTenderTypeModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div id="addTenderTypeModalContent" class="modal-content">

                </div>


            </div>
        </div>
    </section>
</body>
</html>