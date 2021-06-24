<g:each in="${categories}" var="category">
    <label class="radio-container level-${level}">${category.description}
        <g:radio name="category.id" id="category-${category.id}" checked="${category.id == selectedCategoryId}" value="${category.id}" class="form-check-input" />

        <g:if test="${category.childCategories}">
            <span id="plusMinus-${category.id}" class="plus-minus" aria-expanded="false" onclick="expandCollapseCategory(${category.id}, ${level + 1}, ${selectedCategoryId})">+</span>
        </g:if>

        <span class="checkmark"></span>
    </label>

    <div id="categoryContainer-${category.id}"></div>
</g:each>