<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Deliveries</title>

    <script type="text/javascript">
        const urlFileReader = new FileReader()
        $(function ($) {
            urlFileReader.onload = onFileRead
            $("#image").on("change", onFileUpload)
        })

        function onFileUpload() {
            const fileType = this.files[0].type
            if (fileType !== "text/csv" && fileType !== "application/vnd.ms-excel") {
                alert('${message(code:'button.error.incompatible.message', args:['.csv'], default:"Incorrect file type. Please use .csv")}')
                return
            }

            const fileData = this.files[0];
            if (!FileReader || !fileData) {
                // fallback?
                return
            }

            urlFileReader.readAsText(fileData)
        }

        function onFileRead() {
            const csv = urlFileReader.result
            console.log(csv) // TODO: Remove debug logging

            const rowsArray = csv.split("\n")
            const csvArray = []

            for (const row of rowsArray) {
                const rowValues = row.split(",")

                csvArray.push({
                    "supplierReference": rowValues[0],
                    "bool": rowValues[1]
                })
            }

            for (const entry of csvArray) {
                checkCSVEntry(entry)
                break // TODO: Temporary debug break
            }
        }

        function checkCSVEntry(entry) {
            $.ajax({
                url: "${createLink(controller: 'delivery', action: 'ajaxCheckValidDelivery')}",
                method: "POST",
                data: entry,
                statusCode: {
                    500: function (response) {
                        console.log(response.error)
                        /*var errorList = response.responseJSON.error;

                        if (errorList && errorList.length > 0) {
                            let errorHeader = "An error occured when attempting to save the offer."
                            let errorString = "";

                            errorList.forEach(function(errorMessage) {
                                errorString = errorString.concat("<li>" + errorMessage + "</li>");
                            });

                            $('#validation-errors').html("<ul class='no-bullets'>" + errorHeader + errorString + "\n</ul>");
                            $('#validation-errors').prop("hidden", false);
                        }*/
                    },
                    200: function (response) {
                        console.log(response.success)
                        if (response.success) {
                            for (const order in response.orderList) {
                                console.log(order)
                            }
                        }
                        /*var successMessage = "Loyalty Offer Saved Successfully";
                        var redirectUrl = '${createLink(controller: 'loyalty', action:'loyaltyOffers')}';
                        // Append success message as a query parameter
                        redirectUrl += '?successMessage=' + encodeURIComponent(successMessage);
                        // Redirect to the loyaltyOffers page with the success message
                        window.location.href = redirectUrl;*/
                    }
                }
            });
        }
    </script>
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
            <div class="row justify-content-end">
                <div class="col-md-auto">
                    <button id="uploadDelivery" type="button" class="btn btn-wl mt-1"
                            onclick="$('#image').click();">Upload Delivery</button>
                </div>
            </div>

            <div class="row justify-content-end">
                <div class="col-md-auto">
                    <button id="uploadDelivery1" type="button" class="btn btn-wl mt-1 green"
                            onclick="window.location.href = '/store/add/addStoreButton'">Import</button>
                    <button id="uploadDelivery2" type="button" class="btn btn-wl mt-1 red"
                            onclick="window.location.href = '/store/add/addStoreButton'">Cancel</button>
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
        <g:uploadForm name="submission-form" action="save"
                      params="[id: button?.id, buttonGridId: button?.buttonGrid?.id, row: button?.row, column: button?.column]">

            <input id="image" name="image" type="file" accept="text/csv,application/vnd.ms-excel" hidden/>
        </g:uploadForm>
    </div>
</section>
</body>
</html>