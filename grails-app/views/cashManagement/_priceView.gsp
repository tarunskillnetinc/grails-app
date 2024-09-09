<div class="col input-group">
    <div class="input-group-prepend">
        <span class="input-group-text">&pound;</span>
    </div>
        <%
            fieldValue = fieldValue ? fieldValue : 0
            def fieldValeModified =  String.format("%.2f", fieldValue/Math.pow(10, 2))
            def enabledOrDisabled = (!onlyRetailerLevel || isStoreLevelLogin) && !storeLevelExist
        %>
    <g:textField id="${inputId}" name="${inputName}" value="${fieldValeModified}" class="form-control mask-money" disabled="${enabledOrDisabled}"/>
</div>