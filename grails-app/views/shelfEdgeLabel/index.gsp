<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Shelf Edge Labels</title>

        <asset:stylesheet href="radio.css" />

        <script type="text/javascript">
            $(document).ready(function () {
                getAdHocBatches();
                getScheduledBatches();
            });

            function getAdHocBatches() {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGetAdHocBatches')}";

                $("#ad-hoc-results").hide();
                $("#ad-hoc-loading-indicator").show();

                $.ajax({
                    url: url,
                    // data: { },
                    success: function(resp) {
                        $('#ad-hoc-container').html(resp);
                    }
                });
            }

            function getScheduledBatches() {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGetScheduledBatches')}";

                $("#scheduled-results").hide();
                $("#scheduled-loading-indicator").show();

                $.ajax({
                    url: url,
                    // data: { },
                    success: function(resp) {
                        $('#scheduled-container').html(resp);
                    }
                });
            }

            function printAdHocBatch(productListId, labelTemplateId) {
                var url = "${createLink(controller: 'shelfEdgeLabel', action: 'ajaxGenerateAdHocPdf')}";

                var params = { productListId: productListId, labelTemplateId: labelTemplateId, printProcess: 'SHELF_EDGE_LABEL_BATCH', printType: 'PDF' };

                window.location = url + "?" + $.param(params);

                // $.ajax({
                //     url: url,
                //     data: params,
                //     success: function(data) {
                //         var blob=new Blob([data]);
                //         var link=document.createElement('a');
                //         link.href=window.URL.createObjectURL(blob);
                //         link.download="SomePdf.pdf";
                //         link.click();
                //     }
                // });
            }
        </script>
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

                    <div class="row col-8 offset-2 mt-5 px-0 pb-2 table-wl bottom-border">
%{--                        <div class="col-1 font-weight-bold">&nbsp;</div>--}%
                        <div class="col-9 font-weight-bold">Description</div>
                        <div class="col-3 font-weight-bold">Label Count</div>
                    </div>

                    <div id="ad-hoc-container" class="align-content-center">
                        <g:render template="adHocResults" />
                    </div>

                    <div class="row col-8 offset-2">
                        <button class="btn btn-wl" onclick="printAdHocBatch(2731, 1);">Print</button>
                    </div>
                </div>

                <div class="col-6">
                    <div class="header-wl mt-3"><h3>Scheduled changes</h3></div>

                    <div class="row col-8 offset-2 mt-5 pb-2 table-wl bottom-border">
                        <div class="col-6 font-weight-bold">Effective Date</div>
                        <div class="col-3 font-weight-bold">Label Count</div>
                    </div>

                    <div id="scheduled-container" class="align-content-center">
                        <g:render template="scheduledResults" />
                    </div>
                </div>
            </div>
        </section>
    </body>
</html>