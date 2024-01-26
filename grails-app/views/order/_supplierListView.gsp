<g:each in="${suppliers}" var="supplier" status="i">
    <div class="row ml-0 mr-0 pt-2 pb-2 wl-striped${i%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="selectSupplier(${supplier?.id})">
        <div id="supplier-name-${i + 1}" class="col-6">${supplier?.name}</div>
    </div>
</g:each>