<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${isUpdate ? "Edit" : "Add"} Partner Category</title>

    <asset:stylesheet href="checkbox.css" />
    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="multi-category-select.js" />
    <asset:javascript src="money-mask.js" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="co-utils.js"/>

    <style>
        .alert.alert-danger.alert-wl ul {
            list-style: none; /* Remove bullets */
            padding: 0; /* Remove left padding */
            margin: 0; /* Remove margin */
        }

        .alert.alert-danger.alert-wl li {
            margin-bottom: 5px; /* Add spacing between items */
        }
    </style>


    <script type="text/javascript">

        let getChildCategoriesUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxGetChildCategories')}";
        let categorySearchUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxSearchCategories')}";
        let categoryFilterUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxFilterValidCategories')}";

        function handleCancelAddPartnerCategory(url) {
            confirmAndSubmit("Are you sure you want to cancel ?", function() {
                cancelAddPartnerCategory(url);
            });
        }

        function confirmAndSubmit(message, yesCallBack) {
            let result = confirm(message);
            if (result) {
                yesCallBack();
            }
        }

        function cancelAddPartnerCategory(url) {
            var tempLink = document.createElement('a'); // Create a temporary anchor element
            tempLink.href = url;
            document.location.href = tempLink.href; // Navigate to the modified URL
        }

        function validateForm() {
            let isValid = true;
            let messages = [];

            // Get the input values
            let partnerSupplierId = document.getElementById('partnerSupplierId').value.trim();
            let partnerCategoryName = document.getElementById('partnerCategoryName').value.trim();
            let selectedCategories = document.querySelectorAll('input[name="category.id[]"]:checked');

            // Validate Partner Name
            if (!partnerSupplierId) {
                isValid = false;
                messages.push("Partner name is required. Please select partner name.");
            }

            // // Validate Partner Category Name
            // if (!partnerCategoryName) {
            //     isValid = false;
            //     messages.push("Partner category name is required. Please add category name.");
            // }

            if (selectedCategories.length === 0) {
                isValid = false;
                messages.push("Category is required. Please select at least one category.");
            }

            // Display error messages if validation fails
            const messagesContainer = document.getElementById('messages-container');
            messagesContainer.innerHTML = ''; // Clear previous messages
            if (!isValid) {
                const messagesContainer = document.getElementById('messages-container');
                messagesContainer.innerHTML = ''; // Clear any previous messages
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger alert-wl';
                alertDiv.innerHTML = messages.join('<br>'); // Join all messages with <br>
                messagesContainer.appendChild(alertDiv);
                return false; // Prevent form submission
            }

            return true; // Allow form submission
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link href="#" class="partner-category-link" controller="partnerCategoryManagement" action="index">Partner Category</g:link></li>
                    <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${isUpdate ? 'Edit Partner Category' : 'Add Partner Category'}</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="segment-details" class="container-fluid">

    <div class="row header-wl mt-3 mb-5">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">${isUpdate ? 'Edit Partner Category' : 'Add Partner Category'}</h2>
        </div>
    </div>


    <div id="messages-container">
        <g:if test="${ecomSupplierCategory?.hasErrors()}">
            <section id="errors-container">
                <div class="alert alert-danger alert-wl" role="alert">
                    <g:renderErrors bean="${ecomSupplierCategory}"/>
                </div>
            </section>
        </g:if>
        <g:else>
            <g:if test="${flash.categoryMessage}">
                <div id="alerts-success-container-message" class="alert alert-success" role="alert">${flash.categoryMessage}</div>
            </g:if>
            <g:if test="${flash.categoryError}">
                <div id="alerts-success-container-message" class="alert alert-danger" role="alert">${flash.categoryError}</div>
            </g:if>
        </g:else>
    </div>

</section>

<section class="mt-5">
    <g:form method="post" action="savePartnerCategory" class="mt-4" name="partner-category-form" onsubmit="return validateForm();">
        <g:hiddenField id="isUpdate" name="isUpdate" value="${isUpdate}"/>
        <g:hiddenField id="partnerCategoryId" name="partnerCategoryId" value="${ecomSupplierCategory?.id}"/>

        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8 col-md-10">

                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="partnerSupplierId" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Partner Name</label>

                            <div class="flex-grow-1">
                                <g:select name="partnerSupplierId"
                                          id="partnerSupplierId"
                                          from="${ecomSupplierList}"
                                          optionKey="id"
                                          optionValue="name"
                                          value="${selectedPartnerId}"
                                          class="form-control select-border"
                                          noSelection="['':'Please select partner']"
                                          onchange="filterCategories(this.value, '${ecomSupplierCategory?.id ?: ''}')"
                                          disabled="${isUpdate}"></g:select>
                                <g:hiddenField name="partnerSupplierId" value="${selectedPartnerId}" />
                            </div>
                        </div>
                    </div>


                    <div class="mb-5">
                        <div class="d-flex align-items-center">
                            <label for="partnerCategoryName" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Partner Category Name</label>

                            <div class="flex-grow-1">
                                <g:textField id="partnerCategoryName"
                                             name="partnerCategoryName"
                                             value="${ecomSupplierCategory?.description}"
                                             class="form-control bottom-border alpha-field"
                                             minLength="1"
                                             maxLength="45"
                                             size="45"/>
                            </div>
                        </div>
                    </div>

                    <div class="mb-5">
                        <div class="d-flex align-items-top" style="overflow: hidden;">
                            <label for="category" class="col-form-label mb-0 mr-3"
                                   style="width: 120px; text-align: right;">Select Category</label>

                            <div class="flex-grow-1" style="overflow: hidden;">
                                <g:render template="/multiSelectCategory/categorySelect"
                                          model="[categories: categories,
                                                  productCategoryList: productCategoryList,
                                                  selectedCategoryIds: selectedCategoryIds,
                                                  level: 1,
                                                  triggerOnCategoryChange: false,
                                                  specialId: ecomSupplierCategory?.id
                                          ]"

                                />
                            </div>
                        </div>
                    </div>


                    <div class="mt-5 text-center">
                        <button id="save-safe-cancel" type="button" name="safe-save-button" onclick="handleCancelAddPartnerCategory('${createLink(action:'index')}')" class="btn btn-wl mr-2">Cancel</button>
                        <button id="partner-category-save-btn" class="btn btn-success" name="save" onclick="$('#partner-category-form').submit();">Save</button>
                    </div>

                </div>
            </div>
        </div>
    </g:form>
</section>

</body>
</html>