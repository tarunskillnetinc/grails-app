<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Product Attributes</title>

    <style>
    .checkbox-container {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .checkbox-container::before {
        content: '';
        display: inline-block;
        height: 100%;
        vertical-align: middle;
    }

    .checkbox-container .wl-checkbox {
        margin: 0;
        vertical-align: middle;
    }

    .modal-dialog {
        display: flex;
        align-items: center;
        min-height: calc(100% - 1rem);
    }

    @media (min-width: 576px) {
        .modal-dialog {
            min-height: calc(100% - 3.5rem);
        }
    }

    #search-results .text-center {
        display: grid;
        align-items: center;
    }

    #search-results .row {
        height: 3.3em;
    }

    </style>
    <asset:javascript src="jquery-ui.js" />
</head>

<body>
<section id="breadcrumb-container" class="container-fluid">
    <nav aria-label="breadcrumb">
        <div class="row mt-4">
            <div class="col">
                <ol class="breadcrumb">
                    <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Product Attribute Management</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto my-auto">Product Attribute Management</h2>
        </div>

        <div class="col-2 text-right">
            <g:link elementId="cancel-btn" uri="/" tabindex="-1"
                    role="button" class="btn btn-wl ml-1"
                    onclick="return confirm('Are you sure you want to cancel? Any unsaved changes will be lost.');">Cancel</g:link>
            <button id="save-btn" class="btn btn-success ml-1" name="save"
                    onclick="submitForm('${productAttributes?.size() > 0 ? productAttributes?.get(0)?.name:''}');">Save</button>
        </div>
    </div>
    <div class="row">
        <div class="col-12 text-right mt-3">
            <g:link elementId="add-product-attribute-btn"  controller="productAttributes" action="addProductAttribute" tabindex="-1" role="button"
                    class="btn btn-wl ml-1">Add Product Attribute</g:link>
        </div>
    </div>
    
    <div id="error-container" class="container-fluid mt-3">
    </div>
    <div>
        <div id="results-container" class="align-content-center">
            <g:render template="productAttributesResultsView"
                      model="[productAttributes: productAttributes, productAttributesCount: productAttributesCount]"/>
        </div>
    </div>
</section>

<section id="addListItem-modal" class="container-fluid">
    <div class="modal fade" id="addListItemModal" tabindex="-1" role="dialog" aria-labelledby="addListItemModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
            <div id="addListItemContent" class="modal-content"></div>
        </div>
    </div>
</section>

<script type="text/javascript">
    const addAttributeListItemURL = "${createLink(controller: 'productAttributes', action: 'ajaxAddAttributeListItem')}";
    const saveAttributeListItemURL = "${createLink(controller: 'productAttributes', action: 'saveAttributeListItem')}";

    let displayAttributeUpdates = [];
    let defaultValueUpdates = [];

    function addListItem(attributeId) {
        $("#addListItemContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
        $('#addListItemModal').modal({show: true, backdrop: 'static', keyboard: false});
        $.ajax({
            url: addAttributeListItemURL + "?attributeId=" + attributeId,
            method: "GET",
            success: function (resp) {
                $("#addListItemContent").html(resp);
            }
        });
    }

    function closeModal() {
        if (confirm("All unsaved changes will be lost, are you sure you want to cancel?")) {
            $('#addListItemModal').modal('hide')
        }
    }

    function saveListItem() {
        var formValues = $("#addListItemForm").serialize();
        $("#loading-indicator").show();
        $.ajax({
            url: saveAttributeListItemURL,
            method: "POST",
            data: formValues,
            success: function (resp) {
                $("#loading-indicator").hide();
                if (resp === "OK") {
                    $("#addListItemContent").html('');
                    $('#addListItemModal').modal('hide');
                    reloadCurrentPage();
                } else {
                    listItemUpdateGenericError();
                }
            },
            error: function(xhr, status, error) {
                if (xhr.status === 400) {
                    $("#modal-error").html(xhr.responseText);
                } else {
                    listItemUpdateGenericError();
                }
            }
        });
    }

    function listItemUpdateGenericError() {
        $("#modal-error").html("<div class='alert alert-danger'>An error occurred while saving list item of the product attribute. Please try again.</div>");
    }

    function reloadCurrentPage(callback) {
        $("#error-section").html('');
        $("#success-section").html('');
        var currentPage = $('.currentStep').text() || 1;
        var currentMax = ${max ?: 50};
        var currentOffset = (currentPage - 1) * currentMax;

        $.ajax({
            url: '${createLink(controller: 'productAttributes', action: 'ajaxProductAttributes')}',
            data: {
                offset: currentOffset,
                max: currentMax
            },
            success: function(response) {
                $('#results-container').html(response);

                // Ensure the correct page is highlighted after reload
                $('.step').removeClass('current');
                $('.step:contains("' + currentPage + '")').addClass('current');
                if (callback) callback();
            },
            error: function() {
                alert('An error occurred while reloading the page.');
            }
        });
    }

    function submitForm(attributeName) {
        if (displayAttributeUpdates.length > 0 || defaultValueUpdates.length > 0) {
            if (confirm('Are you sure you want to save these changes?')) {
                let updates = [...displayAttributeUpdates, ...defaultValueUpdates];

                $.ajax({
                    url: '${createLink(controller: 'productAttributes', action: 'bulkUpdateAttributes')}',
                    method: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({updates: updates}),
                    success: function (response) {
                        if (response.success) {
                            displayAttributeUpdates = [];
                            defaultValueUpdates = [];
                            reloadCurrentPage(function () {
                                let attributes = ' attributes.'
                                if( response.updatedCount === 1 ) {
                                    attributes = ' attribute.'
                                }
                                $("#success-section").html('<div class="alert alert-success alert-wl mx-0" role="alert">Successfully updated ' + response.updatedCount + attributes + '</div>');
                            });
                        } else {
                            $("#error-section").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + response.errorMessages.general + '</div>');
                            console.error('Update errors:', response.errors);
                        }
                    },
                    error: function (xhr, status, error) {
                        $("#error-section").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + xhr.errorMessage + '</div>');
                    }
                });
            }
        }
    }

    function applyTemporaryStates() {
        if (typeof displayAttributeUpdates !== 'undefined') {
            displayAttributeUpdates.forEach(function(update) {
                var checkbox = $('#displayAttribute_' + update.id);
                if (checkbox.length) {
                    checkbox.prop('checked', update.displayAttribute);
                }
            });
        }

        if (typeof defaultValueUpdates !== 'undefined') {
            defaultValueUpdates.forEach(function(update) {
                var select = $('#defaultValue_' + update.id);
                if (select.length) {
                    select.val(update.defaultValue);
                }
            });
        }
    }

    function updateDisplayAttributeState(attributeId, isChecked) {
        if (typeof displayAttributeUpdates === 'undefined') {
            displayAttributeUpdates = [];
        }

        let index = displayAttributeUpdates.findIndex(u => u.id === attributeId);
        if (index !== -1) {
            displayAttributeUpdates[index].displayAttribute = isChecked;
        } else {
            displayAttributeUpdates.push({id: attributeId, displayAttribute: isChecked});
        }
    }

    function updateDefaultValueState(attributeId, newDefaultValue) {
        if (typeof defaultValueUpdates === 'undefined') {
            defaultValueUpdates = [];
        }

        let index = defaultValueUpdates.findIndex(u => u.id === attributeId);
        if (index !== -1) {
            defaultValueUpdates[index].defaultValue = newDefaultValue;
        } else {
            defaultValueUpdates.push({id: attributeId, defaultValue: newDefaultValue});
        }
    }
</script>

</body>
</html>