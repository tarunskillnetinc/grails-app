<g:each in="${categories}" var="category">
    <label class="radio-container level-${level}">${category.description}
        <g:radio name="category.id" id="category-${category.id}" checked="${category.id == selectedCategoryId}" value="${category.id}" class="form-check-input" />

        <span class="checkmark"></span>
    </label>

    <g:if test="${category.childCategories}">
        <g:render template="categorySelect" model="[categories: category.childCategories, selectedCategoryId: selectedCategoryId, level: (level + 1)]" />
    </g:if>
</g:each>