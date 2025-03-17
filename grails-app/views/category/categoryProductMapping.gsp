<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main" />

  <title>Category Product Mapping</title>

    <asset:javascript src="jquery-ui.js" />

    <script type="application/javascript">
        $(document).ready(function() {
            function updateButtonWidth() {
                var totalWidth = 0;
                $('#button-container a').each(function() {
                    totalWidth += $(this).outerWidth(true);
                });
                
                $('#add-category-products').css('width', totalWidth + 'px');
            }
            
            updateButtonWidth();
            getProductMappings();
            
            $(window).resize(updateButtonWidth);
        });

        function getProductMappings() {
            let url = "${createLink(action: 'ajaxCategoryProductMapping')}";
            let categoryId = ${category.id};

            $("#search-results").hide();
            $("#loading-indicator").show();

            $.ajax({
                url: url,
                data: { id: categoryId, max: 50, offset: 0 },
                success: function(resp) {
                    $('#results-container').html(resp);
                }
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
              <li id="breadcrumb-4" class="breadcrumb-item active" aria-current="page">${category.description}</li>
            </ol>
          </div>
        </div>
      </nav>
    </section>

    <section id="categoryProductManagement" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Category Product Mapping</h2>
            </div>
            <div id="button-container" class="col-3 text-right d-inline-flex flex-row justify-content-end">
                <g:link elementId="category-product-cancel" action="index" class="col-3 btn btn-wl p-2" onClick="return confirm('Are you sure you want to cancel? All unsaved changes will be lost.');">Cancel</g:link>
                <g:link elementId="save-category-product" class="col-3 btn btn-success p-2 ml-2" action="ajaxSaveCategoryProducts" params="[id: category.id]" onClick="return confirm('Confirm changes. Are you sure you wish to save these changes?');">Save</g:link>
            </div>
        </div>

        <g:if test="${flash.message}">
            <div id="success-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </g:if>

        <g:if test="${flash.error}">
            <div id="error-message" class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
        </g:if>

        <div class="row mt-3">
            <div class="col-9">
                <h5>Category: ${category.description}</h5>
            </div>
            <div class="col-3 text-right d-inline-flex flex-row justify-content-end">
                <g:link elementId="add-category-products" class="btn btn-wl p-2" action="addCategoryProduct" params="[id: category.id]">Add Product(s)</g:link>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="categoryProductMappingResults" model="[products: products]" />
        </div>
    </section>
</body>
</html>