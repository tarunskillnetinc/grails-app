<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>
        <script type="text/javascript">
            function setRelevantVariant(val) {
                $('#relevantVariant').val(val);
            }

            $(document).ready(function () {
                $('#vatCode').change(function() {
                    var vatCode = $('#vatCode option:selected').attr("data-code");

                    var vatPercentageOverride = $('#vatPercentageOverride');
                    vatPercentageOverride.attr("readonly", vatCode !== 'O');
                    vatPercentageOverride.val("0.00");
                });

                $("#openPrice").change(function() {
                    $("#restrictions\\.minOpenPrice").attr("readonly", !this.checked);
                    $("#restrictions\\.maxOpenPrice").attr("readonly", !this.checked);
                });

                $("#restrictions\\.buyerIdRequired").change(function() {
                    $("#restrictions\\.buyerIdForced").prop("checked", false);
                    $("#restrictions\\.buyerIdForced").attr("disabled", !this.checked);
                    $("#restrictions\\.buyerAgeRestriction").val("");
                    $("#restrictions\\.buyerAgeRestriction").attr("readonly", !this.checked);
                    $("#restrictions\\.buyerChallengeAge").val("");
                    $("#restrictions\\.buyerChallengeAge").attr("readonly", !this.checked);
                    $("#restrictions\\.sellerAgeRestriction").val("");
                    $("#restrictions\\.sellerAgeRestriction").attr("readonly", !this.checked);
                });
            });
        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item" aria-current="page"><g:link controller="product" action="index">Product Search</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">${product?.itemCode ?: "Add Product"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <div class="row header-wl mt-3">
            <h2 class="mx-auto">Product Maintenance</h2>
        </div>

        <g:hasErrors bean="${product}">
            <div class="alert alert-danger alert-wl" role="alert">
                <g:renderErrors bean="${product}" as="list" />

                <g:hasErrors bean="${product.restrictions}">
                    <g:renderErrors bean="${product.restrictions}" as="list" />
                </g:hasErrors>

                <g:each in="${product.variants.findAll{it.storeId == storeId}}" var="variant" status="i">
                    <g:hasErrors bean="${variant}">
                        <div class="ml-3 pl-3 border">
                            Variant ${i+1}
                            <g:renderErrors bean="${variant}" as="list" />

                            <g:each in="${variant.barcodes}" var="barcode" status="j">
                                <g:hasErrors bean="${barcode}">
                                    <div class="ml-3">
                                        Barcode ${j+1}
                                        <g:renderErrors bean="${barcode}" as="list" />
                                    </div>
                                </g:hasErrors>
                            </g:each>
                        </div>
                    </g:hasErrors>
                </g:each>
            </div>
        </g:hasErrors>

        <g:if test="${flash.message}">
            <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
        </g:if>

        <section id="maintenance-section" class="container-fluid">
            <g:render template="maintenanceForm" model="[product: product,
                                            storeId: storeId,
                                            statusValues: statusValues,
                                            categoryValues: categoryValues,
                                            vatValues: vatValues,
                                            navlink: navlink,
                                            isNewProduct: isNewProduct]"/>
        </section>
    </body>
</html>