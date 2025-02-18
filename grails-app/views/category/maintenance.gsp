<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Category Management</title>

    <asset:stylesheet href="radio.css" />
    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="category-select.js" />
    <asset:javascript src="money-mask.js" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="co-utils.js"/>

    <script type="text/javascript">
        let getChildCategoriesUrl = "${createLink(controller: 'product', action: 'ajaxGetChildCategories')}";
        let categorySearchUrl = "${createLink(controller: 'category', action: 'ajaxSearchMaintenanceCategories')}";

        $(document).ready(function () {
            $(".mask-money").maskMoney({ allowZero: true, allowEmpty: true });
            $(".mask-money").maskMoney('mask');

            $('.numberField').on('input', function() {
                this.value = this.value.replace(/[^0-9]/g, '');
                let maxLength = parseInt(this.getAttribute('maxlength'), 10);

                if (!isNaN(maxLength) && this.value.length > maxLength) {
                    this.value = this.value.slice(0, maxLength);
                }
            });

            $('#collapseCategoryHistory').on('show.bs.collapse', function () {
                getCategoryHistory(${category?.id});
            });
        });

        function validateNumericInputField(input, min, max, step, allowDecimal) {
            const regex = allowDecimal ? /[^0-9.]/g : /[^0-9]/g;
            input.value = input.value.replace(regex, '');

            if (allowDecimal) {
                const parts = input.value.split('.');
                if (parts.length > 2) {
                    input.value = parts[0] + '.' + parts.slice(1).join('');
                }
            }

            const numericValue = parseFloat(input.value);
            if (!isNaN(numericValue)) {
                if (numericValue < min) {
                    input.value = min;
                } else if (numericValue > max) {
                    input.value = input.value.slice(0, -1);
                } else {
                    input.value = allowDecimal ? Math.round(numericValue / step) * step : numericValue;
                    if (allowDecimal && input.value.includes('.')) {
                        input.value = parseFloat(input.value).toFixed(2);
                    }
                }
            }
        }

        function validatePercentageInput(input) {
            const regex = /[^0-9]/g;
            input.value = input.value.replace(regex, '');

            const numericValue = parseInt(input.value, 10);
            if (!isNaN(numericValue)) {
                if (numericValue < 0) {
                    input.value = '0';
                } else if (numericValue > 100) {
                    input.value = '100';
                }
            }
        }

        function onCategoryChanged(selectedCategoryId) {
            let getInheritanceUrl = "${createLink(controller: 'category', action: 'ajaxGetInheritance')}";
            $.ajax({
                url: getInheritanceUrl,
                method: "GET",
                data: {
                    selectedCategoryId: selectedCategoryId,
                },
                success: function (resp) {
                    $("#restrictions").html(resp);
                    $(".mask-money").maskMoney({ allowZero: true });
                    $(".mask-money").maskMoney('mask');
                    setFieldActivity()
                }
            });
        }

        function deleteCategory(categoryId) {
            if (confirm("Category deletion is irreversible, are you sure you want to continue?")) {
                var url = "${createLink(controller: 'category', action: 'ajaxDeleteCategory')}";

                $.ajax({
                    url: url,
                    method: "POST",
                    data: {categoryId: categoryId},
                    success: function (resp) {
                        if (resp === "OK") {
                            // Exit the Add/Edit till modal and return back to index
                            window.location.href = "/category/index";
                        } else {
                            $("#category-form").html(resp);
                        }
                    }
                });
            }
        }

        $(function() {
            setFieldActivity();
        })

        function setFieldActivity() {
            $('#description').on('input', function () {
                $(this).val($(this).val().replace(/[^\x00-\x7F]/g, ""))
            })

            $('#shortDescription').on('input', function () {
                $(this).val($(this).val().replace(/[^\x00-\x7F]/g, ""))
            })

            $('#retailerCategoryCode').on('input', function () {
                $(this).val($(this).val().replace(/[^\x00-\x7F]/g, ""))
            })

            $("#restrictions\\.buyerIdRequired").change(function() {
                $("#restrictions\\.buyerIdForced").prop("checked", false);
                $("#restrictions\\.buyerIdForced").attr("disabled", !this.checked);
                $("#restrictions\\.buyerAgeRestriction").attr("readonly", !this.checked);
                $("#restrictions\\.buyerChallengeAge").attr("readonly", !this.checked);
                $("#restrictions\\.sellerAgeRestriction").attr("readonly", !this.checked);
                $("#restrictions\\.allowsLoyaltyPointsCollection").attr("disabled", ${!loyaltyEnabled});
            });

            $("#restrictions\\.quantityChangeAllowed").change(function() {
                $("#restrictions\\.quantityChangeForced").prop("checked", false);
                $("#restrictions\\.quantityChangeForced").attr("disabled", !this.checked);

                $("#restrictions\\.quantityChangeRestriction").attr("readonly", !this.checked);
            });
            
            $("#restrictions\\.markdownAllowed").change(function() {
                $("#restrictions\\.maximumMarkdownPercentage").attr("readonly", !this.checked);

                $("#restrictions\\.promptForMarkdown").prop("checked", false);
                $("#restrictions\\.promptForMarkdown").attr("disabled", !this.checked);
            });


            $("#restrictions\\.allowsLoyaltyPointsCollection").attr("disabled", ${!loyaltyEnabled});
        }

        function getCategoryHistory(categoryId) {
            $('#categoryHistoryContainer').html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");

            var getCategoryHistoryUrl = "${createLink(controller: 'category', action: 'ajaxGetCategoryHistory')}";

            $.ajax({
                url: getCategoryHistoryUrl,
                method: "GET",
                data: { categoryId: categoryId },
                success: function(resp) {
                    $("#categoryHistoryContainer").html(resp);
                }
            });
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
                        <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="category" action="index">Category Management</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${category?.description ?: "Add Category"}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="maintenance-section" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">Category Management</h2>
            </div>

            <div class="col-2 text-right">
                <g:link elementId="category-maintenance-cancel" action="index" role="button" class="btn btn-wl">Cancel</g:link>
                <g:if test="${!addCategory}">
                    <button id="category-delete" class="btn btn-danger" name="delete" onclick="deleteCategory(${category?.id})">Delete</button>
                </g:if>
                <button id="category-save-btn" class="btn btn-success" name="save" onclick="$('#category-form').submit();">Save</button>
            </div>
        </div>

        <g:if test="${flash.message}">
            <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
        </g:if>

        <g:if test="${flash.error}">
            <div class="alert alert-danger alert-wl" role="alert">${flash.error}</div>
        </g:if>

        <g:hasErrors bean="${category}">
            <div class="alert alert-danger alert-wl" role="alert">
                <g:renderErrors bean="${category}" as="list" />
            </div>
        </g:hasErrors>

        <g:hasErrors bean="${restrictions}">
            <div class="alert alert-danger alert-wl" role="alert">
                <g:renderErrors bean="${restrictions}" as="list" />
            </div>
        </g:hasErrors>

        <g:render template="maintenanceForm" model="[category: category, categoryList: categoryList, topLevelCategories: topLevelCategories]"/>
    </section>
</body>
</html>