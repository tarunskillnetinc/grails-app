<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Order Amendments</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="moment-with-locales.min.js"/>

    <script type="application/javascript">
        $(document).ready(function() {
            search();
        });

        function saveColumns() {
            var url = "${createLink(controller: 'amendableOrder', action: 'ajaxSaveColumns')}";

            var checkboxValues = { };

            $("#reportColumnsForm input:checkbox").each(function() {
                checkboxValues[$(this).val()] = this.checked;
            }).get();

            $.ajax({
                url: url,
                method: "POST",
                data: { reportColumns: JSON.stringify(checkboxValues) },
                success: function(resp) {
                    $("#columnsCollapse").collapse('hide');
                    search();
                }
            });
        }

        function search() {
            var url = "${createLink(controller: 'amendableOrder', action: 'ajaxSearchOrders')}";

            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: url,
                data: { category: $('#categorySearchTerm').val(), storeId: $('#storeIdFilter').val() },
                success: function(resp) {
                    $('#results-container').html(resp);
                    $('#categorySearchTerm').data('prev',$('#categorySearchTerm').val())
                }
            });
        }

        function resetForm() {
            document.getElementById('categorySearchTerm').value = null;
            document.getElementById('storeIdFilter').value = '';
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Order Amendments</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

<section id="orderAmendment" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto">Order Amendments</h2>
        </div>
    </div>

    <g:if test="${flash.message}">
        <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div id="error-message" class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
    </g:if>

    <div class="row mt-4">
        <div class="col-6">
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
                        <label for="categorySearchTerm" class="col-2 col-form-label-sm text-right">Category</label>
                        <div class="col-10 input-group">
                            <g:textField id="categorySearchTerm" name="categorySearchTerm" maxlength="100" value="${category}" class="form-control" aria-describedby="select-addon2" />
                        </div>
                    </div>
                    <div class="form-group row">
                        <label for="storeIdFilter" class="col-2 col-form-label-sm text-right">Store</label>
                        <div class="col-3">
                            <g:select id="storeIdFilter"
                                      name="storeIdFilter" from="${stores}"
                                      optionValue="${{it.config.storeNumber}}"
                                      optionKey="${{it.id}}"
                                      noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                      class="form-control select-border"
                                      disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                        </div>
                    </div>

                    <div class="form-group row">
                        <div class="offset-8 text-right">
                            <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search()">Search</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-2 offset-4">
            <div class="card bg-light border-wl">
                <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                    <div class="row">
                        <div class="col-10">Columns</div>
                        <div class="col-2 text-right">
                            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                            </svg>
                        </div>
                    </div>
                </div>
                <div class="card-body collapse" id="columnsCollapse">
                    <g:form name="reportColumnsForm" id="reportColumnsForm">
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsCategory" class="form-check-input" value="category" checked="${!userColumns || userColumns?.columns?.find { it.column == 'category' }?.enabled}" />
                            <label class="form-check-label" for="columnsCategory">Category</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsStore" class="form-check-input" value="store" checked="${!userColumns || userColumns?.columns?.find { it.column == 'store' }?.enabled}" />
                            <label class="form-check-label" for="columnsStore">Store</label>
                        </div>
                        <div class="form-group form-check">
                            <g:checkBox name="columns" id="columnsAmendableDate" class="form-check-input" value="amendableDate" checked="${!userColumns || userColumns?.columns?.find { it.column == 'amendableDate' }?.enabled}" />
                            <label class="form-check-label" for="columnsAmendableDate">Amendable Date</label>
                        </div>

                        <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveColumns();">Apply</button>
                    </g:form>
                </div>
            </div>
        </div>
    </div>

    <div id="results-container" class="align-content-center">
        <g:render template="orderSearchResults" />
    </div>
</section>
</body>
</html>