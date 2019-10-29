<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Product Maintenance</title>
        <script type="text/javascript">
            function setRelevantVariant(val) {
                $('#relevantVariant').val(val)
            }
        </script>
    </head>

    <body>
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

        <g:render template="/nav/epos" model="[active: 'product']" />

        <section id="maintenance-section" class="container-fluid">
            <g:render template="maintenanceForm" model="[product: product,
                                            storeId: storeId,
                                            statusValues: statusValues,
                                            categoryValues: categoryValues,
                                            vatValues: vatValues,
                                            navlink: navlink,
                                            isNewProduct: false]"/>
        </section>
    </body>
</html>