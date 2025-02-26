<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="uk.co.wonderlane.wlpos.enums.ReasonCodeType" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Reason Code Maintenance</title>

    <asset:javascript src="jquery-ui.js" />
    <asset:stylesheet src="jquery-ui.css" />

    <style>
    #sortable {
        padding-left: 0
    }

    #sortable li {
        cursor: grab
    }

    #sortable .non-sortable .grabhandle {
        visibility: hidden;
    }

    #sortable .non-sortable {
        cursor: auto;
    }

    .grabhandle {
        display: inline-block;
        fill: #AAA;
        height: 1em;
        width: 1em;
        position: relative;
        top: 0.7em;
    }
    </style>

    <script type="application/javascript">
        const searchUrl = "${createLink(controller: 'reasonCode', action: 'ajaxSearch')}";
        const editUrl = "${createLink(controller: 'reasonCode', action: 'ajaxEditReasonCode')}";
        const addUrl = "${createLink(controller: 'reasonCode', action: 'ajaxAddReasonCode')}";
        const saveUrl = "${createLink(controller: 'reasonCode', action: 'ajaxSaveReasonCode')}";
        const deleteUrl = "${createLink(controller: 'reasonCode', action: 'ajaxDeleteReasonCode')}"
        const reinstateUrl = "${createLink(controller: 'reasonCode', action: 'ajaxReinstateReasonCode')}"
        const saveReorderUrl = "${createLink(controller: 'reasonCode', action: 'ajaxSaveReorderReasonCode')}"
        const reasonCodeTypeProductList = "${ReasonCodeType.PRODUCT_LIST.name()}";

        let modalContents;
        let modal;
        let errorMsg;
        let successMsg;
        let reasonCodeNewOrders;

        $(document).ready(function() {
            modalContents = $('#edit-code-content');
            modal = $('#edit-code-modal');
            errorMsg = $('#error-message');
            successMsg = $('success-message');

            ajaxSearch();
            updateDirectionColumnVisibility();

            $('#collapseReasonCodeHistory').on('show.bs.collapse', function () {
                getReasonCodeHistory();
            });
        });

        function ajaxSaveReorder() {
            clearSuccessMsg();
            clearErrorMsg();
            $.ajax({
                url: saveReorderUrl,
                type: "POST",
                data: {order: reasonCodeNewOrders},
                success: function (response) {
                    showSuccessMsg('Order saved.');
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorMsg('Error updating reason code details.');
                    console.log(thrownError)
                }
            })
        }

        function ajaxSearch(sortParams) {
            clearSuccessMsg();
            clearErrorMsg();

            const searchResults = $('#search-results');

            searchResults.html(
                `<div class=\"d-flex justify-content-center\">
                    <div class=\"spinner-border\" role=\"status\">
                        <span class=\"sr-only\">Loading...</span>
                    </div>
                </div>`
            );

            $.ajax({
                url: searchUrl,
                data: {
                    type: $("#code-type-select").val(),
                    deleted: showDeletedFilter.checked,
                    max: sortParams ? sortParams["max"] : null,
                    offset: sortParams ? sortParams.offset : null,
                    sortColumn: sortParams ? sortParams.sortColumn : null,
                    sortOrder: sortParams ? sortParams.sortOrder : null
                },
                success: function (resp) {
                    searchResults.html(resp);
                },
                error: function () {
                    searchResults.html(`
                        <div class="px-0 text-center">
                            <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
                        </div>`
                    );
                }
            });

            updateDirectionColumnVisibility();
        }

        function updateDirectionColumnVisibility() {
            const selectedType = $("#code-type-select").val();
            const directionColumn = $("#direction-column");
            const directionColumnHeader = $("#direct-column-header");

            if (selectedType === reasonCodeTypeProductList) {
                directionColumn.show();
                directionColumnHeader.show();
            } else {
                directionColumn.hide();
                directionColumnHeader.hide();
            }
        }

        function ajaxEdit(id) {
            clearSuccessMsg();
            clearErrorMsg();

            setupModal();
            $.ajax({
                url: editUrl,
                method: "GET",
                data: {id: id},
                success: function (resp) {
                    modalContents.html(resp);
                    updateAdditionalFuncSection();
                },
                error: function () {
                    closeModal();
                    showErrorMsg('Error retrieving reason code details.');
                }
            });
        }

        function ajaxAdd() {
            clearErrorMsg();
            clearSuccessMsg()

            setupModal();
            $.ajax({
                url: addUrl,
                method: "GET",
                success: function (resp) {
                    modalContents.html(resp);
                    updateAdditionalFuncSection();
                },
                error: function () {
                    closeModal();
                    showErrorMsg('Error retrieving reason code details.');
                }
            });
        }

        function ajaxSave() {
            const data = $('#edit-code-form').serialize()
            clearSuccessMsg()
            clearErrorMsg();

            setupModal();
            hideSaveBtns();

            $.ajax({
                url: saveUrl,
                method: "POST",
                data: data,
                success: function (resp) {
                    if (resp === "OK") {
                        closeModal();
                        ajaxSearch();
                    } else {
                        showSaveBtns();
                        modalContents.html(resp);
                        updateAdditionalFuncSection();
                    }
                },
                error: function () {
                    closeModal();
                    showErrorMsg('Error occurred trying to save reason code details.');
                }
            });
        }

        function ajaxDelete(id, desc) {
            clearSuccessMsg()
            clearErrorMsg();

            if (confirm('This will delete reason code "' + desc + '"')) {
                $.ajax({
                    url: deleteUrl,
                    method: "DELETE",
                    data: {id: id},
                    success: function (resp) {
                        if (resp === "OK") {
                            ajaxSearch();
                            showSuccessMsg("Reason code " + desc + " deleted")
                        } else {
                            showErrorMsg(resp);
                        }
                    },
                    error: function () {
                        showErrorMsg('Error occurred trying to delete reason code.');
                    }
                });
            }
        }

        function ajaxReinstate(id, desc) {
            clearSuccessMsg()
            clearErrorMsg();

            if (confirm('This will reinstate reason code "' + desc + '"')) {
                $.ajax({
                    url: reinstateUrl,
                    method: "PUT",
                    data: {id: id},
                    success: function (resp) {
                        if (resp === "OK") {
                            ajaxSearch();
                            showSuccessMsg("Reason code " + desc + " reinstated")
                        } else {
                            showErrorMsg(resp);
                        }
                    },
                    error: function () {
                        showErrorMsg('Error occurred trying to reinstate reason code.');
                    }
                });
            }
        }

        function setupModal() {
            modalContents.html(
                `<div class=\"modal-body\">
                    <div class=\"d-flex justify-content-center\">
                        <div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\">
                            <span class=\"sr-only\">Loading...</span>
                        </div>
                    </div>
                </div>`);
            modal.modal({show: true, backdrop: 'static', keyboard: false});
        }

        function closeModal() {
            modal.modal('hide');
        }

        function clearErrorMsg() {
            errorMsg.text('');
            errorMsg.hide();
        }

        function showErrorMsg(msg) {
            errorMsg.text(msg);
            errorMsg.show();
        }

        function clearSuccessMsg() {
            successMsg.text('');
            successMsg.hide();
        }

        function showSuccessMsg(msg) {
            successMsg.text(msg);
            successMsg.show();
        }

        function hideSaveBtns() {
            $("#cancel-edit-btn").hide()
            $("#save-code-btn").hide()
        }

        function showSaveBtns() {
            $("#cancel-edit-btn").show()
            $("#save-code-btn").show()
        }

        function getReasonCodeHistory() {
            $('#reasonCodeHistoryContainer').html("<div class=\"d-flex justify-content-center\">\n" +
                "  <div class=\"spinner-border\" role=\"status\">\n" +
                "    <span class=\"sr-only\">Loading...</span>\n" +
                "  </div>\n" +
                "</div>");

            var getReasonCodeHistoryUrl = "${createLink(controller: 'reasonCode', action: 'ajaxGetReasonCodeHistory')}";

            $.ajax({
                url: getReasonCodeHistoryUrl,
                method: "GET",
                data: {},
                success: function (resp) {
                    $("#reasonCodeHistoryContainer").html(resp);
                }
            });
        }

        $(function () {
            $("#sortable").sortable();
        });
    </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Reason Code Maintenance</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="reasonCodeMaintenance" class="container-fluid">
        <div class="row header-wl mt-3">
            <div class="col-8 offset-2">
                <h2 id="page-title" class="mx-auto">Reason Code Maintenance</h2>
            </div>
            <div class="col-2 text-right">
                <button id="add-new-category" class="btn btn-wl" onclick="ajaxAdd();">Add Reason Code</button>
            </div>
        </div>

        <div class="alert alert-success alert-wl mx-0" role="alert" id="success-message" style="display: none"></div>
        <div class="alert alert-danger alert-wl mx-0" role="alert" id="error-message" style="display: none"></div>

        <div class="row mt-4">
            <div class="col-6">
                <div class="card bg-light border-wl">
                    <div id="filters-collapse" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="true" aria-controls="filterCollapse">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <div class="card-body collapse show" id="filterCollapse">
                        <div class="form-group row">
                            <label for="code-type-select" class="col-2 col-form-label-sm text-right">Reason Code Type</label>
                            <div class="col-10 input-group">
                                <select name="code-type" id="code-type-select" onchange="ajaxSearch()" class="col-6 form-control select-border">
                                    <option value="CUSTOMER_REFUSAL">Customer Refusal</option>
                                    <option value="LINE_VOID">Line Void</option>
                                    <option value="MARKDOWN">Markdown</option>
                                    <option value="PAID_OUT">Paid Out</option>
                                    <option value="PAID_IN">Paid In</option>
                                    <option value="PRODUCT_LIST">Product List</option>
                                    <option value="REFUND">Refund</option>
                                    <option value="TENDER_RECONCILIATION_VARIANCE">Tender Reconciliation Variance</option>
                                    <option value="TENDER_RECONCILIATION_SAFE_VARIANCE">Tender Reconciliation Safe Variance</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group row mb-0 mt-6">
                            <div class="col-6 offset-2">
                                <g:checkBox id="showDeletedFilter" name="showDeletedFilter"
                                            class="form-check-input wl-checkbox ml-0 pointer"
                                            checked="${showDeletedFilter}" onchange="ajaxSearch()"/>
                                <label for="showDeletedFilter"
                                       class="col-form-label-sm wl-label-right pointer">Show deleted reason codes</label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-auto ml-auto mr-3">
                <button id="save-btn" class="btn btn-success ml-1" name="save"
                        onclick="ajaxSaveReorder();">Save</button>
            </div>
        </div>
        <div id="search-results">
		<div class="row mt-5 pb-2 ml-0 mr-0 table-wl bottom-border">
		    <div class="col-4 font-weight-bold">Description</div>
		    <div class="col-2 font-weight-bold" id="direction-column" style="display: none;">Direction</div>
		    <div class="col-4 font-weight-bold">Secret</div>
		    <div class="col-4 font-weight-bold"></div>
		</div>
            <div class="px-0 text-center">
                <div id="noResultsRow" class="pt-2 pb-2 text-center my-auto wl-striped0">No results found.</div>
            </div>
        </div>

        <section id="edit-modal" class="container-fluid">
            <!-- Advanced Configuration modal -->
            <div class="modal fade" id="edit-code-modal" tabindex="-1" role="dialog" aria-labelledby="edit-code-lbl"
                 aria-hidden="true">
                <div class="modal-dialog modal-lg" role="document">
                    <div id="edit-code-content" class="modal-content"></div>
                </div>
            </div>
        </section>
</section>
<section id="accordion" class='container-fluid'>
    <!-- ReasonCode History. -->
    <div class="card bg-light border-wl accordion-card">
        <div class="card-header pointer" id="reasonCodeHistory" data-toggle="collapse"
             data-target="#collapseReasonCodeHistory" aria-expanded="true" aria-controls="collapseReasonCodeHistory">
            <div class="row">
                <div class="col-10"><strong>Reason Code History</strong></div>

                <div class="col-2 text-right">
                    <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right"
                         fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                        <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                    </svg>
                </div>
            </div>
        </div>

        <div id="collapseReasonCodeHistory" class="collapse collapsed" aria-labelledby="categoryReasonCodeHistory"
             data-parent="#accordion">
            <div class="card-body py-5">
                <div id="reasonCodeHistoryContainer"
                     style="max-height: 300px; overflow-x: auto; overflow-y: auto;"></div>
            </div>
        </div>
    </div>
</section>
</body>
</html>
