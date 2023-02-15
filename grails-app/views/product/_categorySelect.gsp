<g:each in="${categories}" var="category" status="i">
    <label id="category-${i+1}" class="radio-container level-${level}">${category.description}
        <g:radio name="category.id" id="category-${category.id}" checked="${category.id == selectedCategoryId}" value="${category.id}" class="form-check-input" onclick="onCategoryChanged( ${category.id})"/>
        <g:if test="${category.childCategories}">
            <!-- Is this department/category part of the selected category hierarchy? -->
            <g:if test="${productCategoryList?.contains(category.id)}">
                <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="true" onclick="expandCollapseCategory(${category.id}, ${level + 1},${selectedCategoryId})">-</span>
            </g:if>
            <g:else>
                <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="false" onclick="expandCollapseCategory(${category.id}, ${level + 1}, ${selectedCategoryId})">+</span>
            </g:else>
        </g:if>

        <span id="category-${i+1}-checkmark" class="checkmark"></span>
    </label>

    <div id="categoryContainer-${category.id}">
        <g:if test="${category.childCategories && productCategoryList?.contains(category.id)}">
            <g:render template="categorySelect" model="[categories: category.childCategories, productCategoryList: productCategoryList, selectedCategoryId: selectedCategoryId, level: level + 1]" />
        </g:if>
    </div>
</g:each>