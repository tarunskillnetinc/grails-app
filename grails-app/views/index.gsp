<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>WonderLane</title>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item active" aria-current="page">Home</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <g:if test="${flash.error}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">${flash.error}</div>
        </section>
    </g:if>

    <section id="main-container" class="container-fluid">
        <div class="row">
            <div class="col-12 header-wl">
                <h2 id="welcome-message-1">Welcome to WonderLane!</h2>
            </div>
        </div>

        <div class="row mt-4">
            <p id="welcome-message-2" class="col-12 text-center">Quick access functionality below.</p>
        </div>

        <div class="row mt-5">
            <div class="col-2 offset-3">
                <g:link controller="product" class="unstyled-link">
                    <div id="products-button" class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 id="products-text">Products</h3>
                        </div>
                    </div>
                </g:link>
            </div>
            <div class="col-2">
                <g:link controller="promotion" class="unstyled-link">
                    <div id="promotions-button" class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 id="promotions-text">Promotions</h3>
                        </div>
                    </div>
                </g:link>
            </div>
            <div class="col-2">
                <g:link controller="buttonGrid" action="show" params="[type: 'SALES']" class="unstyled-link">
                    <div id="buttons-button" class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 id="buttons-text">Buttons</h3>
                        </div>
                    </div>
                </g:link>
            </div>
        </div>

        <div class="row mt-5">
            <div class="col-2 offset-3">
                <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                    <g:link controller="shift" class="unstyled-link">
                        <div id="cash-button" class="card bg-light border-wl mx-1 py-5">
                            <div class="card-body text-center">
                                <h3 id="cash-text">Cash</h3>
                            </div>
                        </div>
                    </g:link>
                </g:if>
                <g:else>
                    <div id="cash-button" class="card bg-light border-wl-disabled mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 id="cash-text" style="color: #A9A9A9;">Cash</h3>
                        </div>
                    </div>
                </g:else>
            </div>
            <div class="col-2">
                <g:link controller="reporting" action="salesDepartment" class="unstyled-link">
                    <div id="reporting-button" class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 id="reporting-text">Reporting</h3>
                        </div>
                    </div>
                </g:link>
            </div>
            <div class="col-2">
                <g:link controller="monitoring" action="tillConnectivity" class="unstyled-link">
                    <div id="monitoring-button" class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 id="monitoring-text">Monitoring</h3>
                        </div>
                    </div>
                </g:link>
            </div>
        </div>
    </section>
</body>
</html>