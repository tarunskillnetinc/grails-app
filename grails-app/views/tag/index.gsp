<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Tag Management</title>

        <script type="text/javascript">
            $(document).ready(function () {
                $('#tagSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        search();
                    }
                });
            });

            function search() {
                var URL = "${createLink(controller: 'tag', action: 'ajaxGetTags')}";
                var searchTerm = $('#tagSearchTerm').val();

                $('#search-results').html("<div class=\"d-flex justify-content-center\">\n" +
                    "  <div class=\"spinner-border\" role=\"status\">\n" +
                    "    <span class=\"sr-only\">Loading...</span>\n" +
                    "  </div>\n" +
                    "</div>");

                $.ajax({
                    url: URL,
                    data: { searchTerm: searchTerm },
                    success: function(resp) {
                        $('#search-results').html(resp);
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
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">Tag Management</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto">Tag Management</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link controller="tag" action="add" class="btn btn-wl">Add New Tag</g:link>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${flash.error}">
                <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
            </g:if>

            <div class="row mt-4 ml-0 mr-0">
                <div class="input-group offset-2 col-8">
                    <g:textField id="tagSearchTerm" name="tagSearchTerm" maxlength="100" class="form-control" placeholder="Enter a search term." aria-describedby="select-addon2" />

                    <div class="input-group-append">
                        <asset:image src="search.png" id="tagSearchButton" name="tagSearchButton" onclick="search()" class="wl-search-button" />
                    </div>
                </div>
            </div>

            <div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
                <div class="col-3 font-weight-bold">Tag ID</div>
                <div class="col-6 font-weight-bold">Description</div>
                <div class="col-3 font-weight-bold">Product Count</div>
            </div>

            <div id="search-results" class="align-content-center">
                <g:render template="tagSearchResults" model="[tags: tags]" />
            </div>
        </section>
    </body>
</html>