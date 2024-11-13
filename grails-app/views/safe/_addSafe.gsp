<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${isUpdate ? "Edit" : "Add"} Safe</title>

    <asset:stylesheet href="radio.css"/>
    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="category-select.js"/>
    <asset:javascript src="money-mask.js"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="safeUrls.js"/>
    <asset:javascript src="safeConfiguration.js"/>

    <script type="text/javascript">

        $(document).ready(function () {
            $("#messages-container").html('');

            document.querySelector('.safe-configuration-link').addEventListener('click', function(event) {
                event.preventDefault();
                cancelAddSafeView('${createLink(action:'closeSafeAdd')}');
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link href="#" class="safe-configuration-link">Safe Configurations</g:link></li>
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${isUpdate ? 'Edit Safe' : 'Add Safe'}</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="segment-details" class="container-fluid">

    <div class="row header-wl mt-3 mb-5">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">${isUpdate ? 'Edit Safe' : 'Add Safe'}</h2>
        </div>
    </div>

    <div id="messages-container"></div>

    <g:if test="${flash.message}">
        <div id="alerts-success-container-message" class="alert alert-success" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div id="alerts-success-container-message" class="alert alert-danger" role="alert">${flash.error}</div>
    </g:if>

</section>

<section class="mt-5">
    <g:form method="post" action="saveSafe" class="mt-4" name="safeDetails">
        <g:hiddenField id="id" name="id" value="${safe?.id}"/>
        <g:hiddenField id="isUpdate" name="isUpdate" value="${isUpdate}"/>
        <g:hiddenField id="inactiveSafes" name="inactiveSafes" value="${inactiveSafes}"/>

        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8 col-md-10">
                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="description" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Safe Description</label>

                            <div class="flex-grow-1">
                                <g:textField id="description" name="description" value="${safe?.description}"
                                             class="form-control bottom-border alpha-field" minLength="1" maxLength="45" size="45"/>
                            </div>
                        </div>
                    </div>

                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="type" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Safe Type</label>

                            <div class="col-2 form-check form-check-inline">
                                <g:radio class="form-check-input ml-2 wl-radio" type="radio" name="type" id="type"
                                         value="MANUAL"
                                         checked="${safe?.type == uk.co.wonderlane.wlpos.enums.SafeType.MANUAL || safe?.type == null}"/>
                                <label class="form-check-label" for="manual">Manual</label>
                            </div>

                            <div class="col-2 form-check form-check-inline">
                                <g:radio class="form-check-input wl-radio" type="radio" name="type" id="type"
                                         value="SMART"
                                         checked="${safe?.type == uk.co.wonderlane.wlpos.enums.SafeType.SMART}"/>
                                <label class="form-check-label" for="smart">Smart</label>
                            </div>
                        </div>
                    </div>

                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="active" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Safe Status</label>

                            <div class="col-2 form-check form-check-inline">
                                <g:radio class="form-check-input ml-2 wl-radio" type="radio" name="active" id="active"
                                         value="${true}"
                                         checked="${!isUpdate || safe?.active}"
                                         disabled="${!isUpdate}"/>
                                <label class="form-check-label" for="active">Active</label>
                            </div>

                            <div class="col-2 form-check form-check-inline">
                                <g:radio class="form-check-input wl-radio" type="radio" name="active" id="inactive"
                                         value="${false}"
                                         checked="${isUpdate && !safe?.active}"
                                         disabled="${!isUpdate}"/>
                                <label class="form-check-label" for="inactive">Inactive</label>
                            </div>

                            <g:if test="${!isUpdate}">
                                <input type="hidden" name="active" value="${true}" />
                            </g:if>

                        </div>
                    </div>

                    <div class="mt-5 text-center">
                        <button id="save-safe-cancel" type="button" name="safe-save-button" onclick="handleCancelAddSafe('${createLink(action:'closeSafeAdd')}')" class="btn btn-wl mr-2">Cancel</button>
                        <button id="safe-save" type="button" name="safe-save-button" onclick="validateAndSave()" class="btn btn-success ml-2">Save</button>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</section>

</body>
</html>