<div class="form-group row">
    <div class="col-8 offset-2">
        <g:link controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

        <g:if test="${button?.id > 0}">
            <g:link action="unassign" id="${button?.id}" tabindex="-1" role="button" class="btn btn-secondary" onClick="return confirm('You are about to unassign this button.');">Unassign</g:link>
        </g:if>
        <g:else>
            <button class="btn btn-secondary" disabled>Unassign</button>
        </g:else>

        <g:submitButton class="btn btn-success" name="save" value="Save" />
    </div>
</div>