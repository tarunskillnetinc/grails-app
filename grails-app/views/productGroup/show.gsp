<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Product Group Management</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link
                                    controller="productGroup" action="index">Product Group Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active"
                                aria-current="page">${productGroup?.description ?: "View Product Group"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="tag-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="page-title" class="mx-auto">Product Group Management</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="edit-productGroup-btn" action="edit" id="${productGroup.id}"
                            class="btn btn-wl">Edit Product Group</g:link>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <div class="form-container mt-4">

                    <!-- Centered Form Layout -->
                    <div class="row justify-content-center">
                        <!-- First Column (Left) -->
                        <div class="col-12 col-md-6">
                            <!-- Name -->
                            <div class="form-group row mt-4">
                                <label for="description" class="col-6 col-form-label text-right pr-4">Description</label>
                                <g:textField name="description" class="col-6 form-control"
                                             value="${productGroup?.description}" maxlength="50" disabled="true"/>
                            </div>

                            <!-- Start Date -->
                            <div class="form-group row mt-4">
                                <label for="startDate" class="col-6 col-form-label text-right pr-4">Start Date</label>
                                <g:textField name="startDate" type="text" class="col-6 form-control" required="true"
                                             autoComplete="off"
                                             value="${productGroup?.startDate ?: new Date().format("EEEE dd MMMM yyyy")}"/>
                            </div>

                            <!-- Status -->
                            <div class="form-group row mt-4">
                                <label for="status" class="col-6 col-form-label text-right pr-4">Status</label>
                                <g:select name="status" from="${['Active', 'Inactive']}"
                                          value="${productGroup?.active?'Active':'Inactive'}" class="col-6 form-control"/>
                            </div>

                            <!-- Never Expires Checkbox -->
%{--                            <div class="form-group row mt-4">--}%
%{--                                <label for="neverExpires" class="col-6 col-form-label text-right pr-4">Never Expires</label>--}%

%{--                                <div class="col-6 d-flex align-items-center">--}%
%{--                                    <g:checkBox name="neverExpires" value="${productGroup?.neverExpires}" class="big-checkbox"/>--}%
%{--                                </div>--}%
%{--                            </div>--}%
                        </div>

                        <!-- Second Column (Right) -->
                        <div class="col-12 col-md-6">

                            <!-- Category Selection -->
                            <div class="form-group row mt-4">
                                <label for="category" class="col-6 col-form-label text-right pr-4">Category</label>
                                <g:select name="categoryId"
                                          from="${categories}"
                                          optionKey="id"
                                          optionValue="description"
                                          value="${productGroup?.category?.id}"
                                          class="col-6 form-control"/>
                            </div>

                            <!-- End Date -->
                            <div class="form-group row mt-4">
                                <label for="endDate" class="col-6 col-form-label text-right pr-4">End Date</label>
                                <g:textField name="endDate" type="text" class="col-6 form-control" required="true"
                                             autoComplete="off"
                                             value="${productGroup?.endDate ?: new Date().format("EEEE dd MMMM yyyy")}"/>
                            </div>

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
                        <h3 class="mx-auto section-title">Group Restriction</h3>
                    </div>

                    <div class="row justify-content-center">
                        <div class="col-12 col-md-6">
                            <!-- Day Restrictions -->
                            <div class="form-group row mt-4">
                                <label class="col-6 col-form-label text-right pr-4">Day Restrictions</label>
                                <div class="col-6 px-0">
                                    <div class="checkbox-group">
                                        <g:each in="${['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']}" var="day" status="i">
                                            <div class="form-check">
                                                <input type="checkbox" class="form-check-input" id="${day}" name="days" value="${i}"
                                                    ${productGroup?.days ? productGroup?.days[i] ? 'checked' : '':''}>
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
                                <label for="restrictionStartTime" class="col-6 col-form-label text-right pr-4">Start Time</label>

                                <div class="col-6 px-0">
                                    <div id="startTimeContainer" class="input-group bootstrap-timepicker timepicker">
                                        <input id="restrictionStartTime" name="restrictionStartTime" type="text" class="form-control"
                                               value="${productGroup?.restrictionStartTime}"/>
                                    </div>
                                </div>
                            </div>

                            <!-- End Time -->
                            <div class="form-group row mt-4">
                                <label for="restrictionEndTime" class="col-6 col-form-label text-right pr-4">End Time</label>

                                <div class="col-6 px-0">
                                    <div id="endTimeContainer" class="input-group bootstrap-timepicker timepicker">
                                        <input id="restrictionEndTime" name="restrictionEndTime" type="text" class="form-control"
                                               value="${productGroup?.restrictionEndTime}"/>
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
                                <a id="add-product-btn" href="#" class="btn btn-wl mr-2" data-toggle="modal"
                                   data-target="#productSearchModal">
                                    Add Product
                                </a>
                                <button id="addCategoryBtn" class="btn btn-wl">Add Category</button>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-12">
                            <div class="row font-weight-bold mb-2">
                                <div class="col-3">Item Code</div>
                                <div class="col-3">SKU</div>
                                <div class="col-4">Description</div>
                                <div class="col-2">&nbsp;</div>
                            </div>

                            <div id="productList" class="row align-content-center mb-5">
                                <g:if test="${!productGroup?.productGroupProducts || productGroup?.productGroupProducts?.size() == 0}">
                                    <div id="noResultsRow"
                                         class="col-12 pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                                </g:if>

                                <g:each in="${productGroup?.productGroupProducts?.sort { it.sku }}" var="productGroupProduct"
                                        status="i">
                                    <g:render template="productGroupProductRow"
                                              model="[productGroupProduct: productGroupProduct, i: i]"/>
                                </g:each>
                            </div>
                        </div>
                    </div>
            </div>

            <div class="header-wl mt-5">
                <h3 class="mx-auto">Products</h3>
            </div>

            <div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
                <div class="col-2 font-weight-bold">Item Code</div>
                <div class="col-4 font-weight-bold">SKU</div>
                <div class="col-6 font-weight-bold">Description</div>
            </div>

            <div id="search-results" class="align-content-center mb-5">
                <g:if test="${!productGroup.productGroupProducts || productGroup.productGroupProducts?.size() == 0}">
                    <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                </g:if>

                <g:each in="${productGroup.productGroupProducts?.sort { it.sku }}" var="productGroupProduct" status="i">
                    <div id="productGroup-product-${i + 1}" class="row col-8 offset-2 pt-2 pb-2 wl-striped${i % 2}">
                        <div id="productGroup-product-${i + 1}-id" class="col-2">${productGroupProduct.itemCode}</div>

                        <div id="productGroup-product-${i + 1}-sku" class="col-4">${productGroupProduct.sku}</div>

                        <div id="productGroup-product-${i + 1}-description"
                             class="col-6">${productGroupProduct.productDescription}</div>
                    </div>
                </g:each>
            </div>
        </section>
    </body>
</html>