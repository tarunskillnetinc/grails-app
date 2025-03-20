<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>User Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="user-co-utils.js"/>

        <script type='text/javascript'>
            $(function() {
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
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="user" action="index">User Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Add User</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="add-user-section" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto my-auto" id="page-title">Add User</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="cancel" controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>

                    <button id="save" class="btn btn-success" name="save" onclick="$('#add-user-form').submit();">Save</button>
                </div>
            </div>

            <g:if test="${flash.error}">
                <section id="errors-container">
                    <div class="alert alert-success alert-wl mx-0" role="alert">${flash.error}</div>
                </section>
            </g:if>
            <g:else>
                <g:if test="${user}">
                    <g:hasErrors bean="${user}">
                        <section id="errors-container">
                            <div class="alert alert-danger alert-wl mx-0" role="alert">
                                <g:renderErrors bean="${user}" as="list" />
                            </div>
                        </section>
                    </g:hasErrors>
                </g:if>
            </g:else>

            <g:form name="add-user-form" action="save" novalidate="novalidate" class="mt-4">
                <g:hiddenField name="id" value="${user?.id ?: 0}" />

                <div class="row">
                    <div class="col-md-6 mt-5">
                        <!-- Left column -->
                        <div class="form-group row">
                            <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>
                            <div class="col-6">
                                <g:textField name="username" class="form-control bottom-border" value="${user?.username}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                            <div class="col-6">
                                <g:textField name="name" class="form-control bottom-border" value="${user?.name}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="password" class="col-4 col-form-label text-right pr-4">Password</label>
                            <div class="col-6">
                                <g:passwordField name="password" class="form-control bottom-border" value="${user?.password}" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="confirmPassword" class="col-4 col-form-label text-right pr-4">Confirm Password</label>
                            <div class="col-6">
                                <g:passwordField name="confirmPassword" class="form-control bottom-border" value="${user?.confirmPassword}" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>
                            <div class="col-6">
                                <g:textField name="dateOfBirth" type="text" class="form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="defaultStoreId" class="col-4 col-form-label text-right pr-4">Home Store</label>
                            <div class="col-6">
                                <div class="dropdown-content">
                                    <g:hiddenField name="defaultStoreId" value="${user?.defaultStoreId ?: (isLoggedInFromStoreLevel ? defaultStore?.id : '')}" />
                                    <input type="text" class="form-control bottom-border" placeholder="Search for store.."
                                           id="storeIdInput" onkeyup="filter('storeIdInput','defaultStoreIdSelector')"  value="${isLoggedInFromStoreLevel ? defaultStore?.config?.storeNumber + '-' + defaultStore?.config?.storeName : ''}" >
                                    <g:select id="defaultStoreIdSelector"
                                              size="6"
                                              name="defaultStoreIdSelector"
                                              style="overflow-y: scroll; overflow-x: hidden;"
                                              from="${stores}"
                                              onchange="updateFields(this);"
                                              onclick="updateFields(this);"
                                              optionValue="${{it?.config?.storeNumber + ' - ' + it?.config?.storeName}}"
                                              value="${user?.defaultStoreId ?: (isLoggedInFromStoreLevel ? defaultStore?.id : '')}"
                                              optionKey="${{it?.id}}"
                                              class="form-control select-border"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 mt-5">
                        <!-- Right column -->
                        <div class="form-group row">
                            <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>
                            <div class="col-6">
                                <g:checkBox name="active" class="ml-1 form-check-input wl-checkbox" checked="${user?.active || !user}" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>
                            <div class="col-6">
                                <g:checkBox name="ageRelatedSaleAllowed" class="ml-1 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>
                            <div class="col-6">
                                <g:textField name="securityKey" class="form-control bottom-border" value="${user?.securityKey}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>
                            <div class="col-6">
                                <g:select name="role" class="form-control select-border" from="${roleValues}" value="${user?.role}" valueMessagePrefix="Role" />
                            </div>
                        </div>

                        <div class="form-group row mt-3">
                            <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>
                            <div class="col-6">
                                <g:textField name="retailerUserId" class="form-control bottom-border" value="${user?.retailerUserId}" autocomplete="off" />
                            </div>
                        </div>
                    </div>
                </div>
            </g:form>
        </section>
    </body>
</html>