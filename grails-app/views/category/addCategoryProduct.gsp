<%@ page import="grails.converters.JSON" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Add Category Products</title>

    <script type="application/javascript">
        function resetForm() {
            document.getElementById('productSearchTerm').value = null;
            document.getElementById('productSearchBy').value = 'everything';
        }
        
        function search() {
            let url = "${createLink(action: 'ajaxCategoryProductSearch')}";
            let searchTerm = $('#productSearchTerm').val();
            let searchBy = $('#productSearchBy').val();
            let categoryId = ${category.id};

            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: url,
                data: { searchTerm: searchTerm, searchBy: searchBy, categoryId: categoryId },
                success: function(resp) {
                    $('#results-container').html(resp);
                    $('#descriptionSearchTerm').data('prev',$('#descriptionSearchTerm').val())
                    $('#barcodeSearchTerm').data('prev', $('#barcodeSearchTerm').val())
                    $('#itemCodeSearchTerm').data('prev', $('#itemCodeSearchTerm').val())
                }
            });
        }
        
        function toggleProductSelection(productId) {
            $.ajax({
                url: "${createLink(action: 'ajaxToggleProductId')}",
                method: "POST",
                data: { productId: productId },
                success: function(resp) { }
            });
        }
    </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="category" action="index">Category Management</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Category Product Mapping</li>
                        <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page"><g:link action="categoryProductMapping" params="[id: category.id]">${category.description}</g:link></li>
                        <li id="breadcrumb-5" class="breadcrumb-item active" aria-current="page">Add Products</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="addCategoryProduct" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Category Product Mapping</h2>
            </div>
            <div class="col-3 text-right d-inline-flex flex-row justify-content-end">
                <g:link elementId="category-product-cancel" action="index" class="col-3 btn btn-wl p-2" onClick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
                <g:link elementId="add-category-product" action="categoryProductMapping" params="[id: category.id]" class="col-3 btn btn-success p-2 ml-2">Add</g:link>
            </div>
        </div>
        <div class="row mt-4">
            <div class="col-6">
                <div class="card bg-light border-wl">
                    <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                        <div class="row">
                            <div id="filters-header" class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse show" id="filterCollapse">
                        <div class="form-group row">
                            <label for="productSearchTerm" class="col-2 col-form-label-sm text-right">Search Term:</label>
                            <div class="col-10 input-group">
                                <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" value="${session.CATEGORY_PRODUCT_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />
                                <div class="input-group-append">
                                    <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode', 'barcode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                                </div>
                            </div>
                        </div>

                        <div class="form-group row">
                            <div class="col-sm-8 col-xl-6 offset-sm-4 offset-xl-6 text-right">
                                <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="search()">Search</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="results-container" class="align-content-center">
            <g:render template="categoryProductSearchResults" />
        </div>
    </section>
</body>
</html>