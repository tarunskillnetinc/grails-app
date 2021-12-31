<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane User Management</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item"><g:link controller="user" action="index">User Management</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${user.name ?: "View User"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="add-user-section" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto my-auto">View User</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${flash.error}">
                <div class="alert alert-danger alert-wl" role="alert">${flash.error}</div>
            </g:if>

            <g:if test="${user}">
                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="id" class="col-4 col-form-label text-right pr-4">ID</label>
                    <g:textField name="id" class="col-5 form-control bottom-border" value="${user?.id}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>
                    <g:textField name="username" class="col-5 form-control bottom-border" value="${user?.username}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                    <g:textField name="name" class="col-5 form-control bottom-border" value="${user?.name}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>
                    <g:textField name="dateOfBirth" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy", date:user?.dateOfBirth)}" disabled="disabled" />
                </div>

                <div class="form-group form-check row col-12 col-lg-6">
                    <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>
                    <g:checkBox name="active" class="col-1 form-check-input wl-checkbox" checked="${user?.active || !user}" disabled="disabled" />
                </div>

                <div class="form-group form-check row col-12 col-lg-6">
                    <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>
                    <g:checkBox name="ageRelatedSaleAllowed" class="col-1 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>
                    <g:textField name="securityKey" class="col-5 form-control bottom-border" value="${user?.securityKey}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>
                    <g:select name="role" class="col-3 form-control select-border" from="${[user?.role ?: ""]}" value="${user?.role}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>
                    <g:textField name="retailerUserId" class="col-5 form-control bottom-border" value="${user?.retailerUserId}" disabled="disabled" />
                </div>
            </g:if>
        </section>
    </body>
</html>