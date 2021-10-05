<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Central Count Management</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item" aria-current="page"><g:link controller="tag" action="index">Tag Management</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${tag?.description ?: "Add Tag"}</li>
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
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <g:hasErrors bean="${tag}">
                <div class="alert alert-danger alert-wl mx-0" role="alert">
                    <g:renderErrors bean="${tag}" as="list" />
                </div>
            </g:hasErrors>

            <g:form name="central-count-form" action="save" novalidate="novalidate" class="mt-4">
                <g:hiddenField name="id" value="${tag?.id}" />

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                    <g:textField name="description" class="col-6 form-control bottom-border" value="${tag?.description}" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <div class="offset-lg-4">
                        <g:link action="index" role="button" class="btn btn-danger">Cancel</g:link>

                        <g:submitButton class="btn btn-success" name="save" value="Save" />
                    </div>
                </div>

                <div class="header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row mt-4 mx-0">
                    <div class="col-2 offset-10 text-right px-0">
                        <!-- Button trigger modal -->
                        <a href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
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
                    <g:if test="${!tag?.products || tag?.products?.size() == 0}">
                        <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${tag?.products}" var="product" status="i">
                        <g:render template="tagProductRow" model="[product: product, i: i]" />
                    </g:each>
                </div>
            </g:form>
        </section>

        <!-- Product search modal -->
        <g:render template="/product/productSearch" />

        <asset:javascript src="tag.js" />

        <script type='text/javascript'>
            var addProductUrl = "${createLink(controller: 'tag', action: 'ajaxAddProduct')}";
        </script>
    </body>
</html>