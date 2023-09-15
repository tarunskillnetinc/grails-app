<g:form method="post" action="save" class="mt-5" name="category-form">
    <g:hiddenField name="id" value="${category?.id}"/>

    <g:if test="${hasProducts}">
        <div class="alert alert-danger text-center alert-wl mx-0" role="alert">All products must be removed from this category before it can be deleted</div>
    </g:if>

    <g:if test="${hasChildren}">
        <div class="alert alert-danger text-center alert-wl mx-0" role="alert">All child categories must be removed from this parent category before it can be deleted.</div>
    </g:if>

    <h2 class="row col-1 mt-4">Details</h2>
    <div class="row">
        <div class="form-group row col-12 col-sm-6 offset-sm-1">
            <label for="description" class="col-3 col-form-label text-right pr-4">Description</label>
            <g:field type="text" maxlength="60" name="description" class="col-6 form-control bottom-border" required="true" value="${category?.description}"/>
        </div>
    </div>
    <div class="row">
        <div class="form-group row col-12 col-sm-6 offset-sm-1">
            <label for="shortDescription" class="col-3 col-form-label text-right pr-4">Short Description</label>
            <g:field type="text" maxlength="40" name="shortDescription" class="col-6 form-control bottom-border" required="true" value="${category?.shortDescription}"/>
        </div>
    </div>

    <div class="row">
        <div class="form-group row col-12 col-sm-6 offset-sm-1">
            <label for="retailerCategoryCode" class="col-3 col-form-label text-right pr-4">Retailer Category Code</label>
            <g:field type="text" maxlength="30" name="retailerCategoryCode" class="col-4 form-control bottom-border" required="true" value="${category?.retailerCategoryCode}"/>
        </div>
    </div>

    <div class="row mt-2">
        <div class="form-group row col-12 col-sm-6 offset-sm-1">
            <label for="categorySelect" class="col-3 col-form-label text-right pr-4">Parent Category</label>
            <div class="col-12 col-sm-8 px-0">
                <g:render template="/product/categorySelect" model="[categories: topLevelCategories, productCategoryList: categoryList, selectedCategoryId: category?.parentCategory?.id, level: 1, triggerOnCategoryChange: true]" />
            </div>
        </div>
    </div>

    <div id="categoryRestrictions">
        <g:render template="restrictions" model="[category: category]"></g:render>
    </div>
</g:form>