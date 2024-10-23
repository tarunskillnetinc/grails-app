<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${isUpdate ? "Edit" : "Add"} Safe</title>

    <script>
        function submitSaveSafe() {
            if (confirm('Confirm changes. Are you sure you wish to save these changes?')) {
                var activeCheckbox = document.getElementById('active');
                var hiddenActiveInput = document.getElementById('hiddenActive');
                // Copy the value of the checkbox to the hidden input
                hiddenActiveInput.name = 'active'; // Change the name to 'active' right before submission
                hiddenActiveInput.value = activeCheckbox.checked ? 'true' : 'false';
                // Remove the checkbox name temporarily if it is not disabled to avoid duplicate submissions
                if (!activeCheckbox.disabled) {
                    activeCheckbox.removeAttribute('name');
                }
                $('#safeDetails').submit();
            }
        }
    </script>

    <style>
    .large-checkbox {
        width: 20px;
        height: 20px;
        cursor: pointer;
    }
    </style>

</head>

<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link
                            action="index">Safe Management</g:link></li>
                    <li id="breadcrumb-3" class="breadcrumb-item active"
                        aria-current="page">${isUpdate ? 'Edit Safe' : 'Add Safe'}</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="alerts-container" class="container-fluid">
    <g:if test="${flash.message}">
        <div id="alerts-container-message" class="alert alert-success" role="alert">${flash.message}</div>
    </g:if>
</section>

<section id="alerts-error-container" class="container-fluid">
    <g:if test="${flash.error}">
        <div id="alerts-container-message" class="alert alert-danger" role="alert">${flash.error}</div>
    </g:if>
</section>

<section id="segment-details" class="container-fluid">
    <div class="alert alert-danger alert-wl mx-0" role="alert" id="error-message" hidden></div>

    <div class="row header-wl mt-3 mb-5">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">Safe Details</h2>
        </div>
    </div>
</section>

<section class="mt-5">
    <g:form method="post" action="saveSafe" class="mt-4" name="safeDetails">
        <g:hiddenField id="id" name="id" value="${safe?.id}"/>
        <g:hiddenField id="isUpdate" name="isUpdate" value="${isUpdate}"/>

        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8 col-md-10">
                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="description" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Safe Description</label>

                            <div class="flex-grow-1">
                                <g:textField id="description" name="description" value="${safe?.description}"
                                             class="form-control bottom-border alpha-field" maxLength="20"/>
                            </div>
                        </div>
                    </div>

                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="type" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Segment Type</label>

                            <div class="flex-grow-1">
                                <g:select id="type" name="type" from="${safeTypes}" value="${safe?.type}"
                                          class="form-control select-border"/>
                            </div>
                        </div>
                    </div>

                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="active" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Safe Status</label>

                            <div class="flex-grow-1 d-flex align-items-center">
                                <g:checkBox id="active" name="active"
                                            value="${safe?.active}"
                                            checked="${!isUpdate || safe?.active}"
                                            disabled="${!isUpdate}"
                                            class="mt-0 large-checkbox"/>
                                <input type="hidden" id="hiddenActive" name="hiddenActive" value="${safe?.active}"/>
                            </div>
                        </div>
                    </div>

                    <div class="mt-5 text-center">
                        <g:link elementId="editSegment-cancel" action="index" class="btn btn-wl mr-2"
                                onClick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
                        <button id="editSegmentSave" type="button" name="editSegment-save-button"
                                onclick="submitSaveSafe()" class="btn btn-success ml-2">Save</button>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</section>

</body>
</html>