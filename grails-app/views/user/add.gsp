<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane User Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />

        <script type='text/javascript'>
            $(function(){
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
        </script>
    </head>

    <body>
        <section id="add-user-section" class="container-fluid">
            <div class="row header-wl">
                <h2 class="mx-auto">Add User</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <g:hasErrors bean="${user}">
                <div class="alert alert-danger alert-wl" role="alert">
                    <g:renderErrors bean="${user}" as="list" />
                </div>
            </g:hasErrors>

            <g:form name="save-button" action="save" novalidate="novalidate" class="mt-4">
                <g:hiddenField name="id" value="${user?.id ?: 0}" />

                <div class="form-group row col-12 col-lg-6">
                    <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>
                    <g:textField name="username" class="col-5 form-control bottom-border" value="${user?.username}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="password" class="col-4 col-form-label text-right pr-4">Password</label>
                    <g:passwordField name="password" class="col-5 form-control bottom-border" value="${user?.password}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="confirmPassword" class="col-4 col-form-label text-right pr-4">Confirm Password</label>
                    <g:passwordField name="confirmPassword" class="col-5 form-control bottom-border" value="${user?.confirmPassword}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                    <g:textField name="name" class="col-5 form-control bottom-border" value="${user?.name}" />
                </div>

                <div class="form-group row col-12 col-lg-6" id="dateofBirthDatePicker">
                    <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>

                    <g:textField name="dateOfBirth" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
                </div>

                <div class="form-group form-check row col-12 col-lg-6">
                    <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>
                    <g:checkBox name="active" class="col-1 form-check-input wl-checkbox" checked="${user?.active || !user}" />
                </div>

                <div class="form-group form-check row col-12 col-lg-6">
                    <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>
                    <g:checkBox name="ageRelatedSaleAllowed" class="col-1 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>
                    <g:textField name="securityKey" class="col-5 form-control bottom-border" value="${user?.securityKey}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>
                    <g:select name="role" class="col-3 form-control select-border" from="${roleValues}" value="${user?.role}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>
                    <g:textField name="retailerUserId" class="col-5 form-control bottom-border" value="${user?.retailerUserId}" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <div class="offset-lg-4">
                        <g:link controller="user" action="index" role="button" class="btn btn-danger">Cancel</g:link>

                        <g:submitButton class="btn btn-success" name="save" value="Save" />
                    </div>
                </div>
            </g:form>
        </section>
    </body>
</html>