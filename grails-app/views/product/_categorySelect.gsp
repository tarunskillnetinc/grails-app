<div class="row form-group mb-3">
    <label for="category" class="col-3 col-form-label text-right pr-4">Category</label>
    <g:select name="category" from="${categories}" optionKey="id" optionValue="description" value="${selectedCategoryId}" class="col-7 form-control select-border" />
</div>