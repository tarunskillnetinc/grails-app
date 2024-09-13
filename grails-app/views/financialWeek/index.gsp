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

    <style>
    @media (max-width: 768px) {
        #uploadFinancialWeekBtn, #downloadFinancialWeekBtn {
            width: 100%; /* Full width for buttons on smaller screens */
            margin-bottom: 10px;
        }

        #yearSelect, #downloadFinancialWeekBtn {
            width: 100%; /* Full width for dropdown and button on smaller screens */
            margin-bottom: 10px;
        }
    }

    @media (min-width: 769px) {
        #uploadFinancialWeekBtn {
            min-width: 200px; /* Fixed width for upload button on larger screens */
            display: block;
            margin: 20px auto; /* Center the button */
        }

        #yearSelect, #downloadFinancialWeekBtn {
            display: inline-block;
            vertical-align: middle;
        }

        .download-container {
            text-align: center;
            margin-top: 20px;
        }
    }

    .buttons-container {
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
        gap: 10px; /* Adds spacing between the buttons */
        margin-top: 20px;
    }

    /* Styles for the download section */
    .download-container label {
        font-size: 1.5rem;
        font-weight: bold;
        display: block;
        margin-bottom: 10px;
        text-align: center;
    }

    .download-container .form-control {
        display: inline-block;
        max-width: 300px;
        min-width: 250px;
        padding: 0.5rem 1rem;
        font-size: 1.125rem;
    }

    .section-gap {
        margin-bottom: 3rem;
    }
    </style>

    <script type="application/javascript">

        $(document).ready(function() {
            var actionSuccess = '${actionSuccess}';  // This will be 'true' or 'false' based on the backend response
            if (actionSuccess === 'false') { //Display error when loading index page if any error occur
                // Display error message using messageDisplay function
                var errorMessage = '${message}';  // Error message from the backend, if any
                var defaultErrorMessage = "An unexpected error while processing the request.";
                messageDisplay({ responseJSON: { errorsList: [errorMessage] } }, true, defaultErrorMessage, null);
            }


            //Handle csv file download button enable/disable status on view boot
            var enableDownloadButton = '${enableCsvDownload}'
            if (enableDownloadButton === 'false') {
                const downloadButton = document.getElementById('downloadFinancialWeekBtn');
                downloadButton.disabled = true
            }
        });

        function uploadFinancialWeekImportFile() {
            setPreventWindowNavigation(true)
            $("#uploadResults").html("<div class=\"modal-body\">"
                + "<div class=\"row mb-4\"><div class=\"col-12\"><h3 class=\"text-center\">Please wait uploading file...</h3></div></div>"
                + "<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

            const uploadButton = document.getElementById('uploadFinancialWeekBtn');
            uploadButton.disabled = true;
            uploadButton.innerHTML = "Uploading...";
            let url = "${createLink(controller: 'FinancialWeek', action: 'ajaxCSVFinancialWeekImport')}";

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
                        uploadButton.innerHTML = "Upload Financial Week File"
                        resetFileUploadInput();
                        setPreventWindowNavigation(null);
                        messageDisplay(response, true, "CSV file import error, Please try again", null);
                    },
                    200: function (response) {
                        $("#uploadResults").html("");
                        uploadButton.disabled = false
                        uploadButton.innerHTML = "Upload Financial Week File"
                        resetFileUploadInput();
                        setPreventWindowNavigation(null);
                        messageDisplay(response, false, null, "Financial Week import completed successfully");
                        updateDropDown(response)
                    }
                }
            });
        }

        function downloadFinancialWeekUploadFile(){
            var selectedYear = $('#yearSelect').val();
            if (selectedYear) {
                // Construct the download URL with the selected financial year as a query parameter
                var downloadUrl = "${createLink(controller: 'FinancialWeek', action: 'downloadCsv')}?yearSelect=" + encodeURIComponent(selectedYear);
                window.location.href = downloadUrl;
            } else {
                messageDisplay(null, true, "Please select a financial year before downloading", null); //Error generating csv weekly financial file
            }
        }

        function selectFinancialWeekUploadFile() {
            $("#csvFileUploadInput").trigger('click');
        }

        function setPreventWindowNavigation(value) {
            window.onbeforeunload = function () {
                return value;
            };
        }

        function updateDropDown(response) {
            var financialYears = response?.financialYears;
            var $dropdown = $('#yearSelect');
            if (Array.isArray(financialYears) && financialYears.length > 0) {
                $dropdown.empty();  // Clear the existing options

                // Re-add the placeholder option
                $dropdown.append($('<option></option>').val("").text("Select a financial year").prop('disabled', true).prop('selected', true));

                // Populate the dropdown with the updated financial years
                $.each(financialYears, function(index, year) {
                    $dropdown.append($('<option></option>').val(year).text(year));
                });

                // Enable the download button if there are financial years
                const downloadButton = document.getElementById('downloadFinancialWeekBtn');
                downloadButton.disabled = false;
            } else {
                // If there are no financial years, add the placeholder option and disable the download button
                $dropdown.empty();  // Clear the existing options
                $dropdown.append($('<option></option>').val("").text("No financial years available").prop('disabled', true).prop('selected', true));

                const downloadButton = document.getElementById('downloadFinancialWeekBtn');
                downloadButton.disabled = true;
            }
        }

        function handleUploadError(uploadButton, msg) {
            $("#uploadResults").html("");
            uploadButton.disabled = false
            uploadButton.innerHTML = "Upload Financial Week File"
            showErrorAlert(msg)
            resetFileUploadInput();
            setPreventWindowNavigation(null);
        }

        function resetFileUploadInput() {
            $('#csvFileUploadInput').get(0).value = null
        }

        function messageDisplay(response, isError, defaultErrorMessage, defaultSuccessMessage){
            var divClass = null
            var messageDiv = null
            if (isError){ //Display error messages
                divClass = 'alert alert-danger alert-wl mx-0';
                messageDiv = $('<div class="' + divClass + '" role="alert"></div>');
                var messageList = response?.responseJSON?.errorsList;
                if (messageList && messageList.length > 0) {
                    messageList.forEach(function(message) {
                        var messageSpan = $('<span>' + message + '</span>');
                        messageDiv .append(messageSpan);
                        messageDiv .append($('<br>'));
                    });
                } else {
                    var messageSpan = $('<span>' + defaultErrorMessage + '</span>');
                    messageDiv .append(messageSpan);
                    messageDiv .append($('<br>'));
                }
            } else { // Display success messages
                divClass = 'alert alert-success alert-wl mx-0';
                messageDiv = $('<div class="' + divClass + '" role="alert"></div>');
                var messageSpan = $('<span>' + defaultSuccessMessage + '</span>');
                messageDiv .append(messageSpan);
                messageDiv .append($('<br>'));
            }

            var closeIcon = $('<span id="cancel-icon" class="close" aria-label="Close">&times;</span>');

            closeIcon.click(function () {
                messageDiv.remove(); // Remove the error message div when the cancel icon is clicked
            });

            messageDiv.append(closeIcon);
            $('#message-container').html(messageDiv);

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
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Financial Weeks</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="FinancialWeekUpload" class="container-fluid">

        <section id="message-container" class="container-fluid mb-20"></section>
        <input type="file" name="file" accept=".csv,.CSV" id="csvFileUploadInput" style="display:none" oninput="uploadFinancialWeekImportFile()" oncancel="resetFileUploadInput()">

        <!-- Header with title -->
        <div class="header-wl mb-8 section-gap">
            <h2 id="page-title" class="mx-auto my-auto">Financial Weeks</h2>
        </div>

        <!-- Download section with label, dropdown, and button aligned closely together -->
        <div class="download-container mt-8">

            <!-- Upload button centered below the header -->
            <button class="btn btn-wl btn-primary p-2 mt-8" onclick="selectFinancialWeekUploadFile()" id="uploadFinancialWeekBtn">Upload Financial Week File</button>

            <label style="font-size: 1.5rem; font-weight: bold;" for="yearSelect">Select financial year for download:</label>
            <g:form controller="financialWeek" action="downloadCsv" method="GET" class="d-inline">
                <select class="form-control select-border" id="yearSelect" name="yearSelect">
                    <option value="" disabled selected>Select a financial year</option> <!-- Placeholder option -->
                    <g:each in="${financialYears}" var="year">
                        <option value="${year}">${year}</option> <!-- Render each financial year -->
                    </g:each>
                </select>
            </g:form>
            <button class="btn btn-wl btn-primary p-2 ml-3" onclick="downloadFinancialWeekUploadFile()" id="downloadFinancialWeekBtn">Download Financial Week File</button>

        </div>

    </section>

    <section id="uploadResultsSection" class="container-fluid">
        <div id="uploadResults"></div>
    </section>

</body>
</html>
