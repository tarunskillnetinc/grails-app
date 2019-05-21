<!doctype html>
<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonGridType" %>
<html>
<head>
    <meta name="layout" content="main" />

    <title>Well Pharmacy Button Grids</title>
</head>
<body>

    <g:render template="/nav/epos" model="[active: 'quicksell']" />

    <div class="d-flex justify-content-center header-wl">
        <h2><g:message code="ButtonGridType.${buttonGrid.type.name()}" /></h2>
    </div>

    <div class="col-12 col-lg-6 offset-lg-3">
        <g:each var="row" in="${0..<buttonGrid.rows}">
            <div class="d-flex justify-content-center align-items-stretch button-grid">
                <g:each var="column" in="${0..<buttonGrid.columns}">
                    <%
                        def button = buttonGrid.buttons.find { it.row == row && it.column == column }
                    %>
                    <div class="d-flex flex-fill button-grid-button ${button ? '' : 'blank'} justify-content-center align-items-center">
                        <g:link controller="button" action="edit" id="${button?.id ?: 0}" params="[buttonGridId: buttonGrid.id, row: row, column: column]">
                            ${button?.description ?: "Unassigned Button"}
                        </g:link>
                    </div>
                </g:each>
            </div>
        </g:each>
    </div>

</body>
</html>