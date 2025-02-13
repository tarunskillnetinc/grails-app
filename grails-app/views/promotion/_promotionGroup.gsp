<div id="${promoGroupName}" class="promotion-product-container container-fluid col-12 col-lg-4 my-4 mx-4 py-4 px-3">
    <g:hiddenField name="${promotionGroupType}Groups[${promoGroupId}].id" value="${promotionGroup.id}" />
    <g:hiddenField name="${promotionGroupType}Groups[${promoGroupId}].type" value="${promotionGroup.type}" />
    <g:hiddenField name="${promotionGroupType}Groups[${promoGroupId}].sku" value="${promotionGroup.sku}" />
    <g:hiddenField name="${promotionGroupType}Groups[${promoGroupId}].categoryId" value="${promotionGroup.categoryId}" />
    <g:hiddenField name="${promotionGroupType}Groups[${promoGroupId}].productGroupId"
                   value="${promotionGroup.productGroupId}"/>

    <g:hasErrors bean="${promotionGroup}">
        <section id="errors-container" class="container-fluid">
            <div class="alert alert-danger alert-wl mx-0" role="alert">
                <g:renderErrors bean="${promotionGroup}" as="list" />
            </div>
        </section>
    </g:hasErrors>

    <div class="row">
        <div class="col-11"><strong>${promotionGroupDescription}</strong></div>
        <g:if test="${canDeleteProduct == null || (canDeleteProduct != null && canDeleteProduct == true)}">
            <div class="col-1 text-right pl-0"><a id="${promoGroupName}-DeleteButton" href="#" onclick="return deletePromotionGroup('${promoGroupName}', '${promotionGroupType}');" class="text-dark" ><sup>X</sup></a></div>
        </g:if>
    </div>

    <div class="row">
        <div class="col-12 form-group form-inline my-3">
            <g:if test="${showQuantityField}">
                Quantity
                <g:field type="number" min="0" max="1000" step="1" name="${promotionGroupType}Groups[${promoGroupId}].requiredQuantity" value="${promotionGroup.requiredQuantity ? promotionGroup.requiredQuantity : (promotionGroup.requiredValue ? '' : 1)}" class="col-12 col-md-3 form-control mx-3" />

                <g:if test="${showValueField}">
                    or Value

                    <g:field type="number" min="0" max="1000" step="0.01" name="${promotionGroupType}Groups[${promoGroupId}].requiredValue" value="${promotionGroup.requiredValue}" class="col-12 col-md-3 form-control mx-3" />
                </g:if>
            </g:if>
            <g:else>
                <g:hiddenField name="${promotionGroupType}Groups[${promoGroupId}].requiredQuantity" value="${promotionGroup.requiredQuantity ?: 1}" />

                Quantity x ${promotionGroup.requiredQuantity ?: 1}
            </g:else>
        </div>
    </div>
</div>