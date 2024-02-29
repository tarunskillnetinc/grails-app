<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Loyalty Members</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <script type="text/javascript">

        function validateEmail(value) {
            var input = document.createElement('input');

            input.type = 'email';
            input.required = true;
            input.value = value;

            return typeof input.checkValidity === 'function' ? input.checkValidity() : /\S+@\S+\.\S+/.test(value);
        }

        function searchButtonClicked(sortParams) {
            var url = "${createLink(controller: 'loyalty', action: 'ajaxSearchMembers')}";
            var searchTerm = $('#memberSearchTerm').val();

            if (searchTerm.length > 0)
            {
                $("#search-results").hide();
                $("#loading-indicator").show();

                var searchBy;

                if (validateEmail(searchTerm)) {
                    searchBy = "email";
                } else {
                    searchBy = "cardNumber";
                }

                $.ajax({
                    url: url,
                    data: {
                        searchBy: searchBy,
                        searchTerm: searchTerm,
                        max: sortParams ? sortParams["max"] : null,
                        offset: sortParams ? sortParams.offset : null,
                        sortColumn: sortParams ? sortParams.sortColumn : null,
                        sortOrder: sortParams ? sortParams.sortOrder : null
                    },
                    success: function(resp) {
                        $('#results-container').html(resp);

                        $('#memberSearchTerm').data('prev',$('#memberSearchTerm').val())
                    }
                });
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Membership Management</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="membership-search" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 id="page-title" class="mx-auto my-auto">Loyalty Members</h2>
            </div>
        </div>


        <div class="row mt-4">
            <div class="col-5">
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
                        <label for="memberSearchTerm" class="col-form-label-sm">Either e-mail address or membership number can be used to search for members</label>
                        <div class="form-group row">
                            <div class="col-10 input-group">
                                <g:textField id="memberSearchTerm" name="productSearchTerm" maxlength="100" value="${session.MEMBER_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-sm-8 col-xl-6 offset-sm-4 offset-xl-6 text-right">
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="loyaltySearchResults" />
        </div>
    </section>

</body>
</html>