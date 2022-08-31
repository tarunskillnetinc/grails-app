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
    <asset:javascript src="application.js" />

    <g:layoutHead />
</head>

<body>
    <div class="align-items-center topbar d-flex">
        <div class="col-12 col-sm-2">
            <g:link uri="/">
                <asset:image src="wl_logo_topbar.png" class="topbar-logo" />
            </g:link>
        </div>

        <sec:ifLoggedIn>
            <div class="col-12 col-sm-9 text-right">
                <span style="margin-right: 50px;">Store:&nbsp;
                    <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                        <sec:loggedInUserInfo field="storeNumber" />
                    </g:if>
                    <g:else>
                        Head Office
                    </g:else>
                </span>

                <asset:image src="user_icon.png" width="25" style="margin-right: 10px;" />

                <span><sec:loggedInUserInfo field="usersName" /></span>
            </div>
            <div class="col-12 col-sm-1 text-right">
                <g:link controller="logoff" class="btn btn-wl red">Log out</g:link>
            </div>
        </sec:ifLoggedIn>

        <sec:ifNotLoggedIn>
            <div class="col-12 col-sm-10 text-right px-0">
                <span class="mr-4">Website version: <g:meta name="info.app.version" /></span>
            </div>
        </sec:ifNotLoggedIn>
    </div>

    <sec:ifLoggedIn>
        <nav class="navbar navbar-expand-lg navbar-dark nav-wl">
            <button class="navbar-toggler m-auto" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav m-auto align-items-center">
                    <li class="nav-item">
                        <g:link uri="/" class="nav-link">Home</g:link>
                    </li>

                    <li class="nav-item">
                        <g:link controller="user" class="nav-link">Users</g:link>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="eposDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">EPOS</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="eposDropdown">
                            <div class="dropdown-submenu">
                                <a tabindex="-1" href="#" class="dropdown-item dropdown-toggle" id="productsDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Products</a>

                                <div class="dropdown-menu" aria-labelledby="productsDropdown">
                                    <g:link controller="product" class="dropdown-item">Product Maintenance</g:link>

                                    <sec:ifAnyGranted roles='ROLE_ENGINEER,ROLE_HEAD_OFFICE'>
                                        <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                            <div class="dropdown-divider"></div>
                                            <g:link controller="product" action="prices" class="dropdown-item">Price Changes</g:link>
                                            <g:link controller="product" action="ranges" class="dropdown-item">Product Ranging</g:link>
                                            <div class="dropdown-divider"></div>
                                            <g:link controller="product" action="supplierUpdates" class="dropdown-item">Supplier Price Updates</g:link>
                                        </g:if>
                                    </sec:ifAnyGranted>
                                </div>
                            </div>

                            <g:link controller="promotion" class="dropdown-item">Promotions</g:link>

                            <div class="dropdown-submenu">
                                <a tabindex="-1" href="#" class="dropdown-item dropdown-toggle" id="buttonGridsDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Button Grids</a>

                                <div class="dropdown-menu" aria-labelledby="buttonGridsDropdown">
                                    <g:link controller="buttonGrid" action="show" params="[type: 'SALES']" class="dropdown-item">Sales</g:link>
                                    <g:link controller="buttonGrid" action="show" params="[type: 'QUICK_SELL']" class="dropdown-item">Quicksell</g:link>
                                    <g:link controller="buttonGrid" action="show" params="[type: 'TENDER']" class="dropdown-item">Tender</g:link>
                                    <g:link controller="buttonGrid" action="show" params="[type: 'MANAGER_FUNCTIONS']" class="dropdown-item">Manager Functions</g:link>

                                    <div class="dropdown-divider"></div>

                                    <div class="quicksell-scrollbar" style="max-height: 280px; overflow-y: auto;">
                                        <g:quicksellMenu />
                                    </div>

                                    <div class="dropdown-divider"></div>

                                    <g:link controller="buttonGrid" action="add" class="dropdown-item">+ Add Page</g:link>
                                </div>
                            </div>

                            <g:link controller="tag" class="dropdown-item">Tags</g:link>
                            <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                                <g:link controller="shelfEdgeLabel" class="dropdown-item">Shelf Edge Labels</g:link>
                            </g:if>

                            <g:link controller="supplier" class="dropdown-item">Suppliers</g:link>
                        </div>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="reportingDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Reporting</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="reportingDropdown">
                            <span class="dropdown-header">Sales Reports</span>

                            <g:link controller="reporting" action="salesDepartment" class="dropdown-item">Department Sales Report</g:link>
                            <g:link controller="reporting" action="categorySales" class="dropdown-item">Category Sales</g:link>
                            <g:link controller="reporting" action="sales" class="dropdown-item">Product Sales</g:link>
                            <sec:ifAnyGranted roles='ROLE_ENGINEER,ROLE_HEAD_OFFICE'>
                                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                    <g:link controller="reporting" action="paypointSales" class="dropdown-item">PayPoint Sales</g:link>
                                </g:if>
                            </sec:ifAnyGranted>
                            <g:link controller="reporting" action="promotionsGrouped" class="dropdown-item">Promotional Sales</g:link>

                            <div class="dropdown-divider"></div>

                            <span class="dropdown-header">Other Reports</span>

                            <g:link controller="reporting" action="tillControlEvents" class="dropdown-item">Till Control Events</g:link>
                            <g:link controller="receipt" action="index" class="dropdown-item">Receipt Viewer</g:link>
                            <g:link controller="reporting" action="orders" class="dropdown-item">Orders Report</g:link>
                            <a class="dropdown-item disabled" href="#" tabindex="-1" aria-disabled="true">Tender Movement</a>
                            <a class="dropdown-item disabled" href="#" tabindex="-1" aria-disabled="true">Stock Movement</a>
                            <a class="dropdown-item disabled" href="#" tabindex="-1" aria-disabled="true">Journal</a>
                        </div>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="administrationDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Administration</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="administrationDropdown">
                            <g:link controller="storeSettings" class="dropdown-item">Store Settings</g:link>
                            <g:link controller="group" class="dropdown-item disabled">User Groups</g:link>
                            <g:link controller="supplier" class="dropdown-item" action="subscriptions">Supplier Affiliations</g:link>
                            <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                                <g:link controller="shift" class="dropdown-item">Shift Management</g:link>
                            </g:if>
                            <g:link controller="productList" class="dropdown-item disabled">Central Counts</g:link>
                        </div>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="monitoringDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Monitoring</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="monitoringDropdown">
                            <g:link controller="monitoring" action="tillConnectivity" class="dropdown-item">Till Connectivity</g:link>
                            <g:link controller="monitoring" action="transactionServiceStatus" class="dropdown-item">Transaction Service Status</g:link>
                        </div>
                    </li>

                </ul>
            </div>
        </nav>
    </sec:ifLoggedIn>

    <g:layoutBody />

    <div id="spinner" class="spinner" style="display:none;">
        <g:message code="spinner.alt" default="Loading&hellip;"/>
    </div>
</body>
</html>