<div class="category-select">
    <div class="form-group search">
        <g:textField name="category-filter-search" class="form-control bottom-border" onkeyup="searchCategories(event, ${level}, ${triggerOnCategoryChange ?: false}, this.value);" placeholder="Filter categories" />
    </div>

    <div id="category-container-results">
        <g:render template="/product/categorySelectInputs" model="[categories: categories, level: level, productCategoryList: productCategoryList, selectedCategoryId: selectedCategoryId, triggerOnCategoryChange: triggerOnCategoryChange]" />
    </div>
</div>