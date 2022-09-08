<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane User Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type='text/javascript'>

            $(document).ready(function() {

                $('#dateOfBirth').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    endDate: new Date().toString(),
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

            });

            function ConfirmUserDelete() {
                var result = confirm("Are you sure you want to delete this user?");
                if (result) {
                    document.location.href='${createLink(action:'deleteUser', id: user?.id)}';
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
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item"><g:link controller="user" action="index">User Management</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${user?.name ?: "Edit User"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="add-user-section" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-6 offset-2">
                    <h2 class="mx-auto my-auto">Edit User</h2>
                </div>

                <div class="col-4 text-right">
                    <g:link controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>

                    <g:if test="${!isUserReadOnly}">
                        <button class="btn btn-danger" name="save" onclick="ConfirmUserDelete()">Delete</button>
                        <button class="btn btn-success" name="delete" onclick="$('#edit-user-form').submit();">Save</button>
                        <button class="btn btn-warning" name="delete" onclick="document.location.href='${createLink(action:'changePassword', params: [id: user?.id, name : user?.name] )}';">Reset Password</button>
                    </g:if>
                    <g:else>
                        <button class="btn btn-danger" name="save" disabled onclick="ConfirmUserDelete()">Delete</button>
                        <button class="btn btn-success" name="delete" disabled onclick="$('#edit-user-form').submit();">Save</button>
                        <button class="btn btn-warning" name="delete" disabled onclick="document.location.href='${createLink(action:'changePassword', params: [id: user?.id, name : user?.name])}';">Reset Password</button>
                    </g:else>

                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${flash.error}">
                <section id="errors-container">
                    <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
                </section>
            </g:if>

            <g:hasErrors bean="${user}">
                <section id="errors-container">
                    <div class="alert alert-danger alert-wl mx-0" role="alert">
                        <g:renderErrors bean="${user}" as="list" />
                    </div>
                </section>
            </g:hasErrors>

            <g:if test="${user}">
                <g:form name="edit-user-form" action="editSelectedUser" novalidate="novalidate" class="mt-4">

                    <div class="form-group row col-12 col-lg-6 mt-4">
                        <label for="id" class="col-4 col-form-label text-right pr-4">ID</label>
                        <g:textField name="id" class="col-5 form-control bottom-border" value="${user?.id}" readonly="true"/>
                    </div>

                    <div class="form-group row col-12 col-lg-6">
                        <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>
                        <g:textField name="username" class="col-5 form-control bottom-border" value="${user?.username}" readonly="true"/>
                    </div>

                    <div class="form-group row col-12 col-lg-6">
                        <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                        <g:textField name="name" class="col-5 form-control bottom-border" value="${user?.name}" readonly="${isUserReadOnly}"/>
                    </div>

                    <div class="form-group row col-12 col-lg-6">
                        <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>
                        <g:textField name="dateOfBirth" class="col-5 form-control bottom-border" value="${g.formatDate(format: "dd/MM/yyyy", date: user?.dateOfBirth)}" readonly="${isUserReadOnly}"/>
                    </div>

                    <div class="form-group form-check row col-12 col-lg-6">
                        <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>
                        <g:checkBox name="active" class="col-1 form-check-input wl-checkbox" checked="${user?.active || !user}" disabled="${isUserReadOnly}"/>
                    </div>

                    <div class="form-group form-check row col-12 col-lg-6">
                        <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>
                        <g:checkBox name="ageRelatedSaleAllowed" class="col-1 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" disabled="${isUserReadOnly}"/>
                    </div>

                    <div class="form-group row col-12 col-lg-6">
                        <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>
                        <g:textField name="securityKey" class="col-5 form-control bottom-border" value="${user?.securityKey}" readonly="${isUserReadOnly}"/>
                    </div>

                    <div class="form-group row col-12 col-lg-6">
                        <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>
                        <g:select name="role" class="col-3 form-control select-border" from="${roleValues}" value="${user?.role}" readonly="${isUserReadOnly}" valueMessagePrefix="Role"/>
                    </div>

                    <div class="form-group row col-12 col-lg-6">
                        <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>
                        <g:textField name="retailerUserId" class="col-5 form-control bottom-border" value="${user?.retailerUserId}" readonly="${isUserReadOnly}"/>
                    </div>

                </g:form>
            </g:if>
        </section>
    </body>
</html>