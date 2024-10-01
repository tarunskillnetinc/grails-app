<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Retailer Settings</title>

    <asset:stylesheet src="multi-select-checks.css" />

    <asset:javascript src="validators/input-validator.js" />
    <asset:javascript src="popper.min.js" />
    <asset:javascript src="multi-select-checks.js" />
    <asset:javascript src="money-mask.js" />

</head>
<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Cash Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>
<g:render template="cashManagementTemp" model='[config:config,onlyRetailerLevel:onlyRetailerLevel]'/>
</body>
</html>