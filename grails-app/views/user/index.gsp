<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>User Management</title>

        <script type="text/javascript">
            $(document).ready(function () {
                // $('#userSearchTerm').on('keyup', function(event) {
                //     if (event.key === 'Enter') {
                //         search();
                //     }
                // });

                search();

                if (${showInactiveUserFilter}) {
                    $('#showInactiveUserFilter').prop('checked', true);
                }
            });

            function search() {
                var URL = "${createLink(controller: 'user', action: 'ajaxGetUsers')}";

                var filterParams = { };

                $("#filtersForm input").each(function() {
                    filterParams[$(this).attr("name")] = $(this).val();
                }).get();

                $("#filtersForm select").each(function() {
                    filterParams[$(this).attr("name")] = $(this).find(":selected").val();
                }).get();

                $("#filtersForm input[type='checkbox']").each(function() {
                    filterParams[$(this).attr("name")] = $(this).prop("checked");
                });

                filterParams['offset'] = 0;
                filterParams['max'] = 50;


                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: filterParams,
                    success: function(resp) {
                        $('#search-results').html(resp);
                    }
                })
            }

            function clearFilters(){
                $("#userNameFilter").val("");
                $("#homeStoreFilter").val("");
                $('#showInactiveUserFilter').prop('checked', false);
                search()
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
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">User Management</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="users-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto" id="page-title">User Management</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="add-user-button" controller="user" action="add" class="btn btn-wl">Add New User</g:link>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert" id="alert-success">${flash.message}</div>
            </g:if>

            <g:if test="${flash.error}">
                <section id="errors-container">
                    <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
                </section>
            </g:if>


            <section id="filters-section">
                <div class="row mt-3">
                    <div class="col-6">
                        <div id="filters" class="card bg-light border-wl">
                            <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
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
                                <form name="filtersForm" id="filtersForm">
                                    <div class="form-group row">
                                        <label for="userNameFilter" class="col-2 col-form-label-sm text-right">Username or name</label>
                                        <div class="col-4">
                                            <input type="text" id="userNameFilter" name="userNameFilter" value="${userNameFilter}" class="form-control bottom-border" />
                                        </div>

                                        <label for="homeStoreFilter" class="col-2 col-form-label-sm text-right">Home Store</label>
                                        <div class="col-4">
                                            <g:select id="homeStoreFilter" name="homeStoreFilter" from="${stores}" optionValue="${{it.config.storeNumber + '-' + it.config.storeName}}"
                                                      optionKey="id"
                                                      noSelection="${isLoggedInFromStoreLevel ? ['': defaultStore?.config?.storeNumber + '-' + defaultStore?.config?.storeName] : ['': '']}"
                                                      class="form-control select-border"></g:select>
                                        </div>
                                    </div>

                                    <div class="form-group row">
                                        <label for="showInactiveUserFilter" class="col-2 col-form-label-sm text-left mr-0">Search inactive users</label>
                                        <div class="col-4">
                                            <input type="checkbox" id="showInactiveUserFilter" name="showInactiveUserFilter" class="form-check-input ml-1 wl-checkbox pointer"  />
                                        </div>

                                        <div class="col-6 text-right">
                                            <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Reset Filters</button>
                                            <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search();">Filter</button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <div class="row mt-5 ml-0 mr-0 pb-2 table-wl bottom-border">
                <div class="col-4 font-weight-bold" id="username-header">Username</div>
                <div class="col-4 font-weight-bold" id="name-header">Name</div>
                <div class="col-2 font-weight-bold" id="dob-header">Home Store</div>
                <div class="col-2 font-weight-bold" id="status">Status</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="userSearchResults"  model="[users: users]"/>
            </div>
        </section>
    </body>
</html>