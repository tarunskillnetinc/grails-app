<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Suppliers & Affiliations</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="reporting.js" />

    <script type='text/javascript'>
        var getSymbolGroupSubscriptionsUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSymbolGroupSubscriptions')}";
        var addSymbolGroupSubscriptionUrl = "${createLink(controller: 'supplier', action: 'ajaxAddSymbolGroupSubscription')}";
        var editSymbolGroupSubscriptionUrl = "${createLink(controller: 'supplier', action: 'ajaxEditSymbolGroupSubscription')}";
        var saveSymbolGroupSubscriptionUrl = "${createLink(controller: 'supplier', action: 'ajaxSaveSymbolGroupSubscription')}";
        var getSymbolGroupFormUrl = "${createLink(controller: 'supplier', action: 'ajaxGetSymbolGroupForm')}";
        var symbolGroupActionUrl = "${createLink(controller: 'supplier', action: 'ajaxSymbolGroupAction')}"

        $(function() {
            getSymbolGroupSubscriptions();
        });


        function getSymbolGroupSubscriptions() {
            $("#subscriptions-search-results").hide();
            $("#subscriptions-loading-indicator").show();

            $.ajax({
                url: getSymbolGroupSubscriptionsUrl,
                method: "GET",
                success: function(resp) {
                    $("#subscriptions-results-container").html(resp);
                }
            });
        }

        function doSymbolGroupAction(symbolGroupId) {
            $.ajax({
                url: symbolGroupActionUrl,
                method: "GET",
                data: { symbolGroupId: symbolGroupId },
                success: function(resp) {
                    $("#errors-container").html('<div class="alert alert-success alert-wl mx-0" role="alert">' + resp + '</div>');
                },
                error: function (resp) {
                    $("#errors-container").html('<div class="alert alert-danger alert-wl mx-0" role="alert">' + resp + '</div>');
                }
            });
        }

        function rerenderAndEditSymbolGroupSubscription(symbolGroupSubscriptionId){
            $("#subscriptions-search-results").hide();
            $("#subscriptions-loading-indicator").show();

            $.ajax({
                url: getSymbolGroupSubscriptionsUrl,
                method: "GET",
                success: function(resp) {
                    $("#subscriptions-results-container").html(resp);
                   editSymbolGroupSubscription(symbolGroupSubscriptionId)
                }
            });
        }
        
        function rerenderAndShowAddSymbolGroupSubscriptionModal(){
            $("#subscriptions-search-results").hide();
            $("#subscriptions-loading-indicator").show();

            $.ajax({
                url: getSymbolGroupSubscriptionsUrl,
                method: "GET",
                success: function(resp) {
                    $("#subscriptions-results-container").html(resp);
                    showAddSymbolGroupSubscriptionModal()
                }
            });
        }

        function showAddSymbolGroupSubscriptionModal() {
            $("#addSymbolGroupSubscriptionContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addSymbolGroupSubscriptionModal').modal({ show: true });

            $.ajax({
                url: addSymbolGroupSubscriptionUrl,
                method: "GET",
                success: function(resp) {
                    $("#addSymbolGroupSubscriptionContent").html(resp);
                }
            });
        }

        function symbolGroupSubscripionSupplierChanged(symbolGroupId) {
            let form = $("#affiliationForm");
            form.hide();
            $.ajax({
                url: getSymbolGroupFormUrl,
                method: "GET",
                data: {
                    symbolGroupId: symbolGroupId,
                },
                success: function(resp) {
                    form.html(resp);
                    form.show();
                }
            });
        }

        function editSymbolGroupSubscription(symbolGroupSubscriptionId) {
            $("#addSymbolGroupSubscriptionContent").html("<div class=\"modal-body\"><div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div></div>");
            $('#addSymbolGroupSubscriptionModal').modal({ show: true });

            $.ajax({
                url: editSymbolGroupSubscriptionUrl,
                method: "GET",
                data: { symbolGroupSubscriptionId: symbolGroupSubscriptionId },
                success: function(resp) {
                    $("#addSymbolGroupSubscriptionContent").html(resp);
                }
            });
        }

        function saveSymbolGroupSubscription() {
            var formValues = $("#addSymbolGroupSubscriptionForm").serialize();
            $("#addSymbolGroupSubscriptionContent .modal-body").html("<div class=\"d-flex justify-content-center\"><div id=\"loadingIndicator\" class=\"spinner-border\" role=\"status\"><span class=\"sr-only\">Loading...</span></div></div>");
            $.ajax({
                url: saveSymbolGroupSubscriptionUrl,
                method: "POST",
                data: formValues,
                success: function(resp) {
                    if (resp === "OK") {
                        $('#addSymbolGroupSubscriptionModal').modal('hide')
                        getSymbolGroupSubscriptions();
                    } else {
                        $("#addSymbolGroupSubscriptionContent").html(resp);
                    }
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
                    <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Supplier Affiliations</li>
                </ol>
            </div>
        </div>
    </nav>
</section>

<section id="subscriptions-container" class="container-fluid">
    <div class="row header-wl mt-3">
        <div class="col-8 offset-2">
            <h2 id="affiliation-page-title" class="mx-auto my-auto">Supplier Affiliations</h2>
        </div>

        <div class="col-2 text-right">
            <a id="add-new-affiliation-btn" href="#" class="btn btn-wl"
               onclick="rerenderAndShowAddSymbolGroupSubscriptionModal();">Add New Affiliation</a>
        </div>
    </div>

    <section id="errors-container" class="container-fluid"></section>

    <div id="subscriptions-results-container" class="align-content-center">
        <g:render template="symbolGroupSubscriptionsSearchResults"/>
    </div>
</section>


<section id="addSymbolGroupSubscription-modal" class="container-fluid">
    <!-- Add symbol group subscription modal -->
    <div class="modal fade" id="addSymbolGroupSubscriptionModal" tabindex="-1" role="dialog"
         aria-labelledby="addSymbolGroupSubscriptionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div id="addSymbolGroupSubscriptionContent" class="modal-content">

            </div>
        </div>
    </div>
</section>

</body>
</html>