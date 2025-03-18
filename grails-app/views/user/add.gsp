<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>User Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="co-utils.js"/>

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

            <g:if test="${flash.message}">
                <section id="errors-container">
                    <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
                </section>
            </g:if>

            <g:hasErrors bean="${user}">
                <section id="errors-container">
                    <div class="alert alert-danger alert-wl mx-0" role="alert">
                        <g:renderErrors bean="${user}" as="list" />
                    </div>
                </section>
            </g:hasErrors>

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

                        <div class="form-group row mt-5">
                            <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                            <div class="col-6">
                                <g:textField name="name" class="form-control bottom-border" value="${user?.name}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="password" class="col-4 col-form-label text-right pr-4">Password</label>
                            <div class="col-6">
                                <g:passwordField name="password" class="form-control bottom-border" value="${user?.password}" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="confirmPassword" class="col-4 col-form-label text-right pr-4">Confirm Password</label>
                            <div class="col-6">
                                <g:passwordField name="confirmPassword" class="form-control bottom-border" value="${user?.confirmPassword}" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>
                            <div class="col-6">
                                <g:textField name="dateOfBirth" type="text" class="form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="storeId" class="col-4 col-form-label text-right pr-4">Home store</label>
                            <div class="col-6">
                                <div class="dropdown-content">
                                    <input type="text" class="form-control bottom-border" placeholder="Search for store.." id="storeIdInput" onkeyup="filter('storeIdInput','storeId')">
                                    <g:select id="storeId" size="6" name="storeId" style="overflow-y: scroll; overflow-x: hidden;" from="${stores}" optionValue="${{it.config.storeNumber +' - ' +it.config.storeName}}"
                                              value="${user?.storeId}"
                                              optionKey="${{it.config.storeNumber}}"
                                              class="form-control select-border"
                                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}" />
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

                        <div class="form-group row mt-5">
                            <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>
                            <div class="col-6">
                                <g:checkBox name="ageRelatedSaleAllowed" class="ml-1 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>
                            <div class="col-6">
                                <g:textField name="securityKey" class="form-control bottom-border" value="${user?.securityKey}" autocomplete="off" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>
                            <div class="col-6">
                                <g:select name="role" class="form-control select-border" from="${roleValues}" value="${user?.role}" valueMessagePrefix="Role" />
                            </div>
                        </div>

                        <div class="form-group row mt-5">
                            <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>
                            <div class="col-6">
                                <g:textField name="retailerUserId" class="form-control bottom-border" value="${user?.retailerUserId}" autocomplete="off" />
                            </div>
                        </div>
                    </div>
                </div>
            </g:form>

%{--            <g:form name="add-user-form" action="save" novalidate="novalidate" class="mt-4">--}%
%{--                <g:hiddenField name="id" value="${user?.id ?: 0}" />--}%

%{--                <div class="row mt-5 mb-3">--}%
%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>--}%
%{--                        <g:textField name="username" class="col-5 form-control bottom-border" value="${user?.username}" autocomplete="off" />--}%
%{--                    </div>--}%

%{--                    <div class="form-group form-check row col-12 col-lg-6">--}%
%{--                        <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>--}%
%{--                        <g:checkBox name="active" class="col-1 ml-0 form-check-input wl-checkbox" checked="${user?.active || !user}" />--}%
%{--                    </div>--}%
%{--                </div>--}%

%{--                <div class="row mt-5 mb-3">--}%
%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>--}%
%{--                        <g:textField name="name" class="col-5 form-control bottom-border" value="${user?.name}" autocomplete="off" />--}%
%{--                    </div>--}%

%{--                    <div class="form-group form-check row col-12 col-lg-6">--}%
%{--                        <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>--}%
%{--                        <g:checkBox name="ageRelatedSaleAllowed" class="col-1 ml-0 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" />--}%
%{--                    </div>--}%
%{--                </div>--}%

%{--                <div class="row mt-5 mb-3">--}%
%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="password" class="col-4 col-form-label text-right pr-4">Password</label>--}%
%{--                        <g:passwordField name="password" class="col-5 form-control bottom-border" value="${user?.password}" />--}%
%{--                    </div>--}%

%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>--}%
%{--                        <g:textField name="securityKey" class="col-5 form-control bottom-border" value="${user?.securityKey}" autocomplete="off" />--}%
%{--                    </div>--}%
%{--                </div>--}%

%{--                <div class="row mt-5 mb-3">--}%
%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="confirmPassword" class="col-4 col-form-label text-right pr-4">Confirm Password</label>--}%
%{--                        <g:passwordField name="confirmPassword" class="col-5 form-control bottom-border" value="${user?.confirmPassword}" />--}%
%{--                    </div>--}%

%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>--}%
%{--                        <g:select name="role" class="col-3 form-control select-border" from="${roleValues}" value="${user?.role}" valueMessagePrefix="Role" />--}%
%{--                    </div>--}%
%{--                </div>--}%

%{--                <div class="row mt-5 mb-3">--}%
%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>--}%
%{--                        <g:textField name="dateOfBirth" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />--}%
%{--                    </div>--}%

%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>--}%
%{--                        <g:textField name="retailerUserId" class="col-5 form-control bottom-border" value="${user?.retailerUserId}" autocomplete="off" />--}%
%{--                    </div>--}%
%{--                </div>--}%

%{--                <div class="row mt-5 mb-3">--}%
%{--                    <div class="form-group row col-12 col-lg-6">--}%
%{--                        <label for="storeId" class="col-4 col-form-label text-right pr-4">Home store</label>--}%
%{--                        <div class="dropdown-content col-lg-5">--}%
%{--                            <input type="text" class="form-control bottom-border" placeholder="Search for store.." id="storeIdInput" onkeyup="filter('storeIdInput','storeId')">--}%

%{--                            <g:select id="storeId" size="6" name="storeId" style="overflow-y: scroll; overflow-x: hidden;" from="${stores}" optionValue="${{it.config.storeNumber +' - ' +it.config.storeName}}"--}%
%{--                                      value="${till?.storeId}"--}%
%{--                                      optionKey="${{it.config.storeNumber}}"--}%
%{--                                      class="form-control select-border"--}%
%{--                                      disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}" />--}%
%{--                        </div>--}%
%{--                    </div>--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>--}%
%{--                    <g:textField name="username" class="col-5 form-control bottom-border" value="${user?.username}" autocomplete="off" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="password" class="col-4 col-form-label text-right pr-4">Password</label>--}%
%{--                    <g:passwordField name="password" class="col-5 form-control bottom-border" value="${user?.password}" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="confirmPassword" class="col-4 col-form-label text-right pr-4">Confirm Password</label>--}%
%{--                    <g:passwordField name="confirmPassword" class="col-5 form-control bottom-border" value="${user?.confirmPassword}" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>--}%
%{--                    <g:textField name="name" class="col-5 form-control bottom-border" value="${user?.name}" autocomplete="off" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>--}%

%{--                    <g:textField name="dateOfBirth" type="text" class="col-5 form-control bottom-border" value="${user?.dateOfBirth ? user?.dateOfBirth?.format('dd/MM/yyyy') : null}" autocomplete="off" />--}%
%{--                </div>--}%

%{--                <div class="form-group form-check row col-12 col-lg-6">--}%
%{--                    <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>--}%
%{--                    <g:checkBox name="active" class="col-1 form-check-input wl-checkbox" checked="${user?.active || !user}" />--}%
%{--                </div>--}%

%{--                <div class="form-group form-check row col-12 col-lg-6">--}%
%{--                    <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>--}%
%{--                    <g:checkBox name="ageRelatedSaleAllowed" class="col-1 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>--}%
%{--                    <g:textField name="securityKey" class="col-5 form-control bottom-border" value="${user?.securityKey}" autocomplete="off" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>--}%
%{--                    <g:select name="role" class="col-3 form-control select-border" from="${roleValues}" value="${user?.role}" valueMessagePrefix="Role" />--}%
%{--                </div>--}%

%{--                <div class="form-group row col-12 col-lg-6">--}%
%{--                    <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>--}%
%{--                    <g:textField name="retailerUserId" class="col-5 form-control bottom-border" value="${user?.retailerUserId}" autocomplete="off" />--}%
%{--                </div>--}%
%{--            </g:form>--}%
        </section>
    </body>
</html>