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
        <h1>Button</h1>

        <h5>ID</h5>
        <p>${button.id}</p>

        <h5>Type</h5>
        <p>${button.type}</p>

        <h5>Row</h5>
        <p>${button.row}</p>

        <h5>Column</h5>
        <p>${button.column}</p>

        <h5>Description</h5>
        <p>${button.description}</p>

        <h5>Amount</h5>
        <p>${button.amount}</p>

        <h5>Quantity</h5>
        <p>${button.quantity}</p>

        <h5>Product ID</h5>
        <p>${button.productId}</p>

        <h5>Process</h5>
        <p>${button.process}</p>

        <h5>Tender Type</h5>
        <p>${button.tenderType}</p>

        <g:link action="edit" id="${button.id}" class="btn btn-default">Edit</g:link>
    </div>
</div>

</body>
</html>
