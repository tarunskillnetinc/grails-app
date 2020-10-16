<ul class="nav nav-pills nav-fill pills-wl">
    <li class="nav-item">
        <g:link controller="reporting" action="salesDepartments" class="nav-link ${active.equals('sales') ? 'active' : ''}">Sales</g:link>
    </li>

    <li class="nav-item">
        <g:link controller="reporting" action="promotions" class="nav-link ${active.equals('promotion') ? 'active' : ''}">Promotions</g:link>
    </li>

    <li class="nav-item">
        <g:link controller="reporting" action="tillControls" class="nav-link ${active.equals('promotion') ? 'active' : ''}">Till Controls</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Stock Movement</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Journal</g:link>
    </li>
</ul>