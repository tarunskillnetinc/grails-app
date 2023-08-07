<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Tag Management</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="tag" action="index">Tag Management</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${tag?.description ?: "View Tag"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="tag-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 id="page-title" class="mx-auto">Tag Management</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link elementId="edit-tag-btn" action="edit" id="${tag.id}" class="btn btn-wl">Edit Tag</g:link>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <div class="form-group row col-12 col-lg-6 mt-4">
                <label for="id" class="col-4 col-form-label text-right pr-4">ID</label>
                <g:textField name="id" class="col-5 form-control bottom-border" value="${tag.id}" disabled="disabled" />
            </div>

            <div class="form-group row col-12 col-lg-6 mt-4">
                <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                <g:textField name="description" class="col-8 form-control bottom-border" value="${tag.description}" disabled="disabled" />
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
                <g:if test="${!tag.tagProducts || tag.tagProducts?.size() == 0}">
                    <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                </g:if>

                <g:each in="${tag.tagProducts?.sort { it.sku }}" var="tagProduct" status="i">
                    <div id="tag-product-${i+1}" class="row col-8 offset-2 pt-2 pb-2 wl-striped${i%2}">
                        <div id="tag-product-${i+1}-id" class="col-2">${tagProduct.itemCode}</div>
                        <div id="tag-product-${i+1}-sku" class="col-4">${tagProduct.sku}</div>
                        <div id="tag-product-${i+1}-description" class="col-6">${tagProduct.productDescription}</div>
                    </div>
                </g:each>
            </div>
        </section>
    </body>
</html>