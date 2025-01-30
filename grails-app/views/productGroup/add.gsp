<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Product Group Management</title>
    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:stylesheet src="bootstrap-timepicker.min.css"/>

    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="co-utils.js"/>
    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="bootstrap-timepicker.min.js"/>

    <script>
        $(function() {

            intListener("maxSellQuantity", 10, 999);


            $('#startTime, #endTime').timepicker({
                showMeridian: false,
                defaultTime: false,
                minuteStep: 1,
                disableFocus: true,
                showInputs: false,
                icons: {
                    up: 'glyphicon glyphicon-chevron-up',
                    down: 'glyphicon glyphicon-chevron-down'
                }
            });

            $("#startDate").datepicker({
                format: "DD dd MM yyyy",
                weekStart: 1,
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function(event) {
                let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
                let formatted = formatDate(event.date, options, '-');

                $("#startDate").val(formatted);
            });

            $("#endDate").datepicker({
                format: "DD dd MM yyyy",
                weekStart: 1,
                todayHighlight: true,
                autoclose: true,
                todayBtn: "linked",
                orientation: "bottom auto"
            }).on('changeDate', function(event) {
                let options = [{year: 'numeric'}, {month: '2-digit'}, {day: '2-digit'}];
                let formatted = formatDate(event.date, options, '-');

                $("#endDate").val(formatted);
            });

        });

        function formatDate(date, options, separator) {
            function format(option) {
                let formatter = new Intl.DateTimeFormat('en', option);
                return formatter.format(date);
            }

            return options.map(format).join(separator);
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
                        ${productGroup?.name ? "Edit Product Group" : "Add Product Group"}
                    </li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="central-count-search" class="container">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto">Product Group Management</h2>
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

    <g:hasErrors bean="${productGroup}">
        <div id="tag-management-errors-list" class="alert alert-danger alert-wl mx-0" role="alert">
            <g:renderErrors bean="${productGroup}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form name="productGroup-form" action="save" novalidate="novalidate" class="mt-4">
        <g:hiddenField name="id" value="${productGroup?.id ?: 0}"/>

        <!-- Centered Form Layout -->
        <div class="row justify-content-center">
            <!-- First Column (Left) -->
            <div class="col-12 col-md-6">
                <!-- Name -->
                <div class="form-group row mt-4">
                    <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
                    <g:textField name="name" class="col-8 form-control"
                                 value="${productGroup?.name}" maxlength="50"/>
                </div>

                <!-- Status -->
                <div class="form-group row mt-4">
                    <label for="status" class="col-4 col-form-label text-right pr-4">Status</label>
                    <g:select name="status" from="${['Active', 'Inactive']}"
                              value="${productGroup?.status}" class="col-8 form-control"/>
                </div>

                <!-- Start Date -->
                <div class="form-group row mt-4">
                    <label for="startDate" class="col-4 col-form-label text-right pr-4">Start Date</label>
                    <g:textField name="startDate" type="text" class="col-6 form-control" required="true"
                                 autoComplete="off"
                                 value="${productGroup?.startDate?.toString("EEEE dd MMMM yyyy") ?: new Date().format("EEEE dd MMMM yyyy")}" />
                </div>

                <!-- End Date -->
                <div class="form-group row mt-4">
                    <label for="endDate" class="col-4 col-form-label text-right pr-4">End Date</label>
            <g:textField name="endDate" type="text" class="col-6 form-control" required="true"
                         autoComplete="off"
                         value="${productGroup?.endDate?.toString("EEEE dd MMMM yyyy") ?: new Date().format("EEEE dd MMMM yyyy")}" />
                </div>

                <!-- Never Expires Checkbox -->
                <div class="form-group row mt-4">
                    <label class="col-4 col-form-label text-right pr-4">Never Expires</label>
                    <div class="col-8">
                        <g:checkBox name="neverExpires" value="${productGroup?.neverExpires}"/>
                    </div>
                </div>
            </div>

            <!-- Second Column (Right) -->
            <div class="col-12 col-md-6">

                <!-- Maximum Sell Quantity -->
                <div class="form-group row mt-4">
                    <label for="maxSellQuantity" class="col-4 col-form-label text-right pr-4">Maximum Sell Quantity</label>
                    <g:field name="maxSellQuantity" type="number" min="0" max="999"
                             value="${productGroup?.maxSellQuantity}" class="col-8 form-control"
                             onkeypress="return preventNegativeInteger(event);" onpaste="return false;"/>
                </div>

                <!-- Category Selection -->
                <div class="form-group row mt-4">
                    <label for="category" class="col-4 col-form-label text-right pr-4">Category</label>
                    <g:select name="category" from="${categories}" value="${productGroup?.category}"
                              class="col-8 form-control"/>
                </div>
            </div>
        </div>

        <!-- Group Restriction Section -->
        <div class="header-wl mt-2">
            <h3 class="mx-auto section-title">Group Restriction</h3>
        </div>
        <div class="row justify-content-center">
            <div class="col-12 col-md-6">
                <!-- Day Restrictions -->
                <div class="form-group row mt-4">
                    <label class="col-4 col-form-label text-right pr-4">Day Restrictions</label>
                    <g:select name="dayRestrictions" from="${['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']}"
                              value="${productGroup?.dayRestrictions}" class="col-8 form-control"
                              multiple="true" />
                </div>
            </div>
            <div class="col-12 col-md-6">
                <!-- Start Time -->
                <div class="form-group row mt-4">
                    <label for="startTime" class="col-4 col-form-label text-right pr-4">Start Time</label>
                    <div class="input-group col-8 bootstrap-timepicker timepicker">
                        <input id="startTime" name="startTime" type="text" class="form-control input-small" value="${productGroup?.startTime}" readonly/>
                        <span class="input-group-addon">
                            <i class="glyphicon glyphicon-time"></i>
                        </span>
                    </div>
                </div>

                <!-- End Time -->
                <div class="form-group row mt-4">
                    <label for="endTime" class="col-4 col-form-label text-right pr-4">End Time</label>
                    <div class="input-group col-8 bootstrap-timepicker timepicker">
                        <input id="endTime" name="endTime" type="text" class="form-control input-small" value="${productGroup?.endTime}" readonly/>
                        <span class="input-group-addon">
                            <i class="glyphicon glyphicon-time"></i>
                        </span>
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
                    <a id="add-product-btn" href="#" class="btn btn-wl mr-2" data-toggle="modal" data-target="#productSearchModal">
                        Add Product
                    </a>
                    <button id="importProductList" class="btn btn-wl">Import Product List</button>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-12">
                <div class="row font-weight-bold mb-2">
                    <div class="col-2">Item Code</div>
                    <div class="col-3">SKU</div>
                    <div class="col-6">Name</div>
                    <div class="col-1">&nbsp;</div>
                </div>

                <div id="productList" class="align-content-center mb-5">
                    <g:if test="${!productGroup?.productGroupProducts || productGroup?.productGroupProducts?.size() == 0}">
                        <div id="noResultsRow" class="col-12 pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${productGroup?.productGroupProducts?.sort { it.sku }}" var="productGroupProduct"
                            status="i">
                        <g:render template="productGroupProductRow"
                                  model="[productGroupProduct: productGroupProduct, i: i]"/>
                    </g:each>
                </div>
            </div>
        </div>
        <g:render template="/product/productSearch" />
    </g:form>
</section>

<asset:javascript src="productgroup.js"/>


<script type='text/javascript'>
    var addProductUrl = "${createLink(controller: 'productGroup', action: 'ajaxAddProduct')}";
</script>

</body>
</html>
