<div class="row mt-4 offset-3 col-6 px-0">
    <div class="col-9 text-right px-0">
        <g:groupBreadcrumb groupId="${parentId}" />
    </div>

    <div class="col-3 text-right px-0">
        <g:link controller="group" action="add" class="btn btn-wl">Add New Group</g:link>
    </div>
</div>

<div class="row mt-4 offset-3 col-6 pb-2 table-wl bottom-border">
    <div class="col-4 font-weight-bold">Type</div>
    <div class="col-4 font-weight-bold">Name</div>
</div>

<g:if test="${!groups || groups?.size() == 0}">
    <div id="noResultsRow" class="col-6 offset-3 pt-2 pb-2 text-center wl-striped0">No results found.</div>
</g:if>

<g:each in="${groups}" var="group" status="i">
    <div class="row offset-3 col-6 pt-2 pb-2 wl-striped${i%2} hoverable" style="cursor: pointer;" onclick="navigateToGroup(${group.id}, ${group.level.level});">
        <div class="col-4">${group.level.name}</div>
        <div class="col-8">${group.name}</div>
    </div>
</g:each>