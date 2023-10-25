<div class="form-group row margin-top-2rem">
    <div class="col-12 col-sm-8 offset-sm-4">
        <g:link elementId="cancel-btn" controller="buttonGrid" action="show" id="${button?.buttonGrid?.id}" tabindex="-1" role="button" class="btn btn-danger">Cancel</g:link>

        <g:if test="${storeId}">
            <g:if test="${button?.storeId && button?.overrideId}">
                <g:link elementId="remove-btn" action="deleteOverride" id="${button?.id}" tabindex="-1" role="button" class="btn btn-secondary" onClick="return confirm('You are about to delete this override.');">Remove Override</g:link>
                <button id="save-btn" class="btn btn-success" name="save" onclick="attemptSave()">Save</button>
            </g:if>
            <g:else>
                <button id="save-btn" class="btn btn-success" name="save" onclick="saveOverride(${button?.id}, ${storeId})">Override</button>
            </g:else>
        </g:if>
        <g:else>
            <g:if test="${button?.id > 0}">
                <g:link elementId="unassign-btn" action="unassign" id="${button?.id}" tabindex="-1" role="button" class="btn btn-secondary" onClick="return confirm('You are about to unassign this button.');">Unassign</g:link>
            </g:if>
            <g:else>
                <button id="unassign-btn" class="btn btn-secondary" disabled>Unassign</button>
            </g:else>

            <button id="save-btn" class="btn btn-success" name="save" onclick="attemptSave()">Save</button>
        </g:else>
        </div>
</div>

<script type="text/javascript">
    function attemptSave() {
        const amount = $("input[id*=amountInput]");
        if (amount.val() !== "" && amount.val() < 0.01 && !$("input[id*=exactInput]").is(":checked")) {
            alert("Amount cannot be 0")
        } else {
            document.querySelector('#submission-form').submit()
        }
    }
</script>