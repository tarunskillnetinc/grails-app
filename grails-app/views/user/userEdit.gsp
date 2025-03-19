<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>User Management</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="co-utils.js"/>

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
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="user" action="index">User Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${user?.name ?: "Edit User"}</li>
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
                    <g:link elementId="cancel" controller="user" action="index" role="button" class="btn btn-wl">Cancel</g:link>

                    <g:if test="${!isUserReadOnly}">
                        <button id="delete-btn" class="btn btn-danger" name="delete" onclick="ConfirmUserDelete()">Delete</button>
                        <button id="save-btn" class="btn btn-success" name="save" onclick="$('#edit-user-form').submit();">Save</button>
                        <button id="reset-password" class="btn btn-warning" name="reset-password" onclick="document.location.href='${createLink(action:'changePassword', params: [id: user?.id, name : user?.name] )}';">Reset Password</button>
                    </g:if>
                    <g:else>
                        <button id="delete-btn" class="btn btn-danger" name="delete" disabled onclick="ConfirmUserDelete()">Delete</button>
                        <button id="save-btn" class="btn btn-success" name="save" disabled onclick="$('#edit-user-form').submit();">Save</button>
                        <button id="reset-password" class="btn btn-warning" name="reset-password" disabled onclick="document.location.href='${createLink(action:'changePassword', params: [id: user?.id, name : user?.name])}';">Reset Password</button>
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
                    <g:hiddenField name="id" value="${user?.id ?: 0}" />
                    <div class="row">
                        <div class="col-md-6  mt-5">
                            <!-- Left column -->
                            <div class="form-group row">
                                <label for="username" class="col-4 col-form-label text-right pr-4">Username</label>
                                <div class="col-6">
                                    <g:textField name="username" class="form-control bottom-border" value="${user?.username}" readonly="true"/>
                                </div>
                            </div>

                            <div class="form-group row  mt-5">
                                <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                                <div class="col-6">
                                    <g:textField name="name" class="form-control bottom-border" value="${user?.name}" readonly="${isUserReadOnly}"/>
                                </div>
                            </div>

                            <div class="form-group row mt-5">
                                <label for="dateOfBirth" class="col-4 col-form-label text-right pr-4">Date of Birth</label>
                                <div class="col-6">
                                    <g:textField name="dateOfBirth" class="form-control bottom-border" value="${g.formatDate(format: "dd/MM/yyyy", date: user?.dateOfBirth)}" disabled="${isUserReadOnly}"/>
                                </div>
                            </div>

                            <div class="form-group row  mt-5">
                                <label for="defaultStoreId" class="col-4 col-form-label text-right pr-4">Home Store</label>
                                <div class="col-6">
                                    <div class="dropdown-content">
                                        <input type="text" class="form-control bottom-border" placeholder="Search for store.." id="storeIdInput" onkeyup="filter('storeIdInput','defaultStoreId')" value="${user?.getStoreIdentifier()}">
                                        <g:select id="defaultStoreId" size="6" name="defaultStoreId" style="overflow-y: scroll; overflow-x: hidden;"
                                                  from="${stores}" optionValue="${{it.config.storeNumber +' - ' +it.config.storeName}}"
                                                  value="${user?.defaultStoreId}"
                                                  onchange="updateTextField(this,'storeIdInput')"
                                                  optionKey="${{it?.id}}"
                                                  class="form-control select-border"
                                                  disabled="${isUserReadOnly}" />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6  mt-5">
                            <!-- Right column -->
                            <div class="form-group row">
                                <label for="active" class="col-4 col-form-label text-right pr-4">Active</label>
                                <div class="col-6">
                                    <g:checkBox name="active" class="ml-0 form-check-input wl-checkbox" checked="${user?.active}" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="ageRelatedSaleAllowed" class="col-4 col-form-label text-right pr-4">Age Related Sale Allowed</label>
                                <div class="col-6">
                                    <g:checkBox name="ageRelatedSaleAllowed" class="ml-0 form-check-input wl-checkbox" checked="${user?.ageRelatedSaleAllowed || !user}" disabled="${isUserReadOnly}"/>
                                </div>
                            </div>

                            <div class="form-group row  mt-5">
                                <label for="securityKey" class="col-4 col-form-label text-right pr-4">Security Key</label>
                                <div class="col-6">
                                    <g:textField name="securityKey" class="form-control bottom-border" value="${user?.securityKey}" readonly="${isUserReadOnly}"/>
                                </div>
                            </div>

                            <div class="form-group row  mt-5">
                                <label for="role" class="col-4 col-form-label text-right pr-4">Role</label>
                                <div class="col-6">
                                    <g:select name="role" class="form-control select-border" from="${roleValues}" value="${user?.role}" disabled="${isUserReadOnly}" valueMessagePrefix="Role"/>
                                </div>
                            </div>

                            <div class="form-group row  mt-5">
                                <label for="retailerUserId" class="col-4 col-form-label text-right pr-4">Retailer User ID</label>
                                <div class="col-6">
                                    <g:textField name="retailerUserId" class="form-control bottom-border" value="${user?.retailerUserId}" readonly="${isUserReadOnly}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:form>
            </g:if>

        </section>
    </body>
</html>