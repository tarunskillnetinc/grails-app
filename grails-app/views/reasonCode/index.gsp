<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Reason Code Maintenance</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <script type="application/javascript">
        const searchUrl = "${createLink(controller: 'reasonCode', action: 'ajaxSearch')}";

        $(document).ready(function() {
            $('.step').on('click', function() {
                $('html, body').animate({ scrollTop: 0 }, 'fast');
            });
            ajaxSearch()
        });

        function ajaxSearch() {
            const resultsContainer = $('#search-results');

            resultsContainer.html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");

            $.ajax({
                url: searchUrl,
                data: {type: $("#code-type-select").val(), offset: 0, max: 50},
                success: function (resp) {
                    resultsContainer.html(resp);
                },
                error: function () {
                    resultsContainer.html('<div class="px-0 text-center">' +
                    '<div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div></div>')
                }
            })
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Reason Code Maintenance</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="reasonCodeMaintenance" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto">Reason Code Maintenance</h2>
        </div>
        <div class="col-2 text-right">
            <g:link elementId="add-new-category" controller="category" action="add" class="btn btn-wl">Add Reason Code</g:link>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-6">
            <div class="card bg-light border-wl">
                <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
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
                        <label for="code-type-select" class="col-2 col-form-label-sm text-right">Reason Code Type</label>
                        <div class="col-10 input-group">
                            <select name="code-type" id="code-type-select" onchange="ajaxSearch()">
                                <option value="PAID_OUT">Paid Out</option>
                                <option value="CUSTOMER_REFUSAL">Customer Refusal</option>
                                <option value="LINE_VOID">Line Void</option>
                                <option value="MARKDOWN">Markdown</option>
                                <option value="REFUND">Refund</option>
                                <option value="TENDER_RECONCILIATION_VARIANCE">Tender Reconciliation Variance</option>
                                <option value="PRODUCT_LIST">Product List</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
        <div class="col-6 font-weight-bold">Description</div>
        <div class="col-6 font-weight-bold"></div>
    </div>

    <div id="search-results">
        <div class="px-0 text-center">
            <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
        </div>
    </div>
</section>



</body>
</html>