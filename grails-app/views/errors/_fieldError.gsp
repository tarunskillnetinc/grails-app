<g:if test="${error && errorMessages.containsKey(errorKey)}">
    <div class="text-danger text-center">
        <span>${errorMessages[errorKey]}</span>
    </div>
</g:if>
