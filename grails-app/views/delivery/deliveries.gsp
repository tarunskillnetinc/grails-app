<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

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
            <h2 id="page-title" class="mx-auto my-auto">Deliveries</h2>
        </div>

        <div class="col-3">
            <div class="container col-5 float-right">
                <div class="row justify-content-md-center float-right w-100">
                    <div class="col-md-auto w-100">
                        <button id="uploadDelivery" type="button" class="btn btn-wl mt-1 w-100"
                                onclick="window.location.href = '/store/add/addStoreButton'">Upload Delivery</button>
                    </div>
                </div>

                <div class="row float-right w-100">
                    <div class="col-md-auto w-50">
                        <button id="uploadDelivery1" type="button" class="btn btn-wl mt-1 green"
                                onclick="window.location.href = '/store/add/addStoreButton'">Import</button>
                    </div>

                    <div class="col-md-auto w-50">
                        <button id="uploadDelivery2" type="button" class="btn btn-wl mt-1 red"
                                onclick="window.location.href = '/store/add/addStoreButton'">Cancel</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section id="tills-container" class="container-fluid mb-3">
    <div id="results-container">
        <g:render template="deliveryImportResults"/>
    </div>
</section>

<section id="addProduct-section" class="container-fluid mt-4">
    <div class="col-12">
        <g:uploadForm name="submission-form" action="save" params="[id: button?.id, buttonGridId: button?.buttonGrid?.id, row: button?.row, column: button?.column]">

            <input id="image" name="image" type="file" accept="image/png" hidden/>
        </g:uploadForm>
    </div>
</section>
</body>
</html>