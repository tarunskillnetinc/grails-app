<g:if test="${error && errorMessages.containsKey(errorKey)}">
    <div class="alert alert-danger text-center mt-1 mb-1">
        <span>${errorMessages[errorKey]}</span>
    </div>
</g:if>
