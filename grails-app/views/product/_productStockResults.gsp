<g:if test="${!productStocks || productStocks?.size() == 0}">
    <div class="row col-8 offset-2 text-center px-0">
        <div id="noResultsRow" class="col pt-2 pb-2 text-center my-auto wl-striped0">
            Please select a <g:terminology term="storeTerm" case="lower" />.
        </div>
    </div>
</g:if>

<g:each in="${productStocks}" var="productStock" status="i">
    <div id="productStock-${i+1}" class="row col-8 offset-2 pt-2 pb-2 wl-striped${i % 2}">
        <div id="productStock-${i+1}-sku" class="col-3 my-auto">${productStock.sku}</div>
        <div id="productStock-${i+1}-quantityInStock" class="col-3 my-auto">${productStock.quantityInStock}</div>
        <div id="productStock-${i+1}-quantityOnOrder" class="col-3 my-auto">${productStock.quantityOnOrder}</div>
        <div id="productStock-${i+1}-quantityDelivered" class="col-3 my-auto">${productStock.quantityDelivered}</div>
    </div>
</g:each>