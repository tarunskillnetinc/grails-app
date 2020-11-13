<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Central Count Management</title>
    </head>

    <body>
        <section id="central-count-search" class="container-fluid">
            <div class="row header-wl">
                <h2 class="mx-auto">Central Count Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <g:hasErrors bean="${productList}">
                <div class="alert alert-danger alert-wl" role="alert">
                    <g:renderErrors bean="${productList}" as="list" />
                </div>
            </g:hasErrors>

            <g:form name="central-count-form" action="saveCentralCount" novalidate="novalidate" class="mt-4">
                <g:hiddenField name="id" value="${productList?.id}" />

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                    <g:textField name="description" class="col-5 form-control bottom-border" value="${productList?.description}" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="startDate" class="col-4 col-form-label text-right pr-4">Start Date</label>

                    <g:textField name="startDate" type="text" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy", date:productList?.startDate)}" autocomplete="off" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="endDate" class="col-4 col-form-label text-right pr-4">End Date</label>
                    <g:textField name="endDate" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy", date:productList?.endDate)}" autocomplete="off" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <div class="offset-lg-4">
                        <g:link action="listCentralCounts" role="button" class="btn btn-danger">Cancel</g:link>

                        <g:submitButton class="btn btn-success" name="save" value="Save" />
                    </div>
                </div>

                <div class="row header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row mt-4 ml-0 mr-0">
                    <div class="col-2 offset-10 text-right">
                        <!-- Button trigger modal -->
                        <a href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
                            Add Product
                        </a>
                    </div>
                </div>

                <div class="row mt-4 mb-2 ml-0 mr-0">
                    <div class="col-1 font-weight-bold">Product ID</div>
                    <div class="col-2 font-weight-bold">Item Code</div>
                    <div class="col font-weight-bold">Description</div>
                    <div class="col-1 font-weight-bold">Colour</div>
                    <div class="col-1 font-weight-bold">Size</div>
                    <div class="col-1 font-weight-bold">&nbsp;</div>
                </div>

                <div id="productList" class="align-content-center mb-5">
                    <g:if test="${!productList?.productListItems || productList?.productListItems?.size() == 0}">
                        <div id="noResultsRow" class="col pt-2 text-center">No products added.</div>
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