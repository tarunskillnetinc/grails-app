<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Deliveries</title>

    <script type="text/javascript">
        let orders = []
        const urlFileReader = new FileReader()
        $(function ($) {
            urlFileReader.onload = onFileRead
            $("#deliveries-file").on("change", onFileUpload)
        })

        function onFileUpload() {
            orders = []

            const fileType = this.files[0].type
            if (fileType !== "text/csv" && fileType !== "application/vnd.ms-excel") { // types here must match ones defined in the deliveries-file accept field below
                alert('${message(code:'button.error.incompatible.message', args:['.csv',''], default:"Incorrect file type. Please use .csv")}')
                return
            }

            const fileData = this.files[0]
            if (!FileReader || !fileData) {
                // fallback?
                return
            }

            urlFileReader.readAsText(fileData)
        }

        function onFileRead() {
            const csv = urlFileReader.result

            const rowsArray = csv.split("\n")
            const supplierReferences = []

            for (const row of rowsArray) {
                const rowValues = row.split(",")

                supplierReferences.push(rowValues[0])
            }

            $.ajax({
                url: "${createLink(controller: 'delivery', action: 'ajaxCheckValidDeliveries')}",
                method: "POST",
                data: {"supplierReferences": JSON.stringify(supplierReferences)},
                statusCode: {
                    500: function (response) {
                        alert("Unable to validate uploaded deliveries, please try again later.")
                    },
                    200: function (response) {
                        $('#results-container').html(response)
                    }
                }
            })
        }

        function onCancel() {
            $.ajax({
                url: "${createLink(controller: 'delivery', action: 'ajaxCancelDeliveries')}",
                method: "POST",
                statusCode: {
                    500: function (response) {
                        alert("Unable to cancel uploaded deliveries, please try again later.")
                    },
                    200: function (response) {
                        $('#results-container').html(response)
                        $("#deliveries-file").val('')
                    }
                }
            })
        }

        function onImport() {
            if (document.getElementById("no-rows-test") != null || document.getElementById("all-invalid-test")) {
                alert("No valid deliveries to import")
                return
            }

            if (document.getElementById("any-invalid-test") != null) {
                if (!confirm("There are some invalid deliveries, do you still want to import?")) {
                    return
                }
            }

            $.ajax({
                url: "${createLink(controller: 'delivery', action: 'ajaxImportDeliveries')}",
                method: "POST",
                statusCode: {
                    500: function (response) {
                        alert("Unable to import uploaded deliveries, please try again later.")
                    },
                    200: function (response) {
                        alert("Deliveries successfully imported")
                    }
                }
            })
        }
    </script>
</head>

<body>
<section id="breadcrumb-section" class="container-fluid">
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

<section id="title-section" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-6 offset-3">
            <h2 id="page-title" class="mx-auto my-auto">Deliveries</h2>
        </div>

        <div class="col-3">
            <div class="row justify-content-end">
                <div class="col-md-auto">
                    <button id="delivery-upload-button" type="button" class="btn btn-wl mt-1"
                            onclick="$('#deliveries-file').click()">Upload Delivery</button>
                </div>
            </div>

            <div class="row justify-content-end">
                <div class="col-md-auto">
                    <button id="delivery-import-button" type="button" class="btn btn-wl mt-1 green"
                            onclick="onImport()">Import</button>
                    <button id="delivery-cancel-button" type="button" class="btn btn-wl mt-1 red"
                            onclick="onCancel()">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</section>

<section id="results-section" class="container-fluid mb-3">
    <div id="results-container">
        <g:render template="deliveryImportResults" model="[deliveries: deliveries]"/>
    </div>
</section>

<section id="upload-section" class="container-fluid mt-4">
    <div class="col-12">
        <g:uploadForm>
            <!-- accept field types here must match ones defined in the onFileUpload function above -->
            <input id="deliveries-file" name="deliveries-file" type="file" accept="text/csv,application/vnd.ms-excel"
                   hidden/>
        </g:uploadForm>
    </div>
</section>
</body>
</html>