<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Product Group Management</title>
        <asset:javascript src="co-utils.js"/>
        <asset:javascript src="validators/input-validator.js" />

        <script>
            $(function() {
                intListener("maxSellQuantity", 10, 999);
            });
        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="productGroup"
                                                                                  action="index">Product Group Management</g:link></li>
                            <g:if test="${params.action == 'edit'}">
                                <li id="breadcrumb-3" class="breadcrumb-item"><g:link controller="productGroup"
                                                                                      action="show"
                                                                                      id="${productGroup.id}">${productGroup.description}</g:link></li>
                                <li id="breadcrumb-4" class="breadcrumb-item active"
                                    aria-current="page">${productGroup?.description ? "Edit Product Group" : "Add Product Group"}</li>
                            </g:if>
                            <g:else>
                                <li id="breadcrumb-3" class="breadcrumb-item active"
                                    aria-current="page">${productGroup?.description ? "Edit Product Group" : "Add Product Group"}</li>
                            </g:else>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
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

            <g:form name="tag-form" action="save" novalidate="novalidate" class="mt-4">
                <g:hiddenField name="id" value="${productGroup?.id ?: 0}"/>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                    <g:textField name="description" class="col-8 form-control bottom-border"
                                 value="${productGroup?.description}" maxlength="50"/>
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="maxSellQuantity" class="col-4 col-form-label text-right pr-4">Maximum Sell Quantity</label>
                    <g:field name="maxSellQuantity" type="number" min="0" max="999"
                             value="${productGroup?.maxSellQuantity}" class="col-2 form-control bottom-border"
                             onkeypress="return preventNegativeInteger(event);" onpaste="return false;"/>
                </div>

                <div class="header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row mt-4 mx-0">
                    <div class="col-2 offset-8 text-right px-0">
                        <!-- Button trigger modal -->
                        <a id="add-product-btn" href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
                            Add Product
                        </a>
                    </div>
                </div>

                <div class="row col-8 offset-2 mt-4 table-wl bottom-border">
                    <div class="col-2 font-weight-bold">Item Code</div>
                    <div class="col-3 font-weight-bold">SKU</div>
                    <div class="col font-weight-bold">Description</div>
                    <div class="col-1 font-weight-bold">&nbsp;</div>
                </div>

                <div id="productList" class="align-content-center mb-5">
                    <g:if test="${!productGroup?.productGroupProducts || productGroup?.productGroupProducts?.size() == 0}">
                        <div id="noResultsRow" class="col-8 offset-2 pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${productGroup?.productGroupProducts?.sort { it.sku }}" var="productGroupProduct"
                            status="i">
                        <g:render template="productGroupProductRow"
                                  model="[productGroupProduct: productGroupProduct, i: i]"/>
                    </g:each>
                </div>
            </g:form>
        </section>

        <!-- Product search modal -->
        <g:render template="/product/productSearch" />

        <asset:javascript src="tag.js" />

        <script type='text/javascript'>
        var addProductUrl = "${createLink(controller: 'productGroup', action: 'ajaxAddProduct')}";
        </script>
    </body>
</html>