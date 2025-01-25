<g:each in="${categories}" var="category" status="i">
    <g:if test="${!isSearch || (isSearch)}">
        <div id="category-${i+1}" class="checkbox-container level-${level}">
            <g:if test="${category.childCategories}">
                <g:if test="${productCategoryList?.contains(category.id)}">
                    <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="true" data-level="${level + 1}"
                          onclick="expandCollapseCategory(${category.id}, ${level + 1}, ${selectedCategoryIds ?: '[]'}, ${triggerOnCategoryChange ?: false})">-</span>
                </g:if>
                <g:else>
                    <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="false" data-level="${level + 1}"
                          onclick="expandCollapseCategory(${category.id}, ${level + 1}, ${selectedCategoryIds ?: '[]'}, ${triggerOnCategoryChange ?: false})">+</span>
                </g:else>
            </g:if>

            <label class="checkbox-input">${category.description}
            <g:checkBox name="category.id[]" id="category-${category.id}"
                        checked="${selectedCategoryIds?.contains(category.id)}"
                        value="${category.id}"
                        class="col-1 form-check-input wl-checkbox"
                        data-parent-id="${category?.parentCategory?.id}"
                        data-level="${level}"
                        onclick="${triggerOnCategoryChange ? 'onCategoryChanged('+category.id+');' : 'return;'}" />
                <span id="category-${i+1}-checkmark" class="checkmark"></span>
            </label>
        </div>

        <div id="categoryContainer-${category.id}">
            <g:if test="${category.childCategories && productCategoryList?.contains(category.id)}">
                <g:render template="/multiSelectCategory/categorySelectInputs"
                          model="[categories: category.childCategories, productCategoryList: productCategoryList, selectedCategoryIds: selectedCategoryIds, level: level + 1, triggerOnCategoryChange: triggerOnCategoryChange]" />
            </g:if>
        </div>
    </g:if>
</g:each>

<g:if test="${categories == null || categories?.size() == 0}">
    <div class="text-center">No categories found.</div>
</g:if>


%{--<g:each in="${categories}" var="category" status="i">--}%
%{--    <g:if test="${!isSearch || (isSearch)}">--}%
%{--        <div id="category-${i+1}" class="radio-container level-${level}">--}%
%{--            <g:if test="${category.childCategories}">--}%
%{--                <!-- Is this department/category part of the selected category hierarchy? -->--}%
%{--                <g:if test="${productCategoryList?.contains(category.id)}">--}%
%{--                    <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="true" data-level="${level + 1}"--}%
%{--                          onclick="expandCollapseCategory(${category.id}, ${level + 1}, ${selectedCategoryIds ?: '[]'}, ${triggerOnCategoryChange ?: false})">-</span>--}%
%{--                </g:if>--}%
%{--                <g:else>--}%
%{--                    <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="false" data-level="${level + 1}"--}%
%{--                          onclick="expandCollapseCategory(${category.id}, ${level + 1}, ${selectedCategoryIds ?: '[]'}, ${triggerOnCategoryChange ?: false})">+</span>--}%
%{--                </g:else>--}%
%{--            </g:if>--}%

%{--            <label class="radio-input">${category.description}--}%
%{--            <g:checkBox name="category.id[]" id="category-${category.id}"--}%
%{--                        checked="${selectedCategoryIds?.contains(category.id)}"--}%
%{--                        value="${category.id}"--}%
%{--                        class="col-1 form-check-input wl-checkbox"--}%
%{--                        data-parent-id="${category?.parentCategory?.id}"--}%
%{--                        data-level="${level}"--}%
%{--                        onclick="${triggerOnCategoryChange ? 'onCategoryChanged('+category.id+');' : 'return;'}" />--}%
%{--                <span id="category-${i+1}-checkmark" class="checkmark"></span>--}%
%{--            </label>--}%
%{--        </div>--}%

%{--        <div id="categoryContainer-${category.id}">--}%
%{--            <g:if test="${category.childCategories && productCategoryList?.contains(category.id)}">--}%
%{--                <g:render template="/multiSelectCategory/categorySelectInputs"--}%
%{--                          model="[categories: category.childCategories, productCategoryList: productCategoryList, selectedCategoryIds: selectedCategoryIds, level: level + 1, triggerOnCategoryChange: triggerOnCategoryChange]" />--}%
%{--            </g:if>--}%
%{--        </div>--}%
%{--    </g:if>--}%
%{--</g:each>--}%

%{--<g:if test="${categories == null || categories?.size() == 0}">--}%
%{--    <div class="text-center">No categories found.</div>--}%
%{--</g:if>--}%