<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Wonderlane Hardware Import</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="moment-with-locales.min.js"/>

    <script type="application/javascript">
        function selectHardwareUploadFile() {
            $("#csvFileUploadInput").trigger('click');
        }

        function setPreventWindowNavigation(value) {
            window.onbeforeunload = function() {
                return value;
            };
        }

        function resetMessages()
        {
            $('#successMessage').hide();
            $('#failureMessage').hide();
        }

        function uploadHardwareImportFile() {
            setPreventWindowNavigation(true)
            resetMessages();
            $("#uploadResults").html("<div class=\"modal-body\">"
                + "<div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait uploading file...</h3></div></div>"
                + "<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

            const uploadButton = document.getElementById('uploadHardwareBtn');
            uploadButton.disabled = true;
            uploadButton.innerHTML = "Uploading...";
            let url = "${createLink(controller: 'hardwareImport', action: 'ajaxCSVHardwareUpload')}";

            let jForm = new FormData();
            jForm.append("file", $('#csvFileUploadInput').get(0).files[0]);

            $.ajax({
                url: url,
                type: "POST",
                data: jForm,
                mimeType: "multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
                success: function(resp) {
                    $("#uploadResults").html(resp);
                    uploadButton.disabled = false
                    uploadButton.innerHTML = "Upload Hardware"
                    bindUploadButtons()
                    resetFileUploadInput();
                    setPreventWindowNavigation(null);
                },
                error: function (data) {
                    const response = JSON.parse(data)
                    uploadButton.disabled = false
                    uploadButton.innerHTML = "Upload Hardware"
                    showErrorAlert(response.errors)
                    resetFileUploadInput();
                    setPreventWindowNavigation(null);
                }
            });
        }

        function showErrorAlert(errors) {
            $('#failureMessage').show();
            $('#failureMessage').text("There was an error completing the import. Please try again.")
        }

        function showSuccessAlert() {
            $('#successMessage').show();
            $('#successMessage').text("Hardware import completed successfully");
        }

        function resetFileUploadInput() {
            $('#csvFileUploadInput').get(0).value = null
        }

        function bindUploadButtons() {
            $("#uploadSave").click(function () {
                $("#uploadResults").html("<div class=\"modal-body\">"
                    + "<div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait importing results...</h3></div></div>"
                    + "<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
                let url = "${createLink(controller: 'hardwareImport', action:'confirmImport')}";
                const uploadButton = document.getElementById('uploadHardwareBtn');
                $.ajax({
                    url: url,
                    type: "POST",
                    mimeType: "multipart/form-data",
                    contentType: false,
                    cache: false,
                    processData: false,
                    success: function (resp) {
                        $("#uploadResults").html("");// Stop spinner as it has finished
                        uploadButton.disabled = false
                        uploadButton.innerHTML = "Upload Hardware"
                        showSuccessAlert()
                        resetFileUploadInput();
                        setPreventWindowNavigation(null);
                    },
                    error: function (data) {
                        if(!data.status === 504){
                            $("#uploadResults").html(""); // Stop spinner as it has errored
                            const response = JSON.parse(data) // when timing out this fails to parse JSON data as data is not a parsable JSON string
                            uploadButton.disabled = false
                            uploadButton.innerHTML = "Upload Hardware"
                            showErrorAlert(response.errors)
                            resetFileUploadInput();
                            setPreventWindowNavigation(null);
                        } else {
                            $("#uploadResults").html(""); // Stop spinner as it has errored
                            uploadButton.disabled = false
                            uploadButton.innerHTML = "Upload Hardware"
                            showErrorAlert("Server Timeout")
                            resetFileUploadInput();
                            setPreventWindowNavigation(null);
                        }

                    }
                });
            });

            $("#uploadCancel").click(function () {
                resetMessages();
                $("#uploadResults").html("");
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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Hardware Import</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="hardwareUpload" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 id="page-title" class="mx-auto my-auto">Hardware Import</h2>
            </div>
            <div class="col-2 text-right d-inline-flex flex-row justify-content-end">

                <button class="btn btn-wl p-2 ml-2" onclick="selectHardwareUploadFile()" id="uploadHardwareBtn">Upload Hardware</button>
                <input type="file" name="file" accept=".csv,.CSV"
                       id="csvFileUploadInput" style="display:none" oninput="uploadHardwareImportFile()" oncancel="resetHardwareInput()">
            </div>
        </div>
    </section>

    <div class="alert alert-success alert-wl" role="alert" id="successMessage" style="display: none"></div>

    <div class="alert alert-danger alert-wl" role="alert" id="failureMessage" style="display: none"></div>

    <section id="uploadResultsSection" class="container-fluid">
        <div id="uploadResults">

        </div>
    </section>

    <div id="dialog-csv-upload-error" style="display:none; max-height: 80%">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Error uploading products </p>
    </div>
</body>
</html>