<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>Financial Week</title>

    <asset:javascript src="jquery-ui.js"/>
    <asset:stylesheet src="jquery-ui.css"/>

    <asset:stylesheet src="bootstrap-datepicker3.min.css"/>
    <asset:javascript src="bootstrap-datepicker.min.js"/>
    <asset:javascript src="moment-with-locales.min.js"/>

    <script type="application/javascript">

        function selectFinancialWeekUploadFile() {
            $("#csvFileUploadInput").trigger('click');
        }

        function setPreventWindowNavigation(value) {
            window.onbeforeunload = function () {
                return value;
            };
        }

        function resetMessages() {
            $('#successMessage').hide();
            $('#failureMessage').hide();
        }

        function uploadFinancialWeekImportFile() {
            setPreventWindowNavigation(true)
            resetMessages();
            $("#uploadResults").html("<div class=\"modal-body\">"
                + "<div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait uploading file...</h3></div></div>"
                + "<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

            const uploadButton = document.getElementById('uploadFinancialWeekBtn');
            uploadButton.disabled = true;
            uploadButton.innerHTML = "Uploading...";
            let url = "${createLink(controller: 'FinancialWeekCSV', action: 'ajaxCSVFinancialWeekImport')}";

            const file = $('#csvFileUploadInput').get(0).files[0]

            if (!file) {
                handleUploadError(uploadButton, "No file selected. Please choose a file.");
                return;
            }

            const fileName = file.name;
            const fileExtension = fileName.split('.').pop().toLowerCase();

            if (fileExtension !== 'csv') {
                handleUploadError(uploadButton, "Incorrect file format. Please upload a valid .csv file.");
                return;
            }

            let jForm = new FormData();
            jForm.append("file", file);

            $.ajax({
                url: url,
                type: "POST",
                data: jForm,
                mimeType: "multipart/form-data",
                contentType: false,
                cache: false,
                processData: false,
                dataType: 'json',  // Ensure that the response is expected as JSON
                statusCode: {
                    500: function (response) {
                        $("#uploadResults").html("");
                        uploadButton.disabled = false
                        uploadButton.innerHTML = "Upload Financial Week"
                        resetFileUploadInput();
                        setPreventWindowNavigation(null);
                        messageDisplay(response, true);
                    },
                    200: function (response) {
                        $("#uploadResults").html("");
                        uploadButton.disabled = false
                        uploadButton.innerHTML = "Upload Financial Week"
                        resetFileUploadInput();
                        setPreventWindowNavigation(null);
                        showSuccessAlert();
                        messageDisplay(response, false);
                    }
                }
            });
        }

        function downloadFinancialWeekUploadFile(){
            var selectedYear = $('#yearSelect').val();
            if (selectedYear) {
                // Construct the download URL with the selected financial year as a query parameter
                var downloadUrl = "${createLink(controller: 'financialWeekCSV', action: 'downloadCsv')}?yearSelect=" + encodeURIComponent(selectedYear);
                $.ajax({
                    url: downloadUrl,
                    type: "GET",
                    mimeType: "multipart/form-data",
                    contentType: false,
                    cache: false,
                    processData: false,
                    dataType: 'json',  // Ensure that the response is expected as JSON
                    statusCode: {
                        500: function (response) {

                        },
                        200: function (response) {

                        }
                    }
                });
            } else {
                alert('Please select a financial year before downloading.');
            }
        }

        function handleUploadError(uploadButton, msg) {
            $("#uploadResults").html("");
            uploadButton.disabled = false
            uploadButton.innerHTML = "Upload Financial Week"
            showErrorAlert(msg)
            resetFileUploadInput();
            setPreventWindowNavigation(null);
        }

        function showErrorAlert(msg) {
            $('#failureMessage').show();
            $('#failureMessage').text(msg)
        }

        function showSuccessAlert() {
            $('#successMessage').show();
            $('#successMessage').text("Financial Week import completed successfully");
        }

        function resetFileUploadInput() {
            $('#csvFileUploadInput').get(0).value = null
        }

        function messageDisplay(response, isError){
            var divClass = null
            var messageDiv = null
            if (isError){ //Display error messages
                var messageList = response.responseJSON.response;
                if (messageList && messageList.length > 0) {
                    divClass = 'alert alert-danger alert-wl mx-0';
                    messageDiv = $('<div class="' + divClass + '" role="alert"></div>');


                    messageList.forEach(function(message) {
                        var messageSpan = $('<span>' + message + '</span>');
                        messageDiv .append(messageSpan);
                        messageDiv .append($('<br>'));
                    });
                }
            } else { // Display success messages
                divClass = 'alert alert-success alert-wl mx-0';
                messageDiv = $('<div class="' + divClass + '" role="alert"></div>');
                var messageSpan = $('<span>' + 'Financial Week import completed successfully' + '</span>');
                messageDiv .append(messageSpan);
                messageDiv .append($('<br>'));
            }

            var closeIcon = $('<span id="cancel-icon" class="close" aria-label="Close">&times;</span>');

            closeIcon.click(function () {
                messageDiv.remove(); // Remove the error message div when the cancel icon is clicked
            });

            messageDiv.append(closeIcon);
            $('#errors-container').html(messageDiv);

            // Adjust icon position to top-right corner
            closeIcon.css({
                "position": "absolute",
                "top": "-10px",
                "right": "1px",
                "margin": "0.5rem"
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Financial Week</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="FinancialWeekUpload" class="container-fluid">

    <section id="errors-container" class="container-fluid mb-20"></section>

    <div class="row header-wl mt-3">
        <input type="file" name="file" accept=".csv,.CSV" id="csvFileUploadInput" style="display:none" oninput="uploadFinancialWeekImportFile()" oncancel="resetFinancialWeekInput()">
        <div class="col-8 offset-2 text-center">
            <h2 id="page-title" class="mx-auto my-auto">Financial Week</h2>
        </div>
        <div class="col-2 text-right d-inline-flex flex-row justify-content-end">
            <button class="btn btn-wl p-2 ml-2" onclick="selectFinancialWeekUploadFile()" id="uploadFinancialWeekBtn" style="min-width: 200px; white-space: nowrap;">Upload Financial Week</button>
            <button class="btn btn-wl p-2 ml-2" onclick="downloadFinancialWeekUploadFile()" style="min-width: 200px; white-space: nowrap;">Download Financial Week CSV</button>
        </div>
    </div>


    <div class="row mt-5 justify-content-center"> <!-- Increased the margin-top to 5 -->
        <div class="col-6 d-flex align-items-center justify-content-center">
            <span class="font-weight-bold" style="font-size: 1.25rem; margin-right: 15px;">Select financial year:</span>
            <g:form controller="financialWeekCSV" action="downloadCsv" method="GET" class="d-inline">
                <g:select class="form-control select-border" id="yearSelect" name="yearSelect" from="${financialWeeks.financialYear}" style="width: 250px;"/>
            </g:form>
        </div>
    </div>

</section>

</body>
</html>