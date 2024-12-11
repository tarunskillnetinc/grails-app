<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Product Attributes</title>


    <script type="text/javascript">
        function submitForm(attributes) {

            var saveAttributeChangesUrl = "${createLink(controller: 'product', action: 'ajaxSaveProductAttributeChanges')}";


            console.log("Retailer Product Attributes - submitForm:")
            try{
                $.ajax({
                    url: saveAttributeChangesUrl,
                    data: params,
                    success: function(resp) {
                        //navigateToGroup(groupId, lowestLevel);
                    }
                });

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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Retailer Product Attributes</li>
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
                <g:link elementId="cancel-btn" controller="product" action="productAttributes" tabindex="-1" role="button" class="btn btn-wl ml-1">Cancel</g:link>
                <button id="save-btn" class="btn btn-success ml-1" name="save" onclick="submitForm('${ProductAttributes.get(0).name}');">Save</button>
            </div>
        </div>
        <div class="col-12 text-right mt-3">
            <div class="d-flex justify-content-end align-items-center">
                <g:link controller="product" action="addProductAttribute" tabindex="-1" role="button" class="btn btn-wl ml-1">Add Product Attribute</g:link>
            </div>
        </div>
    </div>

<div>

    <g:render template="productAttributesResultsView" />
</section>
</body>


</html>