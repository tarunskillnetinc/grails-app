<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Deliveries</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Deliveries</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <div class="col-6 offset-3">
                    <h2 id="page-title" class="mx-auto my-auto">Store Deliveries</h2>
                </div>

                <div class="col-md-auto">
                    <div class="container">
                        <div class="row justify-content-md-center">
                            <div class="col-md-auto w-100">
                                <button id="uploadDelivery" type="button" class="btn btn-wl mt-1 w-100" onclick="window.location.href='/store/add/addStoreButton'">Upload Delivery</button>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-auto">
                                <button id="uploadDelivery1" type="button" class="btn btn-wl mt-1" onclick="window.location.href='/store/add/addStoreButton'">Import</button>
                            </div>
                            <div class="col-md-auto">
                                <button id="uploadDelivery2" type="button" class="btn btn-wl mt-1" onclick="window.location.href='/store/add/addStoreButton'">Cancel</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <p>TODO</p>
        </section>
    </body>
</html>