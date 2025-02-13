<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>${productGroup?.description ? "Edit Product Group" : "Add Product Group"}</title>
    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:stylesheet src="bootstrap-timepicker.min.css"/>

    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="co-utils.js"/>
    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="bootstrap-timepicker.min.js"/>

    <style>
.bootstrap-timepicker-widget table td a {
    color: #6c757d !important;
    background-color: #f8f9fa !important;
    border: 1px solid #ced4da !important;
        border-radius: 0.25rem;
        padding: 5px;
    }

.bootstrap-timepicker-widget table td a:hover {
    background-color: #e9ecef !important;
    color: #495057 !important;
}

    .bootstrap-timepicker-widget table td a {
        padding: 0;
    }

.bootstrap-timepicker-widget .svg-icon-up:before {
    content: "+";
    }

.bootstrap-timepicker-widget .svg-icon-down:before {
    content: "-";
}

    .big-checkbox {
        width: 25px;
        height: 25px;
        cursor: pointer;
    }

    .big-checkbox + label {
        vertical-align: middle;
        margin-left: 5px;
    }

    .form-container {
        background-color: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 0.25rem;
        padding: 40px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .checkbox-group {
        max-height: 15rem;
        overflow-y: auto;
        border: 1px solid #ced4da;
        border-radius: 0.25rem;
        padding: 5px;
    }

    .form-check {
        padding-left: 1.5rem;
    }

    .form-check-input {
        margin-top: 0.3rem;
        margin-left: -1.5rem;
    }
    </style>
    <script>
        $(function () {
            intListener("maxSellQuantity", 10, 999);

            $('#restrictionStartTime, #restrictionEndTime').timepicker({
                showMeridian: false,
                defaultTime: false,
                minuteStep: 1,
                showInputs: false,
                disableFocus: true,
                showSeconds: false,
                icons: {
                    up: 'svg-icon-up',
                    down: 'svg-icon-down'
                },
                template: 'dropdown'
            }).on('changeTime.timepicker', function (e) {
                var hours = e.time.hours.toString().padStart(2, '0');
                var minutes = e.time.minutes.toString().padStart(2, '0');
                $(this).val(hours + ':' + minutes);
            });
            ;

            // Show widget when clicking on the input or the icon
            $('#startTimeContainer, #endTimeContainer').on('click', function (e) {
                e.preventDefault();
                $(this).children().first().timepicker('showWidget');
            });

            // Prevent default keyboard events on the input
            $('#restrictionStartTime, #restrictionEndTime').on('keydown', function (e) {
                e.preventDefault();
            });

            $("#startDate").datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function (event) {
                let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
                let formatted = formatDate(event.date, options, '-');

                $("#startDate").val(formatted);
            });

            $("#endDate").datepicker({
                format: "dd/mm/yyyy",
                weekStart: 1,
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function (event) {
                let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
                let formatted = formatDate(event.date, options, '-');

                $("#endDate").val(formatted);
            });

            $('#neverExpires').change(function () {
                if ($(this).is(':checked')) {
                    $('#endDate').val('');
                }
            });
        });

        function modeEditInputDisable() {
            if (${edit}) {
                $('input').prop('disabled', true);
                $('select').prop('disabled', true);
                $('button').hide();
                $('input[type="button"]').hide();
                $('input[type="submit"]').hide();
                $('a[role="button"]').hide();
            }
        }

        function formatDate(date, options, separator) {
            function format(option) {
                let formatter = new Intl.DateTimeFormat('en', option);
                return formatter.format(date);
            }

            return options.map(format).join(separator);
        }

        function addProductGroupCategoryProducts(id) {
            var addProductsFromCategoryUrl = "${createLink(controller: 'productGroup', action: 'ajaxAddProductsFromCategory')}";

            addProductsFromCategory(id, addProductsFromCategoryUrl);
        }

        function addProductsFromCategory(id, url) {
            // Figure out what group ID (position) this will be based on how many groups already exist.
            var groupId = 0;

            // if (selectedPromotionGroupType === "required") {
            //     groupId = $("#promotionRequiredGroupsContainer").children("div").length;
            // } else if (selectedPromotionGroupType === "offer") {
            //     groupId = $("#promotionOfferGroupsContainer").children("div").length;
            // }

            $.ajax({
                url: url,
                data: {
                    id: id,
                    groupId: groupId
                },
                success: function (resp) {
                    $("#productList").append(resp);
                    $('#noResultsRow').hide();
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
                    <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li class="breadcrumb-item"><g:link controller="productGroup"
                                                        action="index">Product Group Management</g:link></li>
                    <li class="breadcrumb-item active" aria-current="page">
                        ${productGroup?.description ? "Edit Product Group" : "Add Product Group"}
                    </li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="central-count-search" class="container">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title"
                class="mx-auto">${productGroup?.description ? "Edit Product Group" : "Add Product Group"}</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel-btn" action="${params.action == 'edit' ? 'show' : 'index'}"
                    id="${productGroup?.id}" role="button" class="btn btn-danger">Cancel</g:link>

            <button id="save-btn" class="btn btn-success" name="save"
                    onclick="$('#productGroup-form').submit();">Save</button>
        </div>
    </div>

    <g:if test="${flash.message}">
        <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
    </g:if>

    <g:hasErrors bean="${productGroupErrors}">
        <div id="tag-management-errors-list" class="alert alert-danger alert-wl mx-0" role="alert">
            <g:renderErrors bean="${productGroupErrors}" as="list"/>
        </div>
    </g:hasErrors>
    <div class="form-container mt-4">
        <g:form name="productGroup-form" action="save" novalidate="novalidate" class="mt-4">
            <g:hiddenField name="id" value="${productGroup?.id ?: 0}"/>

            <!-- Centered Form Layout -->
            <div class="row justify-content-center">
                <!-- First Column (Left) -->
                <div class="col-12 col-md-6">
                    <!-- Name -->
                    <div class="form-group row mt-4">
                <label for="description" class="col-6 col-form-label text-right pr-4">Product Group Name</label>
                        <g:textField name="description" class="col-6 form-control"
                                     value="${productGroup?.description}" maxlength="60"/>
                    </div>

                    <!-- Start Date -->
                    <div class="form-group row mt-4">
                        <label for="startDate" class="col-6 col-form-label text-right pr-4">Start Date</label>
                        <g:textField name="startDate" type="text" class="col-6 form-control" required="true"
                                     autoComplete="off"
                                     value="${productGroup?.startDate ?: new Date().format("dd/MM/yyyy")}"/>
                    </div>

                    <!-- Never Expires Checkbox -->
                    <div class="form-group row mt-4">
                        <label for="neverExpires" class="col-6 col-form-label text-right pr-4">Never Expires</label>

                <div class="col-6 d-flex align-items-center p-0">
                            <g:checkBox id="neverExpires" name="neverExpires" value="${productGroup?.neverExpires}"
                                        class="big-checkbox"/>
                        </div>
                    </div>
                </div>

                <!-- Second Column (Right) -->
                <div class="col-12 col-md-6">
                <!-- Status -->
                <div class="form-group row mt-4">
                    <label for="status" class="col-6 col-form-label text-right pr-4">Status</label>
                    <g:select name="status" from="${['Active', 'Inactive']}"
                              value="${productGroup ? (productGroup?.active ? 'Active' : 'Inactive') : 'Active'}"
                              class="col-6 form-control"/>
                </div>

                <!-- End Date -->
                <div class="form-group row mt-4">
                    <label for="endDate" class="col-6 col-form-label text-right pr-4">End Date</label>
                    <g:textField name="endDate" type="text" class="col-6 form-control" required="true"
                                 autoComplete="off"
                                 value="${productGroup?.endDate}"/>
                </div>

            </div>
            </div>

            <!-- Group Restriction Section -->
            <div class="header-wl mt-2">
                <h3 class="mx-auto section-title">Group Restriction</h3>
            </div>

            <div class="row justify-content-center">
                <div class="col-12 col-md-6">
                    <!-- Maximum Sell Quantity -->
                    <div class="form-group row mt-4">
                        <label for="maxSellQuantity"
                               class="col-6 col-form-label text-right pr-4">Maximum Sell Quantity</label>
                        <g:field name="maxSellQuantity" type="number" min="0" max="999"
                                 value="${productGroup?.maxSellQuantity}" class="col-6 form-control"
                                 onkeypress="return preventNegativeInteger(event);" onpaste="return false;"/>
                    </div>
                </div>
            </div>

            <!-- Group Restriction Section -->
            <div class="header-wl mt-2">
                <h3 class="mx-auto section-title">Day/Time Restriction</h3>
            </div>

            <div class="row justify-content-center">
                <div class="col-12 col-md-6">

                    <!-- Day Restrictions -->
                    <div class="form-group row mt-4">
                        <label class="col-6 col-form-label text-right pr-4">Day Restrictions</label>

                        <div class="col-6 px-0">
                            <div class="checkbox-group">
                                <g:each in="${['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']}"
                                        var="day" status="i">
                                    <div class="form-check">
                                        <input type="checkbox" class="form-check-input" id="${day}" name="days"
                                               value="${i}"
                                            ${productGroup?.days?.contains(i) ? 'checked' : ''}>
                                        <label class="form-check-label" for="${day}">${day}</label>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-md-6">
                    <!-- Start Time -->
                    <div class="form-group row mt-4">
                    <label for="restrictionStartTime"
                           class="col-6 col-form-label text-right pr-4">Start Time</label>

                        <div class="col-6 px-0">
                            <div id="startTimeContainer" class="input-group bootstrap-timepicker timepicker">
                                <input id="restrictionStartTime" name="restrictionStartTime" type="text"
                                       class="form-control input-small"
                                       value="${productGroup?.restrictionStartTime}"/>
                                <span class="input-group-addon">
                                    <svg class="icon-clock" width="16" height="16">
                                        <use xlink:href="#icon-clock"></use>
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- End Time -->
                    <div class="form-group row mt-4">
                        <label for="restrictionEndTime" class="col-6 col-form-label text-right pr-4">End Time</label>

                        <div class="col-6 px-0">
                            <div id="endTimeContainer" class="input-group bootstrap-timepicker timepicker">
                                <input id="restrictionEndTime" name="restrictionEndTime" type="text"
                                       class="form-control input-small"
                                       value="${productGroup?.restrictionEndTime}"/>
                                <span class="input-group-addon">
                                    <svg class="icon-clock" width="16" height="16">
                                        <use xlink:href="#icon-clock"></use>
                                    </svg>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Products Table -->
            <div class="header-wl mt-5">
                <h3 class="mx-auto">Products</h3>
            </div>

            <div class="row mt-4">
                <div class="col-12">
                    <div class="d-flex justify-content-end mb-3">
                        <a id="add-product-btn" href="#" role="button" class="btn btn-wl mr-2" data-toggle="modal"
                           data-target="#productSearchModal">
                            Add Products
                        </a>
                        <a id="addCategoriesBtn" href="#" role="button" class="btn btn-wl mr-2" data-toggle="modal"
                           data-target="#categoryAddProductsModal">
                            Add Categories
                        </a>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-12">
                    <div class="row font-weight-bold mb-2">
                        <div class="col-2">Item Code</div>

                        <div class="col-2">SKU</div>

                        <div class="col-2">Barcode</div>

                        <div class="col-3">Description</div>

                        <div class="col-2">Category</div>

                        <div class="col-1"></div>
                    </div>

                    <div id="productList" class="row align-content-center mb-5">
                        <g:if test="${!productGroup?.productGroupProductsDisplayRow || productGroup?.productGroupProductsDisplayRow?.size() == 0}">
                            <div id="noResultsRow"
                                 class="col-17 pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                        </g:if>

                        <g:each in="${productGroup?.productGroupProductsDisplayRow?.sort { it.sku }}"
                                var="productGroupProductsDisplayRow"
                                status="i">
                            <g:render template="productGroupProductRow"
                                      model="[productGroupProduct: productGroupProductsDisplayRow, i: i, edit: edit]"/>
                        </g:each>
                    </div>
                </div>
            </div>
        </g:form>
    </div>
</section>


<g:render template="categorySearch"/>
<g:render template="/product/productSearch"/>

<asset:javascript src="productgroup.js"/>


<script type='text/javascript'>
    var addProductUrl = "${createLink(controller: 'productGroup', action: 'ajaxAddProduct')}";
</script>

</body>
</html>
