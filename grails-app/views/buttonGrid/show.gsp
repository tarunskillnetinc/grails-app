<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Button Grids</title>
    <script type="application/javascript">
        function fullSync(id) {
            if (confirm("This will fully sync tills, are you sure you want to continue?")) {
                var url = "${createLink(controller: 'buttonGrid', action: 'ajaxSyncButtonGrid')}";
                $.ajax({
                    url: url,
                    method: "POST",
                    data: {id: id},
                    success: function (params) {
                        alert("Sync message has been sent to tills.")
                    }
                });
            }
        }
    </script>
</head>
<body>
<g:if test="${flash.message}">
    <section id="message-container">
        <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
    </section>
</g:if>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item active" aria-current="page">Button Grids</li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">
                            <g:if test="${buttonGrid.type.name() == 'OTHER'}">
                                ${buttonGrid.description}
                            </g:if>
                            <g:else>
                                <g:message code="ButtonGridType.${buttonGrid.type.name()}" />
                            </g:else>
                        </li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <div class="header-wl mt-3">
        <h2 id="button-grid-page-title" class="mx-auto">
            <g:if test="${buttonGrid.type.name() == 'OTHER'}">
                ${buttonGrid.description}
            </g:if>
            <g:else>
                <g:message code="ButtonGridType.${buttonGrid.type.name()}" />
            </g:else>
        </h2>
    </div>

    <div class="col-12 col-lg-8 col-xl-6 offset-lg-2 offset-xl-3 mt-4 px-0 text-right" style="padding-right: 5px !important;">
        <g:if test="${storeId}">
        </g:if>
        <g:else>
            <g:link elementId="edit-button-grid-btn" action="edit" id="${buttonGrid.id}" class="btn btn-wl">Edit Button Grid</g:link>
        </g:else>
        <button name="sync-button-grid-btn" onclick="fullSync(${buttonGrid.id})" id="${buttonGrid.id}" class="btn btn-success">Sync Button Grid</button>
    </div>

    <div class="col-12 col-lg-8 col-xl-6 offset-lg-2 offset-xl-3 mt-4">
        <g:each var="row" in="${0 ..< buttonGrid.rows}" status="r">
            <div class="row button-grid">
                <g:each var="column" in="${0 ..< buttonGrid.columns}" status="c">
                    <%
                        def button = buttonGrid.buttons.find { it.row == row && it.column == column }
                    %>
                    <g:if test="${!button}"><!-- Unassigned buttons -->
                        <g:link controller="button" action="edit" id="0" params="[buttonGridId: buttonGrid.id, row: row, column: column]" class="no-underline col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container">
                            <div id="button-${c+1}-${r+1}" class="button-grid-button blank">
                                Unassigned Button
                            </div>
                        </g:link>
                    </g:if>
                    <g:else><!-- All other assigned buttons -->
                        <g:link controller="button" action="edit" id="${button.id}" class="no-underline col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container">
                            <div id="button-${c+1}-${r+1}" class="button-grid-button" style="background: ${button.bgColour}; color: ${button.textColour}; border: 2px ${button.type.name() == 'BLANK' ? 'dashed' : 'solid'} black;">
                                <g:if test="${button.imageDisplay && button.textDisplay}">
                                    <div class="button-grid-image-container">
                                        <g:buttonImage buttonId="${button.id}" />
                                    </div>
                                    <div class="button-grid-text-container">
                                        ${button.description}
                                    </div>
                                </g:if>
                                <g:elseif test="${button.imageDisplay}">
                                    <g:buttonImage buttonId="${button.id}" />
                                </g:elseif>
                                <g:else>
                                    ${button.description}
                                </g:else>

                            </div>
                        </g:link>
                    </g:else>
                </g:each>
            </div>
        </g:each>
    </div>

</body>
</html>