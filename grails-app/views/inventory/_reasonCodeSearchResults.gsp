<g:if test="${reasonCodes}">
    <g:each in="${reasonCodes}" var="reasonCode" status="i">
        <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable pointer" title="Select Reason Code." onclick="selectReasonCode(${reasonCode.id})">
            <div id="reasonCode-name-${i + 1}" class="col-12 text-truncate-wrap">${reasonCode.description}</div>
        </div>
    </g:each>
</g:if>
<g:else>
    <div class="text-center text-muted">
        <p>No reason codes found matching your search</p>
    </div>
</g:else>