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
                                            navlink: navlink]"/>
        </section>
    </body>
</html>