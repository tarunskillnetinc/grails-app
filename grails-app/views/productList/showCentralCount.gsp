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
                        <ol class="breadcrumb flex-nowrap">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item text-truncate" aria-current="page"><g:link controller="productList" action="listCentralCounts">Central Counts</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active text-truncate" aria-current="page">${productList?.description ?: "View Central Count"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Central Count Management</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${productList}">
                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="id" class="col-4 col-form-label text-right pr-4">ID</label>
                    <g:textField name="id" class="col-5 form-control bottom-border" value="${productList?.id}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6">
                    <label for="type" class="col-4 col-form-label text-right pr-4">Type</label>
                    <g:select name="type" class="col-3 form-control select-border" from="${[productList?.type ?: ""]}" value="${productList?.type}" valueMessagePrefix="ProductListType" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="description" class="col-4 col-form-label text-right pr-4">Description</label>
                    <g:textField name="description" class="col-5 form-control bottom-border" value="${productList?.description}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="startDate" class="col-4 col-form-label text-right pr-4">Start Date</label>
                    <g:textField name="startDate" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy", date:productList?.startDate?.toDate())}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="endDate" class="col-4 col-form-label text-right pr-4">End Date</label>
                    <g:textField name="endDate" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy", date:productList?.endDate?.toDate())}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="status" class="col-4 col-form-label text-right pr-4">Status</label>
                    <g:textField name="status" class="col-5 form-control bottom-border" value="${g.message(code: 'ProductListStatus.' +productList?.status)}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="owner" class="col-4 col-form-label text-right pr-4">Owner</label>
                    <g:textField name="owner" class="col-5 form-control bottom-border" value="${productList?.ownerUsersName ?: 'N/A'} (${productList?.ownerUserId ?: 'N/A'})" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="dateStarted" class="col-4 col-form-label text-right pr-4">Date Started</label>
                    <g:textField name="dateStarted" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy HH:mm:ss", date:productList?.dateStarted?.toDate())}" disabled="disabled" />
                </div>

                <div class="form-group row col-12 col-lg-6 mt-4">
                    <label for="dateCompleted" class="col-4 col-form-label text-right pr-4">Date Completed</label>
                    <g:textField name="dateCompleted" class="col-5 form-control bottom-border" value="${g.formatDate(format:"dd/MM/yyyy HH:mm:ss", date:productList?.dateCompleted?.toDate())}" disabled="disabled" />
                </div>

                <div class="header-wl mt-5">
                    <h3 class="mx-auto">Products</h3>
                </div>

                <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
                    <div class="col-1 font-weight-bold">Product ID</div>
                    <div class="col-2 font-weight-bold">Item Code</div>
                    <div class="col font-weight-bold">Description</div>
                    <div class="col-1 font-weight-bold">Colour</div>
                    <div class="col-1 font-weight-bold">Size</div>
                </div>

                <div id="search-results" class="align-content-center mb-5">
                    <g:if test="${!productList.productListItems || productList?.productListItems?.size() == 0}">
                        <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                    </g:if>

                    <g:each in="${productList.productListItems}" var="productListItem" status="i">
                        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2}">
                            <div class="col-1">${productListItem.productVariant?.product?.id}</div>
                            <div class="col-2">${productListItem.productVariant?.sku}</div>
                            <div class="col">${productListItem.productVariant?.product?.description}</div>
                            <div class="col-1">${productListItem.productVariant?.colour ?: 'N/A'}</div>
                            <div class="col-1">${productListItem.productVariant?.size ?: 'N/A'}</div>
                        </div>
                    </g:each>
                </div>
            </g:if>
        </section>
    </body>
</html>