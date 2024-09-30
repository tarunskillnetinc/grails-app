<g:if test="${error && errorMessages}">
    <section id="errors-container" class="container-fluid">
        <div class="alert alert-danger alert-wl mx-0" role="alert">
            <ul>
                <g:each in="${errorMessages}" var="entry">
                    <li>${entry.value}</li>
                </g:each>
            </ul>
        </div>
    </section>
</g:if>