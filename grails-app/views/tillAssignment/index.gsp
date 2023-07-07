<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <meta name="layout" content="main" />

    <title>Till Assignment</title>

    <script type="text/javascript">

        var getTillsUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxSearchForTills')}"
        var deleteTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxDeleteTill')}"
        var addTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxAddTill')}"
        var editTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxEditTill')}"
        var unassignSerialUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxUnassignSerial')}"
        var saveTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxSaveTill')}"
        var generatePinUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxGeneratePin')}"
        var cancelTillUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxCancelTill')}"
        var advancedConfigurationUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxAdvancedConfiguration')}"
        var saveAdvancedConfigurationUrl = "${createLink(controller: 'tillAssignment', action: 'ajaxSaveAdvancedConfiguration')}"

        $(function() {
            getTills();
            applyListeners();
        });

        function getTills() {
            $('#results-container').html("");
            $("#loading-indicator").show();

            var filterParams = {};

            $("#filtersForm input").each(function() {
                filterParams[$(this).attr("name")] = $(this).val();
            }).get();

            $("#filtersForm select").each(function() {
                filterParams[$(this).attr("name")] = $(this).find(":selected").val();
            }).get();

            $.ajax({
                url: getTillsUrl,
                data: filterParams,
                success: function(resp) {
                    $('#results-container').html(resp);
                }
            });
        }

        function deleteTill(storeId, tillId, serialNumber) {
            if (confirm("This will delete till " + tillId + " from Store " + storeId)) {
                $.ajax({
                    url: deleteTillUrl,
                    method: "DELETE",
                    data: {storeId: storeId, tillId: tillId, serialNumber: serialNumber},
                    success: function (data, textStatus, resp) {
                        $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        getTills();
                    },
                    error: function (resp) {
                        $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp.responseText + '</div>');
                        getTills();
                    }
                });
            }
        }

        function advancedConfiguration(serialNumber) {
            $("#advancedTillContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#advancedTillModal').modal({show: true, backdrop: 'static', keyboard: false});
            $.ajax({
                url: advancedConfigurationUrl,
                method: "GET",
                data: {serialNumber: serialNumber},
                success: function (resp) {
                    $("#advancedTillContent").html(resp);
                }
            });
        }

        function clearFilters() {
            $("#storeIdFilter").val("");
            $("#tillIdFilter").val("");
            $("#serialNumberFilter").val("");
            getTills();
        }

        function addTill() {
            $("#addTillContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addTillModal').modal({show: true, backdrop: 'static', keyboard: false});
            $.ajax({
                url: addTillUrl,
                method: "GET",
                success: function (resp) {
                    $("#addTillContent").html(resp);
                    applyListeners();
                }
            });
        }

        function editTill(storeId, tillId, serialNumber) {
            $("#addTillContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addTillModal').modal({show: true, backdrop: 'static', keyboard: false});

            $.ajax({
                url: editTillUrl,
                method: "GET",
                data: {storeId: storeId, tillId: tillId, serialNumber: serialNumber},
                success: function (resp) {
                    $("#addTillContent").html(resp);
                    applyListeners();
                }
            });
        }

        function unassignSerial(id, serialNumber, tillId) {
            if (confirm("Unassign serial " + serialNumber +" from till ID " +tillId +"?")) {
                $.ajax({
                    url: unassignSerialUrl,
                    method: "POST",
                    data: { tillConfigId: id },
                    success: function (resp) {
                        if (resp === "OK") {
                            alert("Successfully unassigned serial " +serialNumber +" from till ID " +tillId +".");
                            getTills();
                        }
                    }
                });
            }
        }


        function saveTill() {
            var formValues = $("#addTillForm").serialize();
            $("#addTillContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>")
            $.ajax({
                url: saveTillUrl,
                method: "POST",
                data: formValues,
                success: function (resp) {
                    if (resp === "OK") {
                        $('#addTillModal').modal('hide')
                        getTills();
                    } else {
                        $("#addTillContent").html(resp);
                    }
                }
            });
        }

        function saveAdvancedConfiguration() {
            var formValues = $("#advancedConfigForm").serialize();
            $("#advancedTillContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>")
            $.ajax({
                url: saveAdvancedConfigurationUrl,
                method: "POST",
                data: formValues,
                success: function (resp) {
                    if (resp === "OK") {
                        $('#advancedTillModal').modal('hide')
                        getTills();
                    } else {
                        $("#advancedTillContent").html(resp);
                    }
                }
            });
        }

        function cancelTill() {
            if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
                $.ajax({
                    url: cancelTillUrl,
                    method: "POST",
                    success: function () {
                        $('#addTillModal').modal('hide')
                        $('#advancedTillModal').modal('hide')
                        getTills();
                    }
                })

            }
        }

        function generatePin() {
            var formValues = $("#addTillForm").serialize();
            $.ajax({
                url: generatePinUrl,
                method: "POST",
                data: formValues,
                success: function (resp) {
                    document.getElementById("registration-code-holder").style.display = "block";
                    document.getElementById("registration-code-value").innerText = resp
                }
            })
        }

        function filter(inputName, dropDownName) {
            var keyword = document.getElementById(inputName).value.toLowerCase();
            var select = document.getElementById(dropDownName);
            for (var i = 0; i < select.length; i++) {
                var txt = select.options[i].text.toLowerCase();
                if (!txt.match(keyword)) {
                    $(select.options[i]).attr('disabled', 'disabled').hide();
                } else {
                    $(select.options[i]).removeAttr('disabled').show();
                }
            }
        }

        function applyListeners() {
            var tillIdFilter = document.getElementById("tillIdFilter")
            var addTillIdField = document.getElementById("tillId")
            var maxLength = 10
            var maxValue = 2147483647

            if (addTillIdField != null) {
                addTillIdField.addEventListener("input", function () {
                    if (addTillIdField.value.length > maxLength) {
                        addTillIdField.value = addTillIdField.value.slice(0, maxLength)
                    }

                    if (addTillIdField.value > maxValue) {
                        addTillIdField.value = maxValue
                    }
                });
            }

            if (tillIdFilter != null) {
                tillIdFilter.addEventListener("input", function () {
                    if (tillIdFilter.value.length > maxLength) {
                        tillIdFilter.value = tillIdFilter.value.slice(0, maxLength)
                    }

                    if (tillIdFilter.value > maxValue) {
                        tillIdFilter.value = maxValue
                    }
                });
            }
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
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Till Assignment</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="tillAssignment" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-6 offset-3">
                <h2 id="page-title" class="mx-auto my-auto">Till Assignment</h2>
            </div>

            <div class="col-3 text-right">
                <a id="addTill" href="#" class="btn btn-wl mt-1" onclick="addTill();">Add Till</a>
                <a id="refresh" href="#" class="btn btn-wl mt-1" onclick="getTills();">Refresh</a>
            </div>
        </div>
    </section>

    <section id="filters-section" class="container-fluid">
        <div class="row mt-3">
            <div class="col-6">
                <div id="filters" class="card bg-light border-wl">
                    <div class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="collapseExample">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm">
                            <div class="form-group row">
                                <label for="storeIdFilter" class="col-2 col-form-label-sm text-right">Store</label>
                                <div class="col-3">
                                    <g:select name="storeIdFilter" from="${stores}" optionValue="storeId"
                                              optionKey="storeId"
                                              noSelection="${sec.loggedInUserInfo(field: 'storeId') ? ['': sec.loggedInUserInfo(field: 'storeNumber')] : ['': 'All']}"
                                              class="form-control select-border"
                                              disabled="${sec.loggedInUserInfo(field: 'storeId') ? true : false}"></g:select>
                                </div>

                                <label for="tillIdFilter" class="col-2 col-form-label-sm text-right">Till ID</label>
                                <div class="col-4">
                                        <g:field id="tillIdFilter" type="number" min="0" max="2147483647" name="tillIdFilter" value="${tillId}" class="form-control bottom-border" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="serialNumberFilter" class="col-2 col-form-label-sm text-right">Serial Number</label>
                                <div class="col-4">
                                    <g:textField name="serialNumberFilter" class="form-control bottom-border" value="${serialNumber}" autocomplete="off"/>
                                </div>

                                <div class="col-6 text-right">
                                    <button id="filter-clear-button" type="button" class="btn btn-danger text-right" onclick="clearFilters();">Clear</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getTills();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="tills-container" class="container-fluid mb-3">
        <div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
            <div class="col-1 font-weight-bold text-center">Store ID</div>
            <div class="col-1 font-weight-bold text-center">Till ID</div>
            <div class="col-2 font-weight-bold text-center">Serial Number</div>
        </div>

        <div id="results-container">

        </div>
    </section>

    <section id="addTill-modal" class="container-fluid">
        <!-- Add Till modal -->
        <div class="modal fade" id="addTillModal" tabindex="-1" role="dialog" aria-labelledby="addTillModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div id="addTillContent" class="modal-content"></div>
            </div>
        </div>
    </section>

    <section id="advancedConfiguration-modal" class="container-fluid">
        <!-- Advanced Configuration modal -->
        <div class="modal fade" id="advancedTillModal" tabindex="-1" role="dialog" aria-labelledby="advancedTillModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div id="advancedTillContent" class="modal-content"></div>
            </div>
        </div>
    </section>
</body>
</html>