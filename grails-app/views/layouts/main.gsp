<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <title>
        <g:layoutTitle default="Well Pharmacy" />
    </title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />

    <asset:stylesheet src="application.css" />

    <g:layoutHead />
</head>

<body>
    <div class="d-flex align-items-center topbar">
        <div class="flex-grow-1">
            <g:link url="/">
                <asset:image src="well_logo.png" class="topbar-logo" />
            </g:link>
        </div>

        <div class="mr-5">Site: 3480</div>

        <div class="mr-5"><asset:image src="user_icon.png" />&nbsp;Admin</div>
    </div>

    <ul class="nav justify-content-center nav-wl align-items-center">
        <li class="vertical-line"></li>

        <li class="nav-item">
            <g:link url="/" class="nav-link active">Home</g:link>
        </li>

        <li class="vertical-line"></li>

        <li class="nav-item">
            <g:link controller="user" class="nav-link">User Management</g:link>
        </li>

        <li class="vertical-line"></li>

        <li class="nav-item">
            <g:link controller="tillSettings" class="nav-link">EPOS</g:link>
        </li>

        <li class="vertical-line"></li>
    </ul>

    <g:layoutBody/>

    <div id="spinner" class="spinner" style="display:none;">
        <g:message code="spinner.alt" default="Loading&hellip;"/>
    </div>

    <asset:javascript src="application.js" />
</body>
</html>