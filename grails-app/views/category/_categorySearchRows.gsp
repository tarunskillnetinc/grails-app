<g:each in="${topLevelCategories}" var="category" status="i">
    <g:if test="${matchedCategories == null || matchedCategories?.contains(category.id)}">
        <div id="category-result-${i+1}" class="row ml-0 mr-0 px-0 pt-2 pb-2 wl-striped${row++%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'show', id: category.id)}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="category-result-${i+1}-description" class="col-4">
                    <g:if test="${level > 0}">
                        <g:each in="${0..level}">
                            &nbsp;&nbsp;
                        </g:each>
                        &boxur;&nbsp;&nbsp;
                    </g:if>
                    ${category.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "categoryCode" }?.enabled}">
                <div id="category-result-${i+1}-category-code" class="col-2 text-truncate">${category?.retailerCategoryCode ?: ""}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "customerIdRequired" }?.enabled}">
                <div id="category-result-${i+1}-customer-id-required" class="col-2">${category?.restrictions?.buyerIdRequired ? "Yes" : "No"}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "customerAgeRestriction" }?.enabled}">
                <div id="category-result-${i+1}-customer-age-restriction" class="col-2">${category?.restrictions?.buyerAgeRestriction ?: ""}</div>
            </g:if>

            <button class="btn btn-wl p-1" style="min-width: 80px; font-size: 0.9rem; margin-left: auto; margin-right: 10px;" disabled>Products</button>
        </div>
    </g:if>
    <g:render template="categorySearchRows" model="[topLevelCategories: category.childCategories, matchedCategories: matchedCategories, level: level+1, userColumns: userColumns]"/>
</g:each>
