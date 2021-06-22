<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane User Group</title>

    <script type="text/javascript">
        function selectGroup(groupId) {
            var URL = "${createLink(controller: 'group', action: 'selectGroup')}";

            window.location = URL +"/" +groupId;
        }
    </script>
</head>
<body>
    <div class="d-flex justify-content-center header-wl">
        <h2>Select Group</h2>
    </div>

    <g:if test="${flash.message}">
        <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div class="alert alert-danger alert-wl" role="alert">${flash.error}</div>
    </g:if>

    <div class="row" style="margin-top: 20px;">
        <p class="col-12 col-lg-8 offset-lg-2">You are a member of ${groups?.size()} ${groups?.size() == 1 ? "group" : "groups"}. Please select which group you would like to manage. To manage a single store please log out and enter the store ID on the login page.</p>
    </div>

    <div class="row mt-5 offset-3 col-6">
        <div class="col-6 font-weight-bold">Name</div>
        <div class="col-6 font-weight-bold">Type</div>
    </div>

    <div class="align-content-center">
        <g:if test="${!groups || groups?.size() == 0}">
            <div id="noResultsRow" class="col-10 pt-2 text-center">No results found.</div>
        </g:if>

        <g:each in="${groups}" var="group" status="i">
            <div class="row offset-3 col-6 pt-2 pb-2 wl-striped${i%2}" style="cursor: pointer;" onclick="selectGroup(${group.id});">
                <div class="col-6 <g:groupHierarchyPadding type="${group.type}" />">${group.name}</div>
                <div class="col-6">${group.type}</div>
            </div>
        </g:each>
    </div>
</body>
</html>