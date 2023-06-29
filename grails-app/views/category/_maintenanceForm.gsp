<g:form method="post" action="save" class="mt-5" name="category-form">
    <g:hiddenField name="id" value="${category?.id}"/>
    <h2 class="row col-1">Details</h2>
    <div class="row">
        <div class="form-group row col-12 col-sm-6">
            <label for="description" class="col-3 col-form-label text-right pr-4">Description</label>
            <g:field type="text" maxlength="60" name="description" class="col-5 form-control bottom-border" required="true" value="${category?.description}"/>
        </div>
        <div class="form-group row col-12 col-sm-6">
            <label for="shortDescription" class="col-3 col-form-label text-right pr-4">Short Description</label>
            <g:field type="text" maxlength="40" name="shortDescription" class="col-5 form-control bottom-border" required="true" value="${category?.shortDescription}"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-12 col-sm-6">
            <label for="retailerCategoryCode" class="col-3 col-form-label text-right pr-4">Retailer Category Code</label>
            <g:field type="text" maxlength="30" name="retailerCategoryCode" class="col-5 form-control bottom-border" required="true" value="${category?.retailerCategoryCode}"/>
        </div>
    </div>

    <div id="categoryRestrictions">
        <g:render template="restrictions" model="[category: category]"></g:render>
    </div>

    <h2 class="row col-4">Parent Category</h2>
    <div class="row form-group">
        <div class="col-6 col-lg-9 pt-2">
            <g:render template="/product/categorySelect" model="[categories: topLevelCategories, productCategoryList: null, selectedCategoryId: null, level: 1, triggerOnCategoryChange: true]" />
        </div>
    </div>
</g:form>