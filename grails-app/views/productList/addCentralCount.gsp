<%@ page import="org.joda.time.DateTimeZone" contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Central Count Management</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb flex-nowrap">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item text-truncate" aria-current="page"><g:link controller="productList" action="listCentralCounts">Central Counts</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active text-truncate" aria-current="page">${productList?.description ?: "Add Central Count"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 id="page-title" class="mx-auto">Central Count Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            %{--set to hidden so we can display warning message with client side rendering--}%
            <div id="warning-message" class="alert alert-warning alert-wl mx-0 hidden" role="alert"></div>

            <g:hasErrors bean="${productList}">
                <div id="error-list" class="alert alert-danger alert-wl mx-0" role="alert">
                    <g:renderErrors bean="${productList}" as="list" />
                </div>
            </g:hasErrors>

            <g:form name="central-count-form" action="saveCentralCount" novalidate="novalidate" class="mt-4">
                <div class="container">
                    <div class="row">
                        <div class="col">
                            <g:hiddenField name="id" value="${productList?.id}" />

                            <div class="form-group row mt-4">
                                <label for="description" class="col-4 col-form-label text-left">Description</label>
                                <g:textField name="description" class="col-8 form-control bottom-border" value="${productList?.description}" />
                            </div>

                            <div class="form-group row mt-4">
                                <label for="startDate" class="col-4 col-form-label text-left">Start Date</label>

                                <g:textField name="startDate" type="text" class="col-8 form-control bottom-border"
                                             value="${g.formatDate(format: "dd/MM/yyyy", date: productList?.startDate?.toDate())}"
                                             autocomplete="off"/>
                            </div>

                            <div class="form-group row mt-4">
                                <label for="endDate" class="col-4 col-form-label text-left">End Date</label>
                                <g:textField name="endDate" class="col-8 form-control bottom-border"
                                             value="${g.formatDate(format: "dd/MM/yyyy", date: productList?.endDate?.toDate())}"
                                             autocomplete="off"/>
                            </div>

                            <div class="form-group row col-12 col-lg-6 mt-4">
                                <div class="offset-lg-4">
                                    <g:link elementId="cancel-btn" action="listCentralCounts" role="button" class="btn btn-danger">Cancel</g:link>
                                    <g:submitButton class="btn btn-success" name="save" value="Save" />
                                </div>
                            </div>
                        </div>

                        <div class="col">
                            <div class="form-group row mt-4 ml-5">
                                <label for="endDate" class="col-4 col-form-label text-left pr-4">Stores</label>
                                    <div class="checkbox-scroll-container" style="height: 200px; overflow-y: auto; padding-left: 20px; padding-right: 20px; border: 1px solid black; ">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" id="selectAllStores" name="selectAllStores" onclick="toggleSelectAll()">
                                            <label class="form-check-label" for="selectAllStores">All Stores</label>
                                        </div>
                                        <g:each in="${availableStores}" var="store">
                                            <div class="form-check">
                                                <g:checkBox class="form-check-input" type="checkbox" id="storeId${store.config.storeNumber}" name="storeIdList" value="${store.id}" checked="${command?.storeIdList?.contains(store.id)}"/>
                                                <label class="form-check-label" for="storeId${store.config.storeNumber}">${store.config.storeName}</label>
                                            </div>
                                        </g:each>
                                    </div>


                            </div>
                        </div>
                    </div>
                </div>

                <div class="header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row mt-4 mx-0">
                    <div class="col-2 offset-10 text-right px-0">
                        <!-- Button trigger modal -->
                        <a id="add-product-btn" href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
                            Add Product
                        </a>
                    </div>
                </div>

                <div class="row mt-4 ml-0 mr-0 bottom-border">
                    <div class="col-1 font-weight-bold">Product ID</div>
                    <div class="col-2 font-weight-bold">Item Code</div>
                    <div class="col font-weight-bold">Description</div>
                    <div class="col-1 font-weight-bold">&nbsp;</div>
                </div>

                <div id="productList" class="align-content-center mb-5">
                    <g:if test="${(!productList?.productListItems || productList?.productListItems?.size() == 0) && (!unsavedVariants || unsavedVariants?.size() == 0) }">
                        <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${productList?.productListItems}" var="productListItem" status="i">
                        <g:render template="centralCountProductRow" model="[productVariant: productListItem.productVariant, i: i]" />
                    </g:each>

                    <g:each in="${unsavedVariants}" var="productVariant" status="i">
                        <g:render template="centralCountProductRow" model="[productVariant: productVariant, i: i]" />
                    </g:each>
                </div>
            </g:form>
        </section>

        <!-- Product search modal -->
        <g:render template="/product/productSearch" />

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="productList.js" />

        <script type='text/javascript'>
            var addProductUrl = "${createLink(controller: 'productList', action: 'ajaxAddProduct')}";

            function isBeforeToday(dateString) {
                <%-- Split the entered dd/MM/yyyy format date into its component parts and generate a new Date object based on it. --%>
                let parts = dateString.split("/");
                let date = new Date(parts[2], parts[1] - 1, parts[0]);
                <%-- Compare the generated date object against today's Date object --%>
                return date < new Date();
            }

            $(document).ready(function () {
                $('#startDate').on("change", function () {
                    $('#startDate').val(this.value);
                    $('#startDate').removeClass('is-invalid');
                    <%-- Ensure that the end date can't be before today, or the selected start date --%>
                    if (isBeforeToday(this.value)) {
                        $('#endDate').datepicker('setStartDate', "${new Date().format("dd/MM/yyyy")}");
                    } else {
                        $('#endDate').datepicker('setStartDate', this.value);
                    }
                });

                $('#endDate').on("change", function () {
                    $('#endDate').val(this.value);
                    $('#endDate').removeClass('is-invalid');
                    <%-- End date has been updated, so ensure the start date doesn't allow a date after this --%>
                    $('#startDate').datepicker('setEndDate', this.value);
                });
            })

            $(function() {
                $('#startDate').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });

                $('#endDate').datepicker({
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    startDate: "${new Date().format("dd/MM/yyyy")}",
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });
            });
            function toggleSelectAll(checkbox) {
                var selectAllCheckbox = document.getElementById("selectAllStores");
                var storeCheckboxes = document.querySelectorAll('input[name="storeIdList"]');

                // Check or uncheck all store checkboxes based on the state of the "Select All" checkbox
                storeCheckboxes.forEach(function(checkbox) {
                    checkbox.checked = selectAllCheckbox.checked;
                });
            }
            function handleStoreCheckboxChange() {
                var selectAllCheckbox = document.getElementById("selectAllStores");
                var storeCheckboxes = document.querySelectorAll('input[name="storeIdList"]');

                // Check if any store checkbox is unchecked
                var isAnyUnchecked = Array.from(storeCheckboxes).some(function(checkbox) {
                    return !checkbox.checked;
                });

                // If any store checkbox is unchecked, uncheck the "Select All" checkbox
                if (isAnyUnchecked) {
                    selectAllCheckbox.checked = false;
                }
            }

            // Add event listeners to individual store checkboxes
            var storeCheckboxes = document.querySelectorAll('input[name="storeIdList"]');
            storeCheckboxes.forEach(function(checkbox) {
                checkbox.addEventListener("change", handleStoreCheckboxChange);
            });
        </script>
    </body>
</html>