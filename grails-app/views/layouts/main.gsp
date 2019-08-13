<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="format-detection" content="telephone=no">

    <title>
        <g:layoutTitle default="WonderLane" />
    </title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />

    <asset:stylesheet src="application.css" />

    <g:layoutHead />
</head>

<body>
    <div class="row align-items-center topbar">
        <div class="col-12 col-sm-2">
            <g:link url="/">
                <asset:image src="wl_logo_transparent.png" class="topbar-logo" />
            </g:link>
        </div>

        <sec:ifLoggedIn>
            <div class="col-12 col-sm-9 text-right">
                <span style="margin-right: 50px;">Store:&nbsp;<sec:loggedInUserInfo field="storeId" /></span>

                <asset:image src="user_icon.png" width="25" style="margin-right: 10px;" />

                <span><sec:loggedInUserInfo field="usersName" /></span>
            </div>
            <div class="col-12 col-sm-1 text-right">
                <g:link controller="logoff" class="btn btn-danger" style="margin-right: 21px;">Log out</g:link>
            </div>
        </sec:ifLoggedIn>
    </div>

    <sec:ifLoggedIn>
        <nav class="navbar navbar-expand-lg navbar-dark nav-wl">
            <button class="navbar-toggler m-auto" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav m-auto align-items-center">
                    <li class="vertical-line d-md-none d-lg-block"></li>

                    <li class="nav-item">
                        <g:link url="/" class="nav-link">Home</g:link>
                    </li>

                    <li class="vertical-line d-md-none d-lg-block"></li>

                    <li class="nav-item">
                        <g:link url="/" class="nav-link">User Management</g:link>
                    </li>

                    <li class="vertical-line d-md-none d-lg-block"></li>

                    <li class="nav-item">
                        <g:link controller="storeSettings" class="nav-link">EPOS</g:link>
                    </li>

                    <li class="vertical-line d-md-none d-lg-block"></li>
                </ul>
            </div>
        </nav>
    </sec:ifLoggedIn>

    <g:layoutBody/>

    <div id="spinner" class="spinner" style="display:none;">
        <g:message code="spinner.alt" default="Loading&hellip;"/>
    </div>

    <asset:javascript src="application.js" />
</body>
</html>