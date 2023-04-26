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

        function uploadHardwareImportFile() {
            setPreventWindowNavigation(true);
            $("#uploadResults").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");

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
            const alertWindow = $('#dialog-csv-upload-error');
            let warningItems = "<ul>"
            let errorHtml = '';

            errors.forEach((error) => {
                errorHtml += "<li>" + error + "</li>"
            })

            warningItems += errorHtml
            warningItems += "<hr>"

            warningItems = warningItems.length > 4 ? warningItems.slice(0, -4) : warningItems
            warningItems += "</ul>"


            alertWindow.html("<div>"
                + "<p><span class='ui-icon ui-icon-alert' style='float:left; margin:12px 12px 20px 0;'></span></p>"
                +  warningItems +
                "</div>");

            alertWindow.dialog({
                title: "The following errors were found while uploading the hardware",
                autoOpen: false,
                resizable: false,
                height: "auto",
                width: "40%",
                modal: true,
                buttons: {
                    Close: function () {
                        $(this).dialog("close");
                    }
                },
                open: function () {
                    $(".ui-dialog-titlebar-close").hide();
                    $(this).dialog('option', 'maxHeight', $(window).height());
                }
            }).dialog('open');
        }

        function showSuccessAlert() {
            const alertWindow = $('#dialog-csv-upload-error');
            alertWindow.html("<div>"
                + "<p>"
                + "Successfully uploaded all the valid serial numbers</p>"
                + "</div>");

            alertWindow.dialog({
                title: "Success",
                autoOpen: false,
                resizable: false,
                height: "auto",
                width: "30%",
                modal: true,
                buttons: {
                    Close: function () {
                        $(this).dialog("close");
                    }
                },
                open: function () { $(".ui-dialog-titlebar-close").hide(); }
            }).dialog('open');
        }

        function resetFileUploadInput() {
            $('#csvFileUploadInput').get(0).value = null
        }

        function bindUploadButtons() {
            $("#uploadSave").click(function () {
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
                        $("#uploadResults").html("");
                        uploadButton.disabled = false
                        uploadButton.innerHTML = "Upload Hardware"

                        showSuccessAlert()
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
            });

            $("#uploadCancel").click(function () {
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
            <div class="col-6 offset-2">
                <h2 id="page-title" class="mx-auto">Hardware Import</h2>
            </div>
            <div class="col-4 text-right d-inline-flex flex-row justify-content-end">
                <button class="btn btn-wl p-2 ml-2" onclick="selectHardwareUploadFile()" id="uploadHardwareBtn">Upload Hardware</button>
                <input type="file" name="file" accept=".csv,.CSV"
                       id="csvFileUploadInput" style="display:none" oninput="uploadHardwareImportFile()" oncancel="resetHardwareInput()">
            </div>
        </div>
    </section>

    <g:if test="${flash.message}">
        <section id="alerts-container" class="container-fluid">
            <div id="alerts-container-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
        </section>
    </g:if>

    <section id="uploadResultsSection" class="container-fluid">
        <div id="uploadResults">

        </div>
    </section>

    <div id="dialog-csv-upload-error" style="display:none; max-height: 80%">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Error uploading products </p>
    </div>
</body>
</html>