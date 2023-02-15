<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Promotion Maintenance</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type="text/javascript">
            $(document).ready(function () {
                $('#promotionSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        searchButtonClicked();
                    }
                });

                $('#validDateFilter').on("change", function () {
                    $('#validDateFilter').val(this.value);
                    $('#validDateFilter').removeClass('is-invalid');
                });

                var existingSearchTerm = $('#promotionSearchTerm').val();
                if (existingSearchTerm != null && existingSearchTerm !== "") {
                    searchButtonClicked();
                }
            });

            function searchButtonClicked(sortParams) {
                search(sortParams);
            }

            function search(sortParams) {
                $("#search-results").hide();
                $("#loading-indicator").show();

                var URL = "${createLink(controller: 'promotion', action: 'promotionSearch')}";

                let searchTerm = $('#promotionSearchTerm').val();
                let searchBy = $('#promotionSearchBy').val();

                let validDate = $('#validDateFilter').val();
                let updatedSince = $('#updatedDateFilter').val();
                let type = $('#typeFilter').val();
                let supplier = $('#supplierFilter').val();
                let status = $('#statusFilter').val();

                $.ajax({
                    url: URL,
                    data: {
                        searchTerm: searchTerm,
                        searchBy: searchBy,
                        validDate: validDate,
                        updatedSince: updatedSince,
                        type: type,
                        supplier: supplier,
                        status: status,
                        max: sortParams ? sortParams["max"] : null,
                        offset: sortParams ? sortParams.offset : null,
                        sortColumn: sortParams ? sortParams.sortColumn : null,
                        sortOrder: sortParams ? sortParams.sortOrder : null
                    },
                    success: function(resp) {
                        $('#search-results-container').html(resp);

                        $('#promotionSearchTerm').data('prev',$('#promotionSearchTerm').val());
                        $('#promotionSearchBy').data('prev', $('#promotionSearchBy').val());
                    }
                })
            }

            $(function() {
                $('#validDateFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#updatedDateFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });
            });

            function resetForm() {
                document.getElementById('validDateFilter').value = null;
                document.getElementById('updatedDateFilter').value = null;
                document.getElementById('typeFilter').value = null;
                document.getElementById('supplierFilter').value = null;
                document.getElementById('statusFilter').value = null;
                document.getElementById('promotionSearchTerm').value = null;
                document.getElementById('promotionSearchBy').value = 'description';

                search();
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
                            <li class="breadcrumb-item active" aria-current="page">Promotion Search</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="alerts-container" class="container-fluid">
            <g:if test="${flash.message}">
                <div id="alerts-container-message" class="alert alert-success" role="alert">${flash.message}</div>
            </g:if>
        </section>

        <section id="promo-maintenance-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="promo-maintenance-search-title" class="mx-auto my-auto">Promotion Search</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="add-new-promotion" controller="promotion" action="add" class="btn btn-wl">Add New Promotion</g:link>
                </div>
            </div>

            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label for="promotionSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                <div class="col-10 input-group">
                                    <g:textField id="promotionSearchTerm" name="promotionSearchTerm" maxlength="100" value="${session.PROMOTION_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />

                                    <div class="input-group-append">
                                        <g:select id="promotionSearchBy" name="productSearchBy" from="${['description', 'promotionId']}" value="everything" valueMessagePrefix="PromotionSearchBy" class="form-control select-border" style="z-index: 0;" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="validDate" class="col-2 col-form-label-sm text-right">Date Valid</label>
                                <div class="col-4">
                                    <g:textField name="validDate" onkeydown="return false" id="validDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>

                                <label for="updatedDate" class="col-2 col-form-label-sm text-right">Updated Since</label>
                                <div class="col-4">
                                    <g:textField name="updatedDate" onkeydown="return false" id="updatedDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="types" class="col-2 col-form-label-sm text-right">Type</label>
                                <div class="col-4">
                                    <g:select name="types" id="typeFilter" placeholder="Please Select" from="${types}" optionValue="friendlyName" noSelection="['': '']" class="form-control select-border"/>
                                </div>

                                <label for="supplier" class="col-2 col-form-label-sm text-right">Supplier</label>
                                <div class="col-4">
                                    <g:select name="supplier" id="supplierFilter" from="${symbolGroups}" optionValue="name" optionKey="id" noSelection="['': '']" class="form-control select-border"/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="status" class="col-2 col-form-label-sm text-right">Status</label>
                                <div class="col-4">
                                    <g:select name="status" id="statusFilter" from="${['ACTIVE', 'INACTIVE']}" valueMessagePrefix="PromotionStatus" noSelection="['': '']" class="form-control select-border"/>
                                </div>

                                <div class="col-4 offset-2 text-right">
                                    <button type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="search-results-container" class="align-content-center">
                <g:render template="promotionSearchResults" />
            </div>
        </section>
    </body>
</html>