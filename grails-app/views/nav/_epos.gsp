<%@ page import="uk.co.wonderlane.wlpos.enums.ButtonGridType" %>

<ul class="nav nav-pills nav-fill pills-wl">
    <li class="nav-item">
        <g:link controller="tillSettings" class="nav-link ${active.equals('tillsettings') ? 'active' : ''}">Till Settings</g:link>
    </li>

    <li class="nav-item dropdown">
        <a class="nav-link dropdown-toggle ${active.equals('quicksell') ? 'active' : ''}" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false">Quicksell Buttons</a>
        <div class="dropdown-menu">
            <g:link controller="buttonGrid" action="show" params="[type: ButtonGridType.SALES]" class="dropdown-item">Sales</g:link>
            <g:link controller="buttonGrid" action="show" params="[type: ButtonGridType.QUICK_SELL]" class="dropdown-item">Quicksell</g:link>
            <g:link controller="buttonGrid" action="show" params="[type: ButtonGridType.TENDER]" class="dropdown-item">Tender</g:link>
            <g:link controller="buttonGrid" action="show" params="[type: ButtonGridType.MANAGER_FUNCTIONS]" class="dropdown-item">Manager Functions</g:link>
            <div class="dropdown-divider"></div>
            <g:quicksellMenu />
            <g:link controller="buttonGrid" action="show" class="dropdown-item">+ Add Page</g:link>
        </div>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Product</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Option 1</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Option 2</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Option 3</g:link>
    </li>
</ul>