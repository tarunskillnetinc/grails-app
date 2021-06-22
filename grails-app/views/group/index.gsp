<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane User Groups</title>

    <script type="text/javascript">
        var lowestLevel = ${lowestLevel};

        function navigateToGroup(groupId, groupLevel) {
            var getChildGroupsUrl = "${createLink(controller: 'group', action: 'ajaxGetChildGroups')}";

            $.ajax({
                url: getChildGroupsUrl,
                data: { parentId: groupId },
                success: function(resp) {
                    $('#search-results').html(resp);
                }
            });

            if (groupLevel === lowestLevel) {
                var getAvailableStoresUrl = "${createLink(controller: 'group', action: 'ajaxGetAvailableStores')}";

                $.ajax({
                    url: getAvailableStoresUrl,
                    data: { groupId: groupId },
                    success: function(resp) {
                        $('#available-stores').html(resp);
                    }
                });
            } else {
                $('#available-stores').html("");
            }
        }

        function addStoreToGroup(storeId, groupId) {
            var addStoreToGroupUrl = "${createLink(controller: 'group', action: 'ajaxAddStoreToGroup')}";

            $.ajax({
                url: addStoreToGroupUrl,
                data: { storeId: storeId, groupId: groupId },
                success: function(resp) {
                    navigateToGroup(groupId, lowestLevel);
                }
            });
        }

        function removeStoreFromGroup(storeId, groupId) {
            var removeStoreFromGroupUrl = "${createLink(controller: 'group', action: 'ajaxRemoveStoreFromGroup')}";

            $.ajax({
                url: removeStoreFromGroupUrl,
                data: { storeId: storeId, groupId: groupId },
                success: function(resp) {
                    navigateToGroup(groupId, lowestLevel);
                }
            });
        }
    </script>
</head>
<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li class="breadcrumb-item active" aria-current="page">User Groups</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="user-groups" class="container-fluid">
        <div class="header-wl mt-3">
            <h2 class="mx-auto">User Groups</h2>
        </div>

        <g:if test="${flash.message}">
            <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
        </g:if>

        <g:if test="${flash.error}">
            <div class="alert alert-danger alert-wl" role="alert">${flash.error}</div>
        </g:if>

        <div id="search-results">
            <g:render template="groupResults" model="[groups: groups]" />
        </div>

        <div id="available-stores"></div>
    </section>
</body>
</html>