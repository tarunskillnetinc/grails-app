<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy Button Grids</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'quicksell']" />

    <div class="d-flex justify-content-center header-wl">
        <h2>
            <g:if test="${buttonGrid.type.name() == 'OTHER'}">
                ${buttonGrid.description}
            </g:if>
            <g:else>
                <g:message code="ButtonGridType.${buttonGrid.type.name()}" />
            </g:else>
        </h2>
    </div>

    <div class="col-12 col-lg-6 offset-lg-3">
        <g:each var="row" in="${0..<buttonGrid.rows}">
            <div class="d-flex justify-content-center align-items-stretch button-grid">
                <g:each var="column" in="${0..<buttonGrid.columns}">
                    <%
                        def button = buttonGrid.buttons.find { it.row == row && it.column == column }
                    %>
                    <g:if test="${!button}">
                        <div class="d-flex flex-fill button-grid-button blank justify-content-center align-items-center">
                            <g:link controller="button" action="edit" id="0" params="[buttonGridId: buttonGrid.id, row: row, column: column]">Unassigned Button</g:link>
                        </div>
                    </g:if>
                    <g:elseif test="${button.type.name() == 'TENDER' && button.tenderType.name() == 'CASH' && !button.description}">
                        <div class="d-flex flex-fill button-grid-button blank justify-content-center align-items-center" style="color: #000000;">
                            Exact
                        </div>
                    </g:elseif>
                    <g:else>
                        <div class="d-flex flex-fill button-grid-button justify-content-center align-items-center">
                            <g:link controller="button" action="edit" id="${button.id}">${button.description}</g:link>
                        </div>
                    </g:else>
                </g:each>
            </div>
        </g:each>
    </div>

</body>
</html>