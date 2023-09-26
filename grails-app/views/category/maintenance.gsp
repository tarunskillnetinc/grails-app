<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Category Maintenance</title>

    <asset:stylesheet href="radio.css" />
    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="category-select.js" />
    <asset:javascript src="money-mask.js" />
    <asset:javascript src="bootstrap-datepicker.min.js" />

    <script type="text/javascript">
        let getChildCategoriesUrl = "${createLink(controller: 'product', action: 'ajaxGetChildCategories')}";
        let categorySearchUrl = "${createLink(controller: 'category', action: 'ajaxSearchMaintenanceCategories')}";

        $(document).ready(function () {
            $(".mask-money").maskMoney({ allowZero: true });
            $(".mask-money").maskMoney('mask');
        });

        function onCategoryChanged(selectedCategoryId) {
            let getInheritanceUrl = "${createLink(controller: 'category', action: 'ajaxGetInheritance')}";
            $.ajax({
                url: getInheritanceUrl,
                method: "GET",
                data: {
                    selectedCategoryId: selectedCategoryId,
                },
                success: function (resp) {
                    $("#categoryInheritance").html(resp);
                    $(".mask-money").maskMoney({ allowZero: true });
                    $(".mask-money").maskMoney('mask');
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
            $('#description').on('input', function () {
                $(this).val($(this).val().replace(/[^\x00-\x7F]/g, ""))
            })

            $('#shortDescription').on('input', function () {
                $(this).val($(this).val().replace(/[^\x00-\x7F]/g, ""))
            })

            $('#retailerCategoryCode').on('input', function () {
                $(this).val($(this).val().replace(/[^\x00-\x7F]/g, ""))
            })
        })
    </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="category" action="index">Category Maintenance</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${category?.description ?: "Add Category"}</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="maintenance-section" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 class="mx-auto my-auto">Category Maintenance</h2>
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