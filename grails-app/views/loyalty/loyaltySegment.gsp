<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Loyalty Segment Management</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <script type="application/javascript">

        function searchButtonClicked() {
            $('#offset').val(0);
            search();
        }

        function resetForm() {
            document.getElementById('loyaltySegmentTerm').value = null;
            document.getElementById('loyaltySegmentSearchBy').value = 'Description';
        }

        function search() {
            var url = "${createLink(controller: 'product', action: 'ajaxSearchLoyaltySegment')}";
            var searchTerm = $('#loyaltySegmentTerm').val();
            var searchBy = $('#loyaltySegmentSearchBy').val();

            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: url,
                data: { searchTerm: searchTerm, searchBy: searchBy },
                success: function(resp) {
                    $('#results-container').html(resp);
                    $('#loyaltySegmentTerm').data('prev',$('#loyaltySegmentTerm').val())
                    $('#loyaltySegmentSearchBy').data('prev', $('#loyaltySegmentSearchBy').val())
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Loyalty Segment Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="reasonCodeMaintenance" class="container-fluid">

    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto">Loyalty Segment Management</h2>
        </div>
    </div>

    <div class="alert alert-danger alert-wl" role="alert" id="error-message" style="display: none"></div>

    <section id="filters-section" class="container-fluid">
        <div class="row mt-3">
            <div class="col-8 offset-2">
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
                            <label for="loyaltySegmentTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                            <div class="col-10 input-group">
                                <g:textField id="loyaltySegmentTerm" name="loyaltySegmentTerm" maxlength="100" value="${session.PRODUCT_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                                <div class="input-group-append">
                                    <g:select id="loyaltySegmentSearchBy" name="loyaltySegmentSearchBy" from="${['Description', 'Id']}" value="everything" valueMessagePrefix="loyaltySegmentSearchBy" class="form-control select-border" style="z-index: 0;" />
                                </div>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="col-sm-8 col-xl-6 offset-sm-4 offset-xl-6 text-right">
                                <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div id="results-container" class="align-content-center">
        <g:render template="loyaltySegmentSearchResults" />
    </div>

</section>

</body>
</html>