<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Till Assignment</title>

    <script type="text/javascript">

        $(function() {
            getTills();
        });

        function getTills() {
            var filterParams = {};

            $("#filtersForm input").each(function() {
                filterParams[$(this).attr("name")] = $(this).val();
            }).get();

            $("#filtersForm select").each(function() {
                filterParams[$(this).attr("name")] = $(this).find(":selected").val();
            }).get();


            var url = "${createLink(controller: 'tillAssignment', action: 'ajaxSearchForTills')}";

            $.ajax({
                url: url,
                data: filterParams,
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function clearFilters() {
            $("#storeIdFilter").val("");
            $("#tillIdFilter").val("");
            $("#serialNumberFilter").val("");
        }

        function searchButtonClicked() {
            $('#offset').val(0);
            getTills();
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
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Till Assignment</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="filters-section" class="container-fluid">
    <div class="row mt-3">
        <div class="col-8 offset-2">
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
                            <label for="storeIdFilter" class="col-2 col-form-label-sm text-right">Store</label>
                            <div class="col-3">
                                <g:select name="storeIdFilter" from="${stores}" optionValue="storeId"
                                          optionKey="storeId"
                                          noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                          class="form-control select-border"
                                          disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                            </div>

                            <label for="tillIdFilter" class="col-2 col-form-label-sm text-right">Till ID</label>
                            <div class="col-4">
                                <g:textField name="tillIdFilter" class="form-control bottom-border" value="${tillId}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <label for="serialNumberFilter" class="col-2 col-form-label-sm text-right">Serial Number</label>
                            <div class="col-4">
                                <g:textField name="serialNumberFilter" class="form-control bottom-border" value="${serialNumber}" autocomplete="off"/>
                            </div>

                            <div class="col-6 text-right">
                                <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Clear</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked();">Filter</button>
                            </div>
                        </div>
                    </g:form>
                </div>
            </div>
        </div>
        <div class="col-2 text-right">
            <a id="refresh" href="#" class="btn btn-wl mt-1" onclick="searchButtonClicked();">Refresh</a>
        </div>
    </div>


</section>

<section id="tills-container" class="container-fluid mb-3">
    <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
        <div class="col-1 font-weight-bold text-center">Store ID</div>
        <div class="col-1 font-weight-bold text-center">Till ID</div>
        <div class="col-2 font-weight-bold text-center">Serial Number</div>
    </div>

    <div id="results-container">

    </div>
</section>
</body>
</html>