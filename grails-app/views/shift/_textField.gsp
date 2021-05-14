<label for="${name}" class="col-${labelCols ?: '3'} col-form-label text-right">${label}</label>

<div class="input-group col-${fieldCols ?: '3'}">
    <div class="input-group-prepend">
        <span class="input-group-text">&pound;</span>
    </div>
    <g:textField name="${name}" value="${value}" class="form-control mask-money" />
</div>