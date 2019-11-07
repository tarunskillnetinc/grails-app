<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane User Management</title>
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
                <div class="col-2 offset-10 text-right">
                    <g:link controller="user" action="add" class="btn btn-wl">Add New User</g:link>
                </div>
            </div>

            <div class="row mt-5 mb-2 ml-0 mr-0">
                <div class="col-2 font-weight-bold">ID</div>
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