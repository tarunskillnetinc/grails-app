<ul class="nav nav-pills nav-fill pills-wl">
    <li class="nav-item">
        <g:link controller="reporting" action="salesDepartments" class="nav-link ${active.equals('sales') ? 'active' : ''}">Sales</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Promotions</g:link>
    </li>

    <li class="nav-item">
        <g:link controller="reporting" action="tillControlEvents" class="nav-link ${active.equals('tillControlEvents') ? 'active' : ''}">Till Control Events</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Stock Movement</g:link>
    </li>

    <li class="nav-item">
        <g:link url="/" class="nav-link disabled">Journal</g:link>
    </li>
</ul>