<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Shelf Edge Labels</title>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li class="breadcrumb-item active" aria-current="page">Shelf Edge Labels</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="central-count-search" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Shelf Edge Labels</h2>
            </div>

            <g:if test="${flash.message}">
                <div class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <div class="row mt-4 ml-0 mr-0">
                <div class="col-6">
                    <div class="header-wl mt-3"><h3>Ad-hoc batches</h3></div>

                    <div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
                        <div class="col-6 font-weight-bold">Description</div>
                        <div class="col-3 font-weight-bold">Label Count</div>
                    </div>

                    <div class="align-content-center">
                        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped0 hoverable pointer" title="Click to view.">
                            <div class="col-6">Damaged labels</div>
                            <div class="col-3">36</div>
                        </div>

                        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped1 hoverable pointer" title="Click to view.">
                            <div class="col-6">Incorrect prices</div>
                            <div class="col-3">11</div>
                        </div>
                    </div>
                </div>

                <div class="col-6">
                    <div class="header-wl mt-3"><h3>Scheduled batches</h3></div>

                    <div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
                        <div class="col-6 font-weight-bold">Effective Date</div>
                        <div class="col-3 font-weight-bold">Label Count</div>
                    </div>

                    <div class="align-content-center">
                        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped0 hoverable pointer" title="Click to view.">
                            <div class="col-6">${dates[2]}</div>
                            <div class="col-3">108</div>
                        </div>

                        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped1 hoverable pointer" title="Click to view.">
                            <div class="col-6">${dates[1]}</div>
                            <div class="col-3">56</div>
                        </div>

                        <div class="row col-8 offset-2 pt-2 pb-2 wl-striped0 hoverable pointer" title="Click to view.">
                            <div class="col-6">${dates[0]}</div>
                            <div class="col-3">219</div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>