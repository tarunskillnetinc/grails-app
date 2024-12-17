<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Retailer Product Attributes</title>

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
    </style>
    <asset:javascript src="jquery-ui.js" />
    <script type="text/javascript">
        const addAttributeListItemURL = "${createLink(controller: 'product', action: 'ajaxAddAttributeListItem')}";
        const saveAttributeListItemURL = "${createLink(controller: 'product', action: 'saveAttributeListItem')}";

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

        function reloadCurrentPage() {
            var currentPage = $('.currentStep').text() || 1;
            var currentMax = ${max ?: 50};
            var currentOffset = (currentPage - 1) * currentMax;

            $.ajax({
                url: '${createLink(controller: 'product', action: 'ajaxProductAttributes')}',
                data: {
                    offset: currentOffset,
                    max: currentMax
                },
                success: function(response) {
                    $('#results-container').html(response);

                    // Ensure the correct page is highlighted after reload
                    $('.step').removeClass('current');
                    $('.step:contains("' + currentPage + '")').addClass('current');
                },
                error: function() {
                    alert('An error occurred while reloading the page.');
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
                    <li id="breadcrumb-2" class="breadcrumb-item active"
                        aria-current="page">Retailer Product Attributes</li>
                </ol>
            </div>
        </div>
    </nav>
</section>
<section id="header-container" class="container-fluid">
    <div class="row header-wl mt-0">
        <div class="col-8 offset-2">
            <h2 id="page-title" class="mx-auto my-auto">Retailer Product Attributes</h2>
        </div>

        <div class="col-12 text-right mt-3">
            <div class="d-flex justify-content-end align-items-center">
                <g:link elementId="cancel-btn" controller="product" action="productAttributes" tabindex="-1"
                        role="button" class="btn btn-wl ml-1">Cancel</g:link>
            </div>
        </div>

        <div class="col-12 text-right mt-3">
            <div class="d-flex justify-content-end align-items-center">
                <g:link controller="product" action="addProductAttribute" tabindex="-1" role="button"
                        class="btn btn-wl ml-1">Add Product Attribute</g:link>
            </div>
        </div>
    </div>

    <div>
        <div id="results-container" class="align-content-center">
            <g:render template="productAttributesResultsView"
                      model="[productAttributes: productAttributes, productAttributesCount: productAttributesCount]"/>
        </div>
</section>

<section id="addListItem-modal" class="container-fluid">
    <div class="modal fade" id="addListItemModal" tabindex="-1" role="dialog" aria-labelledby="addListItemModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
            <div id="addListItemContent" class="modal-content"></div>
        </div>
    </div>
</section>

</body>

</html>