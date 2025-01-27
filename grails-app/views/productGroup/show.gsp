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

            <div class="form-group row col-12 col-lg-6 mt-4">
                <label for="id" class="col-4 col-form-label text-right pr-4">ID</label>
                <g:textField name="id" class="col-5 form-control bottom-border" value="${productGroup.id}"
                             disabled="disabled"/>
            </div>

            <div class="form-group row col-12 col-lg-6 mt-4">
                <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                <g:textField name="description" class="col-8 form-control bottom-border"
                             value="${productGroup?.description}" disabled="disabled"/>
            </div>

            <div class="form-group row col-12 col-lg-6 mt-4">
                <label for="maxSellQuantity" class="col-4 col-form-label text-right pr-4">Maximum Sell Quantity</label>
                <g:textField name="maxSellQuantity" class="col-2 form-control bottom-border"
                             value="${productGroup?.maxSellQuantity}" disabled="disabled"/>
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