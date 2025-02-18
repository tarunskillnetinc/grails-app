<g:form method="post" action="save" class="mt-5" name="category-form">
    <g:hiddenField name="id" value="${category?.id}"/>

    <g:if test="${hasProducts}">
        <div class="alert alert-danger text-center alert-wl mx-0" role="alert">All products must be removed from this category before it can be deleted</div>
    </g:if>

    <g:if test="${hasChildren}">
        <div class="alert alert-danger text-center alert-wl mx-0" role="alert">All child categories must be removed from this parent category before it can be deleted.</div>
    </g:if>

    <div id="accordion">
        <!-- Category details. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="categoryDetails" data-toggle="collapse" data-target="#collapseCategoryDetails" aria-expanded="true" aria-controls="collapseCategoryDetails">
                <div class="row">
                    <div class="col-10"><strong>Category Details</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>
            
            <div id="collapseCategoryDetails" class="collapse show" aria-labelledby="categoryDetails" data-parent="#accordion">
                <div class="card-body py-5">
                    <div class="row">
                        <div class="form-group row col-12 col-sm-6 offset-sm-1">
                            <label for="description" class="col-4 col-form-label text-right pr-4">Description:</label>
                            <g:field type="text" maxlength="60" name="description" class="col-6 form-control bottom-border" required="true" value="${category?.description}"/>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="form-group row col-12 col-sm-6 offset-sm-1">
                            <label for="shortDescription" class="col-4 col-form-label text-right pr-4">Short Description:</label>
                            <g:field type="text" maxlength="40" name="shortDescription" class="col-6 form-control bottom-border" required="true" value="${category?.shortDescription}"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group row col-12 col-sm-6 offset-sm-1">
                            <label for="retailerCategoryCode" class="col-4 col-form-label text-right pr-4">Category Code:</label>
                            <g:field type="text" maxlength="30" name="retailerCategoryCode" class="col-4 form-control bottom-border" required="true" value="${category?.retailerCategoryCode}"/>
                        </div>
                    </div>

                    <div class="row mt-2">
                        <div class="form-group row col-12 col-sm-6 offset-sm-1">
                            <label for="categorySelect" class="col-4 col-form-label text-right pr-4">Parent Category:</label>
                            <div class="col-12 col-sm-8 px-0">
                                <g:render template="/product/categorySelect" model="[categories: topLevelCategories, productCategoryList: categoryList, selectedCategoryId: category?.parentCategory?.id, level: 1, triggerOnCategoryChange: true]" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Category Restrictions. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="categoryRestrictions" data-toggle="collapse" data-target="#collapseCategoryRestrictions" aria-expanded="true" aria-controls="collapseCategoryRestrictions">
                <div class="row">
                    <div class="col-10"><strong>Category Restrictions</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseCategoryRestrictions" class="collapse collapsed" aria-labelledby="categoryRestrictions" data-parent="#accordion">
                <div class="card-body py-5">
                    <div id="productHistoryContainer" style="max-height: 300px; overflow-x: auto; overflow-y: auto;">
                        <div id="restrictions">
                            <g:render template="restrictions" model="[category: category]"></g:render>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Category History. -->
        <div class="card bg-light border-wl accordion-card">
            <div class="card-header pointer" id="categoryHistory" data-toggle="collapse" data-target="#collapseCategoryHistory" aria-expanded="true" aria-controls="collapseCategoryHistory">
                <div class="row">
                    <div class="col-10"><strong>Category History</strong></div>
                    <div class="col-2 text-right">
                        <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                            <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                        </svg>
                    </div>
                </div>
            </div>

            <div id="collapseCategoryHistory" class="collapse collapsed" aria-labelledby="categoryHistory" data-parent="#accordion">
                <div class="card-body py-5">
                    <div id="categoryHistoryContainer" style="max-height: 300px; overflow-x: auto; overflow-y: auto;"></div>
                </div>
            </div>
        </div>
    </div>
</g:form>