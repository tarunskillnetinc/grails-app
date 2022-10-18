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
                        <li class="breadcrumb-item active" aria-current="page">Home</li>
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
                <h2>Welcome to WonderLane!</h2>
            </div>
        </div>

        <div class="row mt-4">
            <p class="col-12 text-center">Quick access functionality below.</p>
        </div>

        <div class="row mt-5">
            <div class="col-2 offset-3">
                <g:link controller="product" class="unstyled-link">
                    <div class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3>Products</h3>
                        </div>
                    </div>
                </g:link>
            </div>
            <div class="col-2">
                <g:link controller="promotion" class="unstyled-link">
                    <div class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3>Promotions</h3>
                        </div>
                    </div>
                </g:link>
            </div>
            <div class="col-2">
                <g:link controller="buttonGrid" action="show" params="[type: 'SALES']" class="unstyled-link">
                    <div class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3>Buttons</h3>
                        </div>
                    </div>
                </g:link>
            </div>
        </div>

        <div class="row mt-5">
            <div class="col-2 offset-3">
                <g:if test="${sec.loggedInUserInfo(field: 'storeId')}">
                    <g:link controller="shift" class="unstyled-link">
                        <div class="card bg-light border-wl mx-1 py-5">
                            <div class="card-body text-center">
                                <h3>Cash</h3>
                            </div>
                        </div>
                    </g:link>
                </g:if>
                <g:else>
                    <div class="card bg-light border-wl-disabled mx-1 py-5">
                        <div class="card-body text-center">
                            <h3 style="color: #A9A9A9;">Cash</h3>
                        </div>
                    </div>
                </g:else>
            </div>
            <div class="col-2">
                <g:link controller="reporting" action="salesDepartment" class="unstyled-link">
                    <div class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3>Reporting</h3>
                        </div>
                    </div>
                </g:link>
            </div>
            <div class="col-2">
                <g:link controller="monitoring" action="tillConnectivity" class="unstyled-link">
                    <div class="card bg-light border-wl mx-1 py-5">
                        <div class="card-body text-center">
                            <h3>Monitoring</h3>
                        </div>
                    </div>
                </g:link>
            </div>
        </div>
    </section>
</body>
</html>