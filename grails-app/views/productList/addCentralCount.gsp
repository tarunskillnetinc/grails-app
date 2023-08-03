<%@ page contentType="text/html;charset=UTF-8" %>
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
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="productList" action="listCentralCounts">Central Counts</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${productList?.description ?: "Add Central Count"}</li>
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
                                <g:select id="storeIdList"
                                          name="storeIdList"
                                          from="${availableStores}"
                                          multiple="true"
                                          value=""
                                          optionValue="storeName"
                                          optionKey="storeId"
                                          class="form-control col-8"
                                          style="height: 200px;"/>
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
                    <div class="col-1 font-weight-bold">Colour</div>
                    <div class="col-1 font-weight-bold">Size</div>
                    <div class="col-1 font-weight-bold">&nbsp;</div>
                </div>

                <div id="productList" class="align-content-center mb-5">
                    <g:if test="${!productList?.productListItems || productList?.productListItems?.size() == 0}">
                        <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${productList?.productListItems}" var="productListItem" status="i">
                        <g:render template="centralCountProductRow" model="[productVariant: productListItem.productVariant, i: i]" />
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

            $(function() {
                var $options = {
                    format: "dd/mm/yyyy",
                    weekStart: 1,
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                };

                $('#startDate').datepicker($options);
                $('#endDate').datepicker($options);
            });
        </script>
    </body>
</html>