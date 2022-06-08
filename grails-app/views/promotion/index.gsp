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

                $('#startDateFilter').on("change", function () {
                    $('#startDateFilter').val(this.value);
                    $('#startDateFilter').removeClass('is-invalid');
                    $('#endDateFilter').datepicker('setStartDate', this.value);
                });

                $('#endDateFilter').on("change", function () {
                    $('#endDateFilter').val(this.value);
                    $('#endDateFilter').removeClass('is-invalid');
                    $('#startDateFilter').datepicker('setEndDate', this.value);
                });

                var existingSearchTerm = $('#promotionSearchTerm').val();
                if (existingSearchTerm != null && existingSearchTerm !== "") {
                    searchButtonClicked();
                }
            });

            function searchButtonClicked() {
                $('#offset').val(0);
                search();
            }

            function search() {
                var URL = "${createLink(controller: 'promotion', action: 'promotionSearch')}";

                var searchTerm = $('#promotionSearchTerm').val();
                var searchBy = $('#promotionSearchBy').val();

                let startDate = $('#startDateFilter').val();
                let endDate = $('#endDateFilter').val();
                let updatedSince = $('#updatedDateFilter').val();
                let type = $('#typeFilter').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center pt-2\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: {
                        searchTerm: searchTerm,
                        searchBy: searchBy,
                        startDate: startDate,
                        endDate: endDate,
                        updatedSince: updatedSince,
                        type: type
                    },
                    success: function(resp) {
                        $('#search-results').html(resp);
                        $('#promotionSearchTerm').data('prev',$('#promotionSearchTerm').val());
                        $('#promotionSearchBy').data('prev', $('#promotionSearchBy').val());
                    }
                })
            }

            $(function() {
                $('#startDateFilter').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#endDateFilter').datepicker({
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

            function searchButtonClicked2() {
                $('#offset').val(0);
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
                <div class="alert alert-success" role="alert">${flash.message}</div>
            </g:if>
        </section>

        <section id="promo-maintenance-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Promotion Search</h2>
            </div>

            <div class="row mt-4">
                <div class="col-5">
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
                                <label for="startDate" class="col-2 col-form-label-sm text-right">Start Date</label>
                                <div class="col-4">
                                    <g:textField name="startDate" onkeydown="return false" id="startDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>

                                <label for="endDate" class="col-2 col-form-label-sm text-right">End Date</label>
                                <div class="col-4">
                                    <g:textField name="endDate" onkeydown="return false" id="endDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="updatedDate" class="col-2 col-form-label-sm text-right">Updated Since</label>
                                <div class="col-4">
                                    <g:textField name="updatedDate" onkeydown="return false" id="updatedDateFilter" class="form-control bottom-border" autocomplete="off"/>
                                </div>

                                <label for="types" class="col-2 col-form-label-sm text-right">Type</label>
                                <div class="col-4">
                                    <g:select name="types" id="typeFilter" placeholder="Please Select" from="${types}" optionValue="friendlyName" noSelection="['': '']" class="form-control select-border"/>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="align-content-end">
                                    <div class="input-group">
                                        <g:textField name="promotionSearchTerm" maxlength="100"
                                                     class="form-control"
                                                     placeholder="Enter a search term."
                                                     aria-describedby="select-addon2"
                                                     value="${session.PROMOTION_SEARCH_TERM}"/>
                                        <div class="input-group-append">
                                            <g:select id="promotionSearchBy" name="productSearchBy"
                                                      from="${['description', 'promotionId']}"
                                                      value="everything"
                                                      valueMessagePrefix="PromotionSearchBy"
                                                      class="form-control select-border" style="z-index: 0;"/>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group col-4 offset-8 text-right">
                                <button id="filter-submit-button" type="button"
                                        class="btn btn-wl text-right"
                                        onclick="searchButtonClicked2()">Search</button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-2 offset-5 text-right">
                    <g:link controller="promotion" action="add" class="btn btn-wl">Add New Promotion</g:link>
                </div>
            </div>

            <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                <div class="col-1 font-weight-bold">Promotion ID</div>
                <div class="col-3 font-weight-bold">Description</div>
                <div class="col-1 font-weight-bold">Last Updated</div>
                <div class="col-1 font-weight-bold">Start Date</div>
                <div class="col-1 font-weight-bold">End Date</div>
                <div class="col-1 font-weight-bold">Active</div>
                <div class="col-2 font-weight-bold">Type</div>
                <div class="col-2 font-weight-bold">Discount Amount</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="promotionSearchResults" />
            </div>
        </section>
    </body>
</html>