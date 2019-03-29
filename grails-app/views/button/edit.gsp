<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.ProcessType" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.TenderType" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Button Grids</title>
</head>
<body>
<content tag="nav">
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Application Status <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <li class="dropdown-item"><a href="#">Environment: ${grails.util.Environment.current.name}</a></li>
            <li class="dropdown-item"><a href="#">App profile: ${grailsApplication.config.grails?.profile}</a></li>
            <li class="dropdown-item"><a href="#">App version:
                <g:meta name="info.app.version"/></a>
            </li>
            <li role="separator" class="dropdown-divider"></li>
            <li class="dropdown-item"><a href="#">Grails version:
                <g:meta name="info.app.grailsVersion"/></a>
            </li>
            <li class="dropdown-item"><a href="#">Groovy version: ${GroovySystem.getVersion()}</a></li>
            <li class="dropdown-item"><a href="#">JVM version: ${System.getProperty('java.version')}</a></li>
            <li role="separator" class="dropdown-divider"></li>
            <li class="dropdown-item"><a href="#">Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</a></li>
        </ul>
    </li>
</content>

<div class="svg" role="presentation">
    <div class="grails-logo-container">
        <asset:image src="grails-cupsonly-logo-white.svg" class="grails-logo"/>
    </div>
</div>

<div id="content" role="main">
    <div style="text-align: center;">
        <h1>Edit Button</h1>

        <g:form name="save-button" action="save">
            <g:hiddenField name="id" value="${button?.id}" />
            <g:hiddenField name="buttonGrid.id" value="${button?.buttonGrid?.id}" />
            <g:hiddenField name="retailerId" value="1" />
            <g:hiddenField name="storeId" value="1" />

            <h5>Type</h5>
            <p><g:select name="type" from="${ButtonType}" value="${button.type}" noSelection="['':'-Please select-']"/></p>

            <h5>Row</h5>
            <p><g:textField name="row" value="${button?.row}" /></p>

            <h5>Column</h5>
            <p><g:textField name="column" value="${button?.column}" /></p>

            <h5>Description</h5>
            <p><g:textField name="description" value="${button?.description}" /></p>

            <h5>Amount</h5>
            <p><g:textField name="amount" value="${button?.amount}" /></p>

            <h5>Quantity</h5>
            <p><g:textField name="quantity" value="${button?.quantity}" /></p>

            <h5>Product ID</h5>
            <p><g:textField name="productId" value="${button?.productId}" /></p>

            <h5>Process</h5>
            <p><g:select name="process" from="${ProcessType}" value="${button.process}" noSelection="['':'']" /></p>

            <h5>Tender Type</h5>
            <p><g:select name="type" from="${TenderType}" value="${button.tenderType}" noSelection="['':'']" /></p>

            <g:submitButton name="save" value="Save" />
        </g:form>
    </div>
</div>

</body>
</html>
