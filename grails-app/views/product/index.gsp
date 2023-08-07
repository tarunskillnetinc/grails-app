<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>Product Maintenance</title>

        <asset:javascript src="jquery-ui.js" />
        <asset:stylesheet src="jquery-ui.css" />

        <script type="text/javascript">
            $(document).ready(function () {
                $('#productSearchTerm').on('keyup', function(event) {
                    if (event.key === 'Enter') {
                        searchButtonClicked();
                    }
                });

                var existingSearchTerm = $('#productSearchTerm').val();
                if (existingSearchTerm != null && existingSearchTerm !== "") {
                    searchButtonClicked();
                }
            });

            function searchButtonClicked() {
                $('#offset').val(0);
                search();
            }

            function resetForm() {
                document.getElementById('productSearchTerm').value = null;
                document.getElementById('productSearchBy').value = 'everything';
            }

            function search() {
                var url = "${createLink(controller: 'product', action: 'ajaxSearchProducts')}";
                var searchTerm = $('#productSearchTerm').val();
                var searchBy = $('#productSearchBy').val();

                if (searchBy === "barcode" && searchTerm.length < 4) {
                    alert("Please enter at least 4 digits of a barcode.");
                    return;
                }

                $("#search-results").hide();
                $("#loading-indicator").show();

                $.ajax({
                    url: url,
                    data: { searchTerm: searchTerm, searchBy: searchBy },
                    success: function(resp) {
                        $('#results-container').html(resp);
                        $('#productSearchTerm').data('prev',$('#productSearchTerm').val())
                        $('#productSearchBy').data('prev', $('#productSearchBy').val())
                    }
                });
            }

            function saveColumns() {
                var url = "${createLink(controller: 'product', action: 'ajaxSaveColumns')}";

                var checkboxValues = { };

                $("#reportColumnsForm input:checkbox").each(function() {
                    checkboxValues[$(this).val()] = this.checked;
                }).get();

                $.ajax({
                    url: url,
                    method: "POST",
                    data: { reportColumns: JSON.stringify(checkboxValues), reportType: "PRODUCT_SEARCH" },
                    success: function(resp) {
                        $("#columnsCollapse").collapse('hide');
                        search();
                    }
                });

            }

            function selectProductsCSVFile() {
                $("#csvFileUploadInput").trigger('click');
            }

            function uploadProductsCSVFile() {
                setPreventWindowNavigation(true);

                const uploadButton = document.getElementById('uploadProductsBtn');
                uploadButton.disabled = true;
                uploadButton.innerHTML = "Uploading...";
                let url = "${createLink(controller: 'product', action: 'ajaxCSVProductUpload')}";

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
                    success: function(data) {
                        const response = JSON.parse(data)
                        if (response.status === "SUCCESS") {
                            uploadButton.disabled = false
                            uploadButton.innerHTML = "Upload Products"
                            showSuccessAlert()
                        } else {
                            uploadButton.disabled = false
                            uploadButton.innerHTML = "Upload Products"
                            showErrorAlert(response.errors)
                        }

                        resetFileUploadInput();
                        setPreventWindowNavigation(null);

                    },
                    error: function (data) {
                        const response = JSON.parse(data)
                        uploadButton.disabled = false
                        uploadButton.innerHTML = "Upload Products"
                        showErrorAlert(response.errors)
                        resetFileUploadInput();
                        setPreventWindowNavigation(null);
                    }
                });
            }

            function showSuccessAlert() {
                const alertWindow = $('#dialog-csv-upload-error');
                alertWindow.html("<div>"
                    + "<p>"
                    + "Successfully uploaded all the products</p>"
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

            function showErrorAlert(errors) {
                console.log(errors)
                const alertWindow = $('#dialog-csv-upload-error');
                let warningItems = "<ul>"

                errors.forEach((error) => {
                    let errorHtml = '';
                    let setHeader = false;
                    error.errors.forEach((errorItem) => {
                        if(!setHeader) {
                            errorHtml += "<h5>" + errorItem['message'] + "</h5>"
                            setHeader = true
                        } else {
                            errorHtml += "<li>" + errorItem['message'] + "</li>"
                        }
                    });
                    warningItems += errorHtml
                    warningItems += "<hr>"
                })

                warningItems = warningItems.length > 4 ? warningItems.slice(0, -4) : warningItems
                warningItems += "</ul>"


                alertWindow.html("<div>"
                    + "<p><span class='ui-icon ui-icon-alert' style='float:left; margin:12px 12px 20px 0;'></span></p>"
                    +  warningItems +
                    "</div>");

                alertWindow.dialog({
                    title: "Following errors were found while uploading the products",
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


            function resetFileUploadInput() {
                $('#csvFileUploadInput').get(0).value = null
            }

            function setPreventWindowNavigation(value) {
                window.onbeforeunload = function() {
                    return value;
                };
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
                            <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Product Search</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <g:if test="${flash.message}">
            <section id="alerts-container" class="container-fluid">
                <div id="alerts-container-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </section>
        </g:if>

        <div id="dialog-csv-upload-error" style="display:none; max-height: 80%">
            <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>Error uploading products </p>
        </div>

        <section id="maintenance-search" class="container-fluid">
            <div class="row header-wl mt-3">
                <input type="file" name="file" accept=".csv,.CSV"
                       id="csvFileUploadInput" style="display:none" oninput="uploadProductsCSVFile()" oncancel="resetFileUploadInput()">
                <div class="col-6 offset-2">
                    <h2 id="page-title" class="mx-auto my-auto">Product Search</h2>
                </div>

                <div class="col-4 text-right d-inline-flex flex-row justify-content-end">
                    <g:link elementId="add-new-product-btn" controller="product" action="add" class="btn btn-wl p-2">Add New Product</g:link>
                    <button class="btn btn-wl p-2 ml-2" onclick="selectProductsCSVFile()" id="uploadProductsBtn">Upload Products</button>
                </div>
            </div>

            <div class="row mt-4">
                <div class="col-6">
                    <div class="card bg-light border-wl">
                        <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                            <div class="row">
                                <div id="filters-header" class="col-10">Filters</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>

                        <div class="card-body collapse show" id="filterCollapse">
                            <div class="form-group row">
                                <label for="productSearchTerm" class="col-2 col-form-label-sm text-right">Search Term</label>
                                <div class="col-10 input-group">
                                    <g:textField id="productSearchTerm" name="productSearchTerm" maxlength="100" value="${session.PRODUCT_SEARCH_TERM}" class="form-control" aria-describedby="select-addon2" />

                                    <div class="input-group-append">
                                        <g:select id="productSearchBy" name="productSearchBy" from="${['everything', 'description', 'itemCode', 'barcode']}" value="everything" valueMessagePrefix="ProductSearchBy" class="form-control select-border" style="z-index: 0;" />
                                    </div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-4 offset-8 text-right">
                                    <button id="reset-filters-btn" type="button" class="btn btn-danger text-right mr-2" onclick="resetForm()">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="searchButtonClicked()">Search</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-2 offset-4">
                    <div class="card bg-light border-wl">
                        <div id="columns-collapse" class="card-header pointer" data-toggle="collapse" data-target="#columnsCollapse" aria-expanded="false" aria-controls="columnsCollapse">
                            <div class="row">
                                <div class="col-10">Columns</div>
                                <div class="col-2 text-right">
                                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-body collapse" id="columnsCollapse">
                            <g:form name="reportColumnsForm" id="reportColumnsForm">
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsItemCode" class="form-check-input" value="itemCode" checked="${!userColumns || userColumns?.columns?.find { it.column == 'itemCode' }?.enabled}" />
                                    <label class="form-check-label" for="columnsItemCode">Item Code</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsDescription" class="form-check-input" value="description" checked="${!userColumns || userColumns?.columns?.find { it.column == 'description' }?.enabled}" />
                                    <label class="form-check-label" for="columnsDescription">Description</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsUnitSize" class="form-check-input" value="unitSize" checked="${!userColumns || userColumns?.columns?.find { it.column == 'unitSize' }?.enabled}" />
                                    <label class="form-check-label" for="columnsUnitSize">Unit Size</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsVatRate" class="form-check-input" value="vatRate" checked="${!userColumns || userColumns?.columns?.find { it.column == 'vatRate' }?.enabled}" />
                                    <label class="form-check-label" for="columnsVatRate">VAT Rate</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsCategory" class="form-check-input" value="category" checked="${!userColumns || userColumns?.columns?.find { it.column == 'category' }?.enabled}" />
                                    <label class="form-check-label" for="columnsCategory">Category</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsCostPrice" class="form-check-input" value="costPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'costPrice' }?.enabled}" />
                                    <label class="form-check-label" for="columnsCostPrice">Cost</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsRetailPrice" class="form-check-input" value="retailPrice" checked="${!userColumns || userColumns?.columns?.find { it.column == 'retailPrice' }?.enabled}" />
                                    <label class="form-check-label" for="columnsRetailPrice">Retail Price</label>
                                </div>
                                <div class="form-group form-check">
                                    <g:checkBox name="columns" id="columnsMargin" class="form-check-input" value="margin" checked="${!userColumns || userColumns?.columns?.find { it.column == 'margin' }?.enabled}" />
                                    <label class="form-check-label" for="columnsMargin">Margin</label>
                                </div>

                                <button id="columns-submit-button" type="button" class="btn btn-wl" onclick="saveColumns();">Apply</button>
                            </g:form>
                        </div>
                    </div>
                </div>
            </div>

            <div id="results-container" class="align-content-center">
                <g:render template="productSearchResults" />
            </div>
        </section>
    </body>
</html>