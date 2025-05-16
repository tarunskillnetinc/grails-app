<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />
    <title>Inventory Management</title>

      <asset:javascript src="validators/input-validator.js" />
      <asset:javascript src="jquery-ui.js" />
      <asset:stylesheet src="jquery-ui.css" />
<script>
    function productSelected (id, itemCode, description) {
        var addProductUrl = "${createLink(controller: 'inventory', action: 'ajaxAddProduct')}";

        $.ajax({
            url: addProductUrl,
            data: { productId: id },
            success: function(resp) {
                let productList = $("#productList")
                let warningMessage = $('#warning-message')

                for (const element of productList.children()) {
                    if (element.id.toUpperCase() === "PRODUCTVARIANT" + id) {
                        if (warningMessage.length) {
                            warningMessage.text("Product has already been added.")
                            warningMessage.removeClass("hidden")
                        }
                        return
                    }
                }

                if (warningMessage.length) {
                    warningMessage.addClass("hidden")
                }

                productList.append(resp);

                $('#noResultsRow').hide();

                let i = productList.children().length - 1; // remove hidden noResultsRow
                let row = $('#productVariant' +id);
                row.addClass("wl-striped" +((i-1) % 2));
                row.find('#prod-0-id').attr("id", "prod-" + i + "-id");
                row.find('#prod-0-sku').attr("id", "prod-" + i + "-sku");
                row.find('#prod-0-description').attr("id", "prod-" + i + "-description");
                row.find('#prod-0-colour').attr("id", "prod-" + i + "-colour");
                row.find('#prod-0-size').attr("id", "prod-" + i + "-size");
                row.find('#prod-0-remove-btn').attr("id", "prod-" + i + "-remove-btn");
            }
        });
    }

    function removeProduct(productId) {
        $('#product' +productId).remove();

        var productList = $('#productList');

        if (productList.children().length === 1) {
            var noResultsRow = $('#noResultsRow');

            noResultsRow.removeClass("wl-striped0");
            noResultsRow.removeClass("wl-striped1");
            noResultsRow.addClass("wl-striped0");
            noResultsRow.show();
        } else {
            for (var i = 1 ; i <= productList.children().length ; i++) {
                var child = $('#productList>div:nth-child(' +i +')');

                child.removeClass("wl-striped0");
                child.removeClass("wl-striped1");

                child.addClass("wl-striped" +(i % 2));
            }
        }
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
                        <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link uri="/inventory/index">Inventory Management</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Create Stock Adjustment</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="store-management" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Store Stock Adjustment</h2>
            </div>

            <div class="col-3 text-right">
                <button id="cancelButton" type="button" class="btn btn-wl mt-1" onclick="">Cancel</button>
                <button id="saveButton" type="button" class="btn btn-wl mt-1" onclick="">Save</button>
            </div>
        </div>
    </section>

    <section id="alerts-container" class="container-fluid">
            <div class="alert alert-success alert-wl mx-0" role="alert" id="successMessage" style="display: none"></div>
            <div class="alert alert-danger alert-wl mx-0" role="alert" id="failureMessage" style="display: none"></div>
    </section>
    </section>

     <div class="row mx-5 pt-3 pb-2" style="display: flex; align-items: center;">
          <div class="col-12 text-right">
            <button id="addProductButton" type="button" class="btn btn-wl mt-1" data-toggle="modal" data-target="#productSearchModal">Add Product(s)</button>
            <button id="addStoreButton" type="button" class="btn btn-wl mt-1" onclick="">Add Store(s)</button>
          </div>
          </div>

     <div class="row mt-4 ml-0 mr-0 bottom-border">
           <div class="col my-auto font-weight-bold">Item Code</div>
               <div class="col my-auto font-weight-bold">Barcode</div>
               <div class="col my-auto font-weight-bold">Description</div>
               <div class="col my-auto font-weight-bold">Category</div>
               <div class="col my-auto  font-weight-bold">Current Quantity</div>
               <div class="col my-auto  font-weight-bold">Amended Quantity</div>
               <div class="col my-auto font-weight-bold">Total Number of Stores</div>
               <div class="col my-auto font-weight-bold">Action</div>
           </div>
           <div id="productList" class="align-content-center mb-5">
                               <g:if test="${(!productList?.productListItems || productList?.productListItems?.size() == 0) && (!unsavedVariants || unsavedVariants?.size() == 0) }">
                                   <div id="noResultsRow" class="col pt-2 pb-2 my-auto text-center wl-striped0">No products added.</div>
                               </g:if>

                               <g:each in="${productList?.productListItems}" var="productListItem" status="i">
                                   <g:render template="stockSearchResults" model="[productVariant: productListItem.productVariant, i: i]" />
                               </g:each>

                               <g:each in="${unsavedVariants}" var="productVariant" status="i">
                                   <g:render template="stockSearchResults" model="[productVariant: productVariant, i: i]" />
                               </g:each>
                           </div>

             <!-- Product search modal -->
             <g:render template="/product/productSearch" />


</body>
</html>