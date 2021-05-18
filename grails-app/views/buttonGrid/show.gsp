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

    <div class="row header-wl mt-3">
        <h2 class="mx-auto">
            <g:if test="${buttonGrid.type.name() == 'OTHER'}">
                ${buttonGrid.description}
            </g:if>
            <g:else>
                <g:message code="ButtonGridType.${buttonGrid.type.name()}" />
            </g:else>
        </h2>
    </div>

    <div class="col-12 col-lg-8 col-xl-6 offset-lg-2 offset-xl-3" style="margin-top: 25px;">
        <g:each var="row" in="${0 ..< buttonGrid.rows}">
            <div class="row button-grid">
                <g:each var="column" in="${0 ..< buttonGrid.columns}">
                    <%
                        def button = buttonGrid.buttons.find { it.row == row && it.column == column }
                    %>
                    <g:if test="${!button}"><!-- Unassigned buttons -->
                        <g:link controller="button" action="edit" id="0" params="[buttonGridId: buttonGrid.id, row: row, column: column]" class="no-underline col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container">
                            <div class="button-grid-button blank">
                                Unassigned Button
                            </div>
                        </g:link>
                    </g:if>
                    <g:elseif test="${button.type.name() == 'TENDER' && button.tenderType.name() == 'CASH' && !button.description}"><!-- Exact cash button -->
                        <div class=" col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container" style="color: #000000;">
                            <div class="button-grid-button blank">Exact</div>
                        </div>
                    </g:elseif>
                    <g:elseif test="${button.buttonGrid?.type?.name() == 'TENDER' && button.type.name() == 'PROCESS'}"><!-- Tender back button -->
                        <div class="col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container" style="color: #000000;">
                            <div class="button-grid-button blank">${button.description}</div>
                        </div>
                    </g:elseif>
                    <g:else><!-- All other assigned buttons -->
                        <g:link controller="button" action="edit" id="${button.id}" class="no-underline col-6 col-sm-${(12 / buttonGrid.columns)} button-grid-container">
                            <div class="button-grid-button">
                                ${button.description}
                            </div>
                        </g:link>
                    </g:else>
                </g:each>
            </div>
        </g:each>
    </div>

</body>
</html>