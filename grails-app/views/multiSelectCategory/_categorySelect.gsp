<div class="category-select">
    <div class="form-group search" style="position: sticky; top: 0; z-index: 1; background: white;">
        <g:textField name="category-filter-search" class="form-control bottom-border"
                     onkeyup="searchCategories(event, ${level}, ${triggerOnCategoryChange ?: false}, this.value, ${selectedCategoryIds}, ${specialId});" placeholder="Filter categories" />
    </div>

    <div id="category-container-results">
        <g:render template="/multiSelectCategory/categorySelectInputs" model="[categories: categories, level: level, productCategoryList: productCategoryList, selectedCategoryIds: selectedCategoryIds, triggerOnCategoryChange: triggerOnCategoryChange]" />
    </div>
</div>