<div class="form-group row margin-top-2rem">
    <div class="col-12 col-sm-8 offset-sm-4">
        <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

        <g:if test="${button?.id > 0}">
            <g:link action="unassign" id="${button?.id}" tabindex="-1" role="button" class="btn btn-secondary" onClick="return confirm('You are about to unassign this button.');">Unassign</g:link>
        </g:if>
        <g:else>
            <button class="btn btn-secondary" disabled>Unassign</button>
        </g:else>

        <button class="btn btn-success" name="save" onclick="document.querySelector('#submission-form').submit()">Save</button>
    </div>
</div>