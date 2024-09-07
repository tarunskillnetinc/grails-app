<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Store Configuration</title>
    <asset:javascript src="validators/input-validator.js"/>
    <asset:javascript src="store-settings/color-pick.js" />

    <script type="text/javascript">
        function updateColorIndicator(color, indicatorId) {
            var colorPickerElement = document.getElementById(indicatorId);
            colorPickerElement.value = "#" + color; // Prepend "#" to the color value
        }

        $(document).ready(function() {
            // Select the first tab by default if none are active
            if (!$('.nav-link.active').length) {
                $('#store-tab').addClass('active');
                $('#store-container').addClass('active');
            }

            // Detect tab change event
            $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                var selectedTab = $(e.target).attr('id');  // Get the ID of the selected tab
                console.log("Selected tab: " + selectedTab);

            });

            $.get("${createLink(controller: 'cashManagement', action: 'index')}?storeId=" + ${storeSettings?.id} + "&onlyRetailerLevel=false", function(data) {
                $('#cash-container').html(data);
            });

        });
    </script>

    <style>
        .tooltip-trigger {
            position: relative;
            cursor: pointer;
            display: inline-block;
            width: 25px; /* Adjust size as needed */
            height: 25px; /* Adjust size as needed */
            border-radius: 50%; /* Makes it round */
            background-color: lightblue; /* Light blue background color */
            text-align: center; /* Centers the '?' mark */
            line-height: 25px; /* Vertically centers the '?' mark */
        }

        .tooltip-trigger:hover .tooltip-content {
            display: inline-block;
        }
    </style>

</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <sec:ifAnyGranted roles='ROLE_ENGINEER, ROLE_HEAD_OFFICE'>
                            <g:if test="${!sec.loggedInUserInfo(field: 'storeId')}">
                                <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page"><g:link controller="store" action="index" params="[storeNumberFilter: storeNumberFilter, storeNameFilter: storeNameFilter, showDeletedFilter: showDeletedFilter, max: max, offset: offset, sort: sort, order: order]">Store Management</g:link></li>
                            </g:if>
                        </sec:ifAnyGranted>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Store ${storeSettings?.config?.storeNumber} Configuration</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>
    <section id="tab-container" class="container-fluid">
        <div class="row mt-0">
            <div class="col-4">
                <ul class="nav nav-tabs nav-fill tabs-wl mx-4" role="tablist">
                    <li class="nav-item">
                        <a id="store-tab" data-toggle="tab" href="#store-container" aria-selected="true" role="tab" aria-controls="store-container" class="nav-link ${ tabType.equals('store' ? 'active' : 'disabled')}">Store Config</a>
                    </li>
                    <li class="nav-item">
                        <a id="cash-tab" data-toggle="tab" href="#cash-container" role="tab" aria-controls="cash-container" class="nav-link ${tabType.equals('cash' ? 'active' : 'disabled')}">Cash Management</a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="ui-menu-divider w-100"/>
        <div class="tab-content">
            <div id="store-container" class="tab-pane ${tabType.equals('store') ? 'active' : ''}">
                <g:render template="storeConfig" model='${pageScope}'/>
            </div>
            <div id="cash-container" class="tab-pane ${tabType.equals('cash') ? 'active' : ''}">

            </div>
        </div>
    </section>

</body>
</html>