<div class="form-group row">
    <input type="hidden" name="storeAdditionalDetails[${index}].description" value="${detail?.description?.encodeAsHTML()}" />
    <label for="storeAdditionalDetails[${index}].description" class="col-5 col-lg-3 offset-lg-2 col-form-label text-right pr-4">${detail?.description?.encodeAsHTML()}</label>
    <div class="col-7 col-lg-4">
        <g:textField name="storeAdditionalDetails[${index}].value" maxlength="240" value="${detail?.value?.encodeAsHTML()}" class="form-control bottom-border" readonly="true"/>
    </div>
    <div class="col-3 mt-2">
        <a href="#" class="btn btn-sm btn-primary mr-2" style="min-width: 80px; font-size: 0.9rem;" onclick="addStoreAdditionalDetail('${index}', '${detail?.description}', '${detail?.value}')">Edit</a>
        <a href="#" class="btn btn-sm btn-danger" style="min-width: 80px; font-size: 0.9rem;" onclick="deleteAdditionalDetail(${index})">Delete</a>
    </div>
</div>
