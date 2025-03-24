<div class="form-group row mb-3 align-items-center">
    <div class="col-md-3">
        <input type="hidden" name="storeAdditionalDetails[${index}].description" value="${detail?.description?.encodeAsHTML()}" />
        <label class="form-control-label mb-0" id="storeAdditionalDetails[${i}].description">${detail?.description?.encodeAsHTML()}:</label>
    </div>
    <div class="col-md-6 pl-0">
        <g:textField name="storeAdditionalDetails[${index}].value" maxlength="20" value="${detail?.value?.encodeAsHTML()}" class="form-control bottom-border" readonly="true"/>
    </div>
    <div class="col-md-3">
        <a href="#" class="btn btn-sm btn-primary mr-2" style="min-width: 80px; font-size: 0.9rem;" onclick="addStoreAdditionalDetail('${index}', '${detail?.description}', '${detail?.value}')">Edit</a>
        <a href="#" class="btn btn-sm btn-danger" style="min-width: 80px; font-size: 0.9rem;" onclick="deleteAdditionalDetail(${index})">Delete</a>
    </div>
</div>