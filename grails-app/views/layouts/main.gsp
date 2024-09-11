<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="format-detection" content="telephone=no">

    <title>
        <g:layoutTitle default="Trust Retail" />
    </title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />

    <asset:stylesheet src="application.css" />
    <asset:javascript src="application.js" />

    <g:layoutHead />
</head>

<body>
    <div class="align-items-center topbar d-flex">
        <div class="col-12 col-sm-2">
            <g:link elementId="wl-logo-link" uri="/">
                <asset:image id="wl-logo-image" src="topbar_logo.png" class="topbar-logo" />
            </g:link>
        </div>

        <sec:ifLoggedIn>
            <div class="col-12 col-sm-7 col-lg-9 text-right">
                <span id="store-number" style="margin-right: 50px;">Store:&nbsp;
                    <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                        <sec:loggedInUserInfo field="storeNumber" />
                    </g:if>
                    <g:else>
                        Head Office
                    </g:else>
                </span>

                <asset:image src="user_icon.png" width="25" style="margin-right: 10px;" />

                <span id="user"><sec:loggedInUserInfo field="usersName" /></span>
            </div>
            <div class="col-12 col-sm-3 col-lg-1 text-right">
                <g:link elementId="logout-button" controller="logoff" class="btn btn-wl red">Log out</g:link>
            </div>
        </sec:ifLoggedIn>

        <sec:ifNotLoggedIn>
            <div class="col-12 col-sm-10 text-right px-0">
                <span id="website-version" class="mr-4">Website version: <g:meta name="info.app.version" /></span>
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
                        <g:link elementId="home-dropdown" uri="/" class="nav-link">Home</g:link>
                    </li>

                    <li class="nav-item">
                        <g:link elementId="users-dropdown" controller="user" class="nav-link">Users</g:link>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="eposDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">EPOS</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="eposDropdown">
                            <div class="dropdown-submenu">
                                <a tabindex="-1" href="#" class="dropdown-item dropdown-toggle" id="productsDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Products</a>

                                <div class="dropdown-menu" aria-labelledby="productsDropdown">
                                    <g:link elementId="product-maintenance-dropdown" controller="product" class="dropdown-item">Product Maintenance</g:link>

                                    <sec:ifAnyGranted roles='ROLE_ENGINEER,ROLE_HEAD_OFFICE'>
                                        <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                            <div class="dropdown-divider"></div>
                                            <g:link elementId="price-changes-dropdown" controller="product" action="prices" class="dropdown-item">Price Changes</g:link>
                                            <g:link elementId="product-ranging-dropdown" controller="product" action="ranges" class="dropdown-item">Product Ranging</g:link>
                                            <div class="dropdown-divider"></div>
                                            <g:link elementId="supplier-price-updates-dropdown" controller="product" action="supplierUpdates" class="dropdown-item">Supplier Price Updates</g:link>
                                        </g:if>
                                    </sec:ifAnyGranted>
                                </div>
                            </div>

                            <g:link elementId="promotions-dropdown" controller="promotion" class="dropdown-item">Promotions</g:link>

                            <div class="dropdown-submenu">
                                <a tabindex="-1" href="#" class="dropdown-item dropdown-toggle" id="buttonGridsDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Button Grids</a>
                                <g:set var="service" value="service" bean="springSecurityService" />
                                <div class="dropdown-menu" aria-labelledby="buttonGridsDropdown">
                                    <g:link elementId="sales-dropdown" controller="buttonGrid" action="show" params="[type: 'SALES']" class="dropdown-item">Sales</g:link>
                                    <g:link elementId="quicksell-dropdown" controller="buttonGrid" action="show" params="[type: 'QUICK_SELL']" class="dropdown-item">Quicksell</g:link>
                                    <g:link elementId="tender-dropdown" controller="buttonGrid" action="show" params="[type: 'TENDER']" class="dropdown-item">Tender</g:link>
                                    <g:if test="${service.principal.retailer.config.scoEnabled}">
                                        <g:link elementId="sco-quicksell-dropdown" controller="buttonGrid" action="show" params="[type: 'SCO_QUICK_SELL']" class="dropdown-item">Self Checkout</g:link>
                                    </g:if>
                                    <g:link elementId="manager-functions-dropdown" controller="buttonGrid" action="show" params="[type: 'MANAGER_FUNCTIONS']" class="dropdown-item">Manager Functions</g:link>
                                    <g:if test="${service.principal.retailer.config.scoEnabled}">
                                        <g:link controller="buttonGrid" action="show" params="[type: 'SCO_MANAGER_FUNCTIONS']" class="dropdown-item">SCO Manager Functions</g:link>
                                    </g:if>

                                    <div class="dropdown-divider"></div>

                                    <div class="quicksell-scrollbar" style="max-height: 280px; overflow-y: auto;">
                                        <g:quicksellMenu />
                                    </div>


                                    <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                        <div class="dropdown-divider"></div>
                                        <g:link elementId="add-page-dropdown" controller="buttonGrid" action="add" class="dropdown-item">+ Add Page</g:link>
                                    </g:if>
                                </div>
                            </div>

                            <g:link elementId="tags-dropdown" controller="tag" class="dropdown-item">Tags</g:link>
                            <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                                <g:link elementId="shelf-edge-labels-dropdown" controller="shelfEdgeLabel" class="dropdown-item">Shelf Edge Labels</g:link>
                            </g:if>

                            <g:link elementId="suppliers-dropdown" controller="supplier" class="dropdown-item">Suppliers</g:link>
                        </div>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="reportingDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Reporting</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="reportingDropdown">
                            <span id="sales-report" class="dropdown-header">Sales Reports</span>

                            <g:link elementId="department-sales-report-dropdown" controller="reporting" action="salesDepartment" class="dropdown-item">Department Sales Report</g:link>
                            <g:link elementId="category-sales-dropdown" controller="reporting" action="categorySales" class="dropdown-item">Category Sales</g:link>
                            <g:link elementId="product-sales-dropdown" controller="reporting" action="sales" class="dropdown-item">Product Sales</g:link>
                            <sec:ifAnyGranted roles='ROLE_ENGINEER,ROLE_HEAD_OFFICE'>
                                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                    <g:link elementId="paypoint-sales-dropdown" controller="reporting" action="paypointSales" class="dropdown-item">PayPoint Sales</g:link>
                                </g:if>
                            </sec:ifAnyGranted>
                            <g:link elementId="promotional-sales-dropdown" controller="reporting" action="promotionsGrouped" class="dropdown-item">Promotional Sales</g:link>
                            <sec:ifAnyGranted roles='ROLE_ENGINEER,ROLE_HEAD_OFFICE'>
                                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                    <g:link elementId="charity-donations-dropdown" controller="reporting" action="charityDonations" class="dropdown-item">Charity Donations</g:link>
                                </g:if>
                            </sec:ifAnyGranted>

                            <div class="dropdown-divider"></div>

                            <span id="other-reports" class="dropdown-header">Other Reports</span>

                            <g:link elementId="till-control-events-dropdown" controller="reporting" action="tillControlEvents" class="dropdown-item">Till Control Events</g:link>
                            <g:link elementId="receipt-viewer-dropdown" controller="receipt" action="index" class="dropdown-item">Receipt Viewer</g:link>
                            <g:link elementId="product-lists-report-dropdown" controller="reporting" action="productLists" class="dropdown-item">Product Lists Report</g:link>
                            <g:link elementId="orders-report-dropdown" controller="reporting" action="orders" class="dropdown-item">Orders Report</g:link>
                            <g:link elementId="deliveries-report-dropdown" controller="reporting" action="deliveries" class="dropdown-item">Deliveries Report</g:link>
                            <g:link elementId="tender-movements-dropdown" controller="reporting" action="tenderMovements" class="dropdown-item">Tender Movements</g:link>
                            <a id="stock-movement" class="dropdown-item disabled" href="#" tabindex="-1" aria-disabled="true">Stock Movements</a>
                            <a id="journal" class="dropdown-item disabled" href="#" tabindex="-1" aria-disabled="true">Journal</a>
                        </div>
                    </li>

            <sec:ifAnyGranted roles='ROLE_ENGINEER, ROLE_HEAD_OFFICE'>
                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="loyaltyDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Loyalty</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="loyaltyDropdown">
                            <g:link elementId="membership-management-dropdown" controller="loyalty" action="loyaltyMembers" class="dropdown-item">Membership Management</g:link>
                            <g:link elementId="loyalty-segment-maintenance-dropdown" controller="loyalty" action="loyaltySegment" class="dropdown-item">Loyalty Segment Management</g:link>
                            <g:link elementId="loyalty-segment-maintenance-dropdown" controller="loyalty" action="loyaltyOffers" class="dropdown-item">Loyalty Offer Management</g:link>
                        </div>
                    </li>
                </g:if>
            </sec:ifAnyGranted>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="administrationDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Administration</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="administrationDropdown">
                            <sec:ifAnyGranted roles='ROLE_ENGINEER, ROLE_HEAD_OFFICE'>
                                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                    <span id="estate-management" class="dropdown-header">Estate Management</span>

                                    <g:link elementId="stores-list-dropdown" controller="store" class="dropdown-item">Store Management</g:link>
                                    <g:link elementId="till-assignment-dropdown" controller="tillAssignment" class="dropdown-item">Till Management</g:link>
                                    <g:link elementId="user-groups-dropdown" controller="group" class="dropdown-item disabled">Store Hierarchy</g:link>

                                    <div class="dropdown-divider"></div>
                                </g:if>

                                <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                                    <g:link elementId="store-settings-dropdown" controller="store" action="config" class="dropdown-item">Store Configuration</g:link>
                                    <g:link elementId="cash-management-dropdown" controller="cashManagement" params="[storeId:sec.loggedInUserInfo(field: 'storeId'),isStoreLevelLogin:true]"  class="dropdown-item">Cash Managment</g:link>
                                </g:if>
                            </sec:ifAnyGranted>

                            <g:link elementId="supplier-affiliations-dropdown" controller="supplier" class="dropdown-item" action="subscriptions">Supplier Affiliations</g:link>

                            <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                                <g:link elementId="shift-management-dropdown" controller="shift" class="dropdown-item">Shift Management</g:link>
                            </g:if>

                            <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                <g:link elementId="central-counts-dropdown" controller="productList" class="dropdown-item">Central Counts</g:link>
                            </g:if>

                            <sec:ifAnyGranted roles='ROLE_ENGINEER, ROLE_HEAD_OFFICE'>
                                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                    <div class="dropdown-divider"></div>

                                    <span id="retailer-configuration" class="dropdown-header">Retailer Configuration</span>

                                    <g:link elementId="category-maintenance-dropdown" controller="category" class="dropdown-item">Departments & Categories</g:link>
                                    <g:link elementId="cash-management-dropdown" controller="cashManagement" class="dropdown-item">Cash Managment</g:link>
                                    <g:link elementId="reason-code-dropdown" controller="reasonCode" class="dropdown-item">Reason Codes</g:link>
                                    <g:link elementId="user-groups-dropdown" controller="barcodeConfig" class="dropdown-item">Barcode Configuration</g:link>
                                </g:if>
                            </sec:ifAnyGranted>

                            <sec:ifAnyGranted roles='ROLE_ENGINEER'>
                                <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                    <div class="dropdown-divider"></div>

                                    <span id="engineer-functions" class="dropdown-header">Engineer Functions</span>

                                    <g:link elementId="retailer-settings-dropdown" controller="retailer" class="dropdown-item">Retailer Configuration</g:link>
                                    <g:link elementId="hardware-import-dropdown" controller="hardwareImport" class="dropdown-item">Hardware Import</g:link>
                                </g:if>
                            </sec:ifAnyGranted>
                        </div>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="monitoringDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Monitoring</a>

                        <div class="dropdown-menu dropdown-menu-right" aria-labelledby="monitoringDropdown">
                            <g:link elementId="till-connectivity-dropdown" controller="monitoring" action="tillConnectivity" class="dropdown-item">Till Connectivity</g:link>
                            <g:link elementId="transaction-service-status-dropdown" controller="monitoring" action="transactionServiceStatus" class="dropdown-item">Transaction Service Status</g:link>
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
