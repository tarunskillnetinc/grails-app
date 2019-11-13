<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane User Management</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#userSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        search();
                    }
                });
            });

            function search() {
                var URL = "${createLink(controller: 'user', action: 'ajaxGetUsers')}";
                var searchTerm = $('#userSearchTerm').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: { searchTerm: searchTerm, offset: 0, max: 50 },
                    success: function(resp) {
                        $('#search-results').html(resp);
                    }
                })
            }
        </script>
    </head>

    <body>
        <section id="users-search" class="container-fluid">
            <div class="row header-wl">
                <h2 class="mx-auto">User Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <div class="row mt-4 ml-0 mr-0">
                <div class="input-group offset-2 col-8">
                    <g:textField id="userSearchTerm" name="userSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                    <div class="input-group-append">
                        <g:select id="userSearchBy" name="userSearchBy" from="${['everything']}" value="everything" valueMessagePrefix="UserSearchBy" class="form-control select-border" style="z-index: 0;" />
                        <asset:image src="search.png" id="userSearchButton" name="userSearchButton" onclick="search()" class="wl-search-button" />
                    </div>
                </div>

                <div class="col-2 text-right">
                    <g:link controller="user" action="add" class="btn btn-wl">Add New User</g:link>
                </div>
            </div>

            <div class="row mt-5 mb-2 ml-0 mr-0">
                <div class="col-4 font-weight-bold">Username</div>
                <div class="col-4 font-weight-bold">Name</div>
                <div class="col-2 font-weight-bold">Date of Birth</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="userSearchResults" model="[users: users]" />
            </div>
        </section>
    </body>
</html>