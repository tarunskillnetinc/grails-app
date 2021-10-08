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
                            <li class="breadcrumb-item"><g:link controller="tag" action="index">Tag Management</g:link></li>
                            <g:if test="${params.action == 'edit'}">
                                <li class="breadcrumb-item"><g:link controller="tag" action="show" id="${tag.id}">${tag.description}</g:link></li>
                            </g:if>
                            <li class="breadcrumb-item active" aria-current="page">${tag?.description ? "Edit Tag" : "Add Tag"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-8 offset-2">
                    <h2 class="mx-auto">Tag Management</h2>
                </div>

                <div class="col-2 text-right">
                    <g:link action="${params.action == 'edit' ? 'show' : 'index'}" id="${tag?.id}" role="button" class="btn btn-danger">Cancel</g:link>

                    <button class="btn btn-success" name="save" onclick="$('#tag-form').submit();">Save</button>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <g:hasErrors bean="${tag}">
                <div class="alert alert-danger alert-wl mx-0" role="alert">
                    <g:renderErrors bean="${tag}" as="list" />
                </div>
            </g:hasErrors>

            <g:form name="tag-form" action="save" novalidate="novalidate" class="mt-4">
                <g:hiddenField name="id" value="${tag?.id}" />

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="tagId" class="col-4 col-form-label text-right pr-4">ID</label>
                    <g:textField name="tagId" class="col-5 form-control bottom-border" value="${tag?.id}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                    <g:textField name="description" class="col-8 form-control bottom-border" value="${tag?.description}" />
                </div>

                <div class="header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row mt-4 mx-0">
                    <div class="col-2 offset-8 text-right px-0">
                        <!-- Button trigger modal -->
                        <a href="#" class="btn btn-wl" data-toggle="modal" data-target="#productSearchModal">
                            Add Product
                        </a>
                    </div>
                </div>

                <div class="row col-8 offset-2 mt-4 table-wl bottom-border">
                    <div class="col-2 font-weight-bold">Product ID</div>
                    <div class="col-3 font-weight-bold">SKU</div>
                    <div class="col font-weight-bold">Description</div>
                    <div class="col-1 font-weight-bold">&nbsp;</div>
                </div>

                <div id="productList" class="align-content-center mb-5">
                    <g:if test="${!tag?.tagProducts || tag?.tagProducts?.size() == 0}">
                        <div id="noResultsRow" class="col-8 offset-2 pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${tag?.tagProducts?.sort { it.sku }}" var="tagProduct" status="i">
                        <g:render template="tagProductRow" model="[tagProduct: tagProduct, i: i]" />
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