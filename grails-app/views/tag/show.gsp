<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Tag Management</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item" aria-current="page"><g:link controller="tag" action="index">Tag Management</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${tag?.description ?: "View Tag"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Tag Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${tag}">
                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="id" class="col-4 col-form-label text-right pr-4">ID</label>
                    <g:textField name="id" class="col-5 form-control bottom-border" value="${tag?.id}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                    <g:textField name="description" class="col-5 form-control bottom-border" value="${tag?.description}" disabled="disabled" />
                </div>

                <div class="header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
                    <div class="col-2 font-weight-bold">Product ID</div>
                    <div class="col-4 font-weight-bold">Item Code</div>
                    <div class="col-6 font-weight-bold">Description</div>
                </div>

                <div id="search-results" class="align-content-center mb-5">
                    <g:if test="${!tag.products || tag?.products?.size() == 0}">
                        <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${tag.products}" var="product" status="i">
                        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped${i%2}">
                            <div class="col-2">${product.id}</div>
                            <div class="col-4">${product.itemCode}</div>
                            <div class="col-6">${product.description}</div>
                        </div>
                    </g:each>
                </div>
            </g:if>
        </section>
    </body>
</html>