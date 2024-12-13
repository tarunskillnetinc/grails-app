<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Retailer Product Attributes</title>

    <style>
    .checkbox-container {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .checkbox-container::before {
        content: '';
        display: inline-block;
        height: 100%;
        vertical-align: middle;
    }

    .checkbox-container .wl-checkbox {
        margin: 0;
        vertical-align: middle;
    }
    </style>

    <script type="text/javascript">
        function submitForm(attributeName) {
            var saveAttributeChangesUrl = "${createLink(controller: 'product', action: 'ajaxSaveProductAttributeChanges')}";

            console.log("Retailer Product Attributes - submitForm:")
            try {
                // Only proceed if there's an attribute name
                if (attributeName) {
                    $.ajax({
                        url: saveAttributeChangesUrl,
                        data: {name: attributeName},  // Pass the attribute name
                        success: function (resp) {
                            console.log("Changes saved successfully");
                            // You might want to refresh the page or show a success message here
                        },
                        error: function (xhr, status, error) {
                            console.error("Error saving changes:", error);
                            // You might want to show an error message to the user here
                        }
                    });
                } else {
                    console.log("No attributes to save");
                    // You might want to show a message to the user that there's nothing to save
                }
            } catch (ex) {
                console.log("Exception:" + ex)
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
                    <li id="breadcrumb-2" class="breadcrumb-item active"
                        aria-current="page">Retailer Product Attributes</li>
                </ol>
            </div>
        </div>
    </nav>
</section>
<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-0">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto my-auto">Retailer Product Attributes</h2>
        </div>

        <div class="col-12 text-right mt-3">
            <div class="d-flex justify-content-end align-items-center">
                <g:link elementId="cancel-btn" controller="product" action="productAttributes" tabindex="-1"
                        role="button" class="btn btn-wl ml-1">Cancel</g:link>
                <button id="save-btn" class="btn btn-success ml-1" name="save"
                        onclick="submitForm('${productAttributes?.size() > 0 ? productAttributes?.get(0)?.name:''}');">Save</button>
            </div>
        </div>

        <div class="col-12 text-right mt-3">
            <div class="d-flex justify-content-end align-items-center">
                <g:link controller="product" action="addProductAttribute" tabindex="-1" role="button"
                        class="btn btn-wl ml-1">Add Product Attribute</g:link>
            </div>
        </div>
    </div>

    <div>
        <div id="results-container" class="align-content-center">
            <g:render template="productAttributesResultsView"
                      model="[productAttributes: productAttributes, productAttributesCount: productAttributesCount]"/>
        </div>
</section>
</body>

</html>