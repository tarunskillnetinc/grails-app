<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${isUpdate ? "Edit" : "Add"} Partner Category</title>

    <asset:stylesheet href="radio.css" />
    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="multi-category-select.js" />
    <asset:javascript src="money-mask.js" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="co-utils.js"/>


    <script type="text/javascript">

        let getChildCategoriesUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxGetChildCategories')}";
        let categorySearchUrl = "${createLink(controller: 'partnerCategoryManagement', action: 'ajaxSearchCategories')}";

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

            // Validate Partner Category Name
            if (!partnerCategoryName) {
                isValid = false;
                messages.push("Partner category name is required. Please add category name.");
            }

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
                alertDiv.className = 'alert alert-danger';
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

    <g:hasErrors bean="${ecomSupplierCategory}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${ecomSupplierCategory}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <div id="messages-container"></div>

    <g:if test="${flash.message}">
        <div id="alerts-success-container-message" class="alert alert-success" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div id="alerts-success-container-message" class="alert alert-danger" role="alert">${flash.error}</div>
    </g:if>

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
                                          class="form-control select-border"></g:select>
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
                                                  productCategoryList: partnerCategoryList,
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