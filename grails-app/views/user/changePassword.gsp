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
                            <li class="breadcrumb-item"><g:link controller="user" action="userEdit" params="[id: userId]">User Edit</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${userId ?: "Change Password"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="add-user-section" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-7 offset-2">
                    <h2 class="mx-auto my-auto">Change Password</h2>
                </div>

                <div class="col-3 text-right">
                    <g:link controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>

                    <g:if test="${!isUserReadOnly}">
                        <button class="btn btn-success" name="delete"  onclick="$('#change-user-password-form').submit();">Save</button>
                    </g:if>
                    <g:else>
                        <button class="btn btn-success" name="delete" disabled onclick="$('#change-user-password-form').submit();">Save</button>
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

            <g:hasErrors bean="${saveUserPasswordCommand}">
                <section id="errors-container">
                    <div class="alert alert-danger alert-wl mx-0" role="alert">
                        <g:renderErrors bean="${saveUserPasswordCommand}" as="list" />
                    </div>
                </section>
            </g:hasErrors>

            <g:form name="change-user-password-form" action="editUserPassword" novalidate="novalidate" class="mt-4">

                <g:hiddenField name="id" value="${userId ?: 0}" />

                <div class="form-group row col-12 col-lg-6">
                    <label for="password" class="col-4 col-form-label text-right pr-4">Password</label>
                    <g:passwordField name="password" id="password" class="col-5 form-control bottom-border" autocomplete="off" readonly="${isUserReadOnly}"/>
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="confirmPassword" class="col-4 col-form-label text-right pr-4">Confirm Password</label>
                    <g:passwordField name="confirmPassword" id="confirmPassword" class="col-5 form-control bottom-border" readonly="${isUserReadOnly}"/>
                </div>

            </g:form>

        </section>
    </body>
</html>