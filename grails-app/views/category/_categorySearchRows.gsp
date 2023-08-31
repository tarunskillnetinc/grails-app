<g:each in="${topLevelCategories}" var="category" status="i">
    <g:if test="${matchedCategories == null || matchedCategories?.contains(category.id)}">
        <div id="category-result-${i+1}" class="row col-10 offset-1 px-0 pt-2 pb-2 wl-striped${row++%2} hoverable" title="Click to edit." style="cursor: pointer;" onclick="document.location.href='${createLink(action:'show', id: category.id)}';">
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "description" }?.enabled}">
                <div id="category-result-${i+1}-description" class="col-6">
                    <g:if test="${level > 0}">
                        <g:each in="${0..level}">
                            &nbsp;&nbsp;
                        </g:each>
                        &boxur;&nbsp;&nbsp;
                    </g:if>
                    ${category.description}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "retailerCategoryCode" }?.enabled}">
                <div id="category-result-${i+1}-retailer-Category-Code" class="col-2 text-truncate">${category.retailerCategoryCode}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "buyerId" }?.enabled}">
                <div id="category-result-${i+1}-buyerId" class="col-2">${category.restrictions.buyerIdRequired}</div>
            </g:if>
            <g:if test="${!userColumns || userColumns?.columns?.find { it.column == "buyerAge" }?.enabled}">
                <div id="category-result-${i+1}-buyer-age" class="col-2">${category.restrictions.buyerAgeRestriction}</div>
            </g:if>
        </div>
    </g:if>
    <g:render template="categorySearchRows" model="[topLevelCategories: category.childCategories, matchedCategories: matchedCategories, level: level+1, userColumns: userColumns]"/>
</g:each>
