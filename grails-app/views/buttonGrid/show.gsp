<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane Button Grids</title>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li class="breadcrumb-item active" aria-current="page">Button Grids</li>
                        <li class="breadcrumb-item active" aria-current="page">
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
        <g:link elementId="edit-button-grid-btn" action="edit" id="${buttonGrid.id}" class="btn btn-wl">Edit Button Grid</g:link>
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
                    <g:elseif test="${button.type.name() == 'TENDER' && button.tenderType.name() == 'CASH' && !button.description}"><!-- Exact cash button -->
                        <div id="button-${c+1}-${r+1}" class=" col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container" style="color: #000000;">
                            <div class="button-grid-button blank">Exact</div>
                        </div>
                    </g:elseif>
                    <g:elseif test="${button.buttonGrid?.type?.name() == 'TENDER' && button.type.name() == 'PROCESS'}"><!-- Tender back button -->
                        <div id="button-${c+1}-${r+1}" class="col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container" style="color: #000000;">
                            <div class="button-grid-button blank">${button.description}</div>
                        </div>
                    </g:elseif>
                    <g:else><!-- All other assigned buttons -->
                        <g:link controller="button" action="edit" id="${button.id}" class="no-underline col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container">
                            <div id="button-${c+1}-${r+1}" class="button-grid-button" style="background: ${button.bgColour}; color: ${button.textColour}; border: 2px solid black;">
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