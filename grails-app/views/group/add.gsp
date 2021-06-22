<!doctype html>
<html>
<head>
    <meta name="layout" content="main" />

    <title>WonderLane User Groups</title>

    <script type="text/javascript">
        function levelChanged() {
            var getParentGroupsUrl = "${createLink(controller: 'group', action: 'ajaxGetParentGroups')}";

            var level = $('#level\\.id :selected').attr("data-level");

            $.ajax({
                url: getParentGroupsUrl,
                data: { level: level },
                success: function(resp) {
                    $('#parent-select').html(resp);
                }
            });
        }
    </script>
</head>
<body>
    <div class="d-flex justify-content-center header-wl">
        <h2>Groups</h2>
    </div>

    <g:if test="${flash.message}">
        <div class="alert alert-success alert-wl" role="alert">${flash.message}</div>
    </g:if>

    <g:if test="${flash.error}">
        <div class="alert alert-danger alert-wl" role="alert">${flash.error}</div>
    </g:if>

    <g:hasErrors bean="${group}">
        <div class="alert alert-danger alert-wl" role="alert">
            <g:renderErrors bean="${group}" as="list" />
        </div>
    </g:hasErrors>

    <g:form name="save-button" action="save" novalidate="novalidate" class="mt-4">
        <div class="form-group row col-12 col-lg-6">
            <label for="name" class="col-4 col-form-label text-right pr-4">Name</label>
            <g:textField name="name" class="col-5 form-control bottom-border" value="${group?.name}" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="level.id" class="col-4 col-form-label text-right pr-4">Level</label>
            <g:select name="level.id" class="col-3 form-control select-border" noSelection="[null: '-- Please select --']" from="${groupLevels}" optionKey="id" optionValue="name" value="${group?.level?.id}" dataAttrs="[level: 'level']" onChange="levelChanged();" />
        </div>

        <div class="form-group row col-12 col-lg-6">
            <label for="parent.id" class="col-4 col-form-label text-right pr-4">Parent</label>
            <div id="parent-select">
                <g:select name="parent.id" class="col-12 form-control select-border" optionKey="id" optionValue="name" noSelection="[null: '-- Select type --']" from="${groupValues}" value="${group?.parentGroup?.id}" />
            </div>
        </div>

        <div class="form-group row col-12 col-lg-6">
            <div class="offset-lg-4">
                <g:link controller="group" action="index" role="button" class="btn btn-danger">Cancel</g:link>

                <g:submitButton class="btn btn-success" name="save" value="Save" />
            </div>
        </div>
    </g:form>
</body>
</html>