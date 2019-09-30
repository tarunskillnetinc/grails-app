<%@ page import="java.util.stream.Collectors" %>

<g:if test="${products == null}">
    <div class="row">
        <h2 class="text-center my-5 mx-auto">Please enter a search term.</h2>
    </div>
</g:if>

<g:if test="${products?.size() == 0}">
    <div class="row">
        <h2 class="text-center my-5 mx-auto">No results found.</h2>
    </div>
</g:if>

<g:hiddenField name="page-number" value="${page}"/>

<g:each in="${products}" var="product">
    <div class="row mx-3 mb-1 text-center">
        <div class="col-2 my-auto"><g:link controller="product" action="maintenance" params="[productId: product.id]">${product.itemCode}</g:link></div>
        <div class="col-6 my-auto"><g:link controller="product" action="maintenance" params="[productId: product.id]">${product.description}</g:link></div>
        <div class="col-2 my-auto">${product.unitSize}</div>
        <div class="col-2 my-auto">${product.category?.description}</div>
    </div>
</g:each>

<div class="row my-3 mx-3">
    <g:if test="${pageCount != 0}">
        <div id="maintenance-pagination" class="col">
            <div class="mr-3 row justify-content-center">
                <button class="btn btn-wl mx-1" ${page == 1 ? "disabled" : ""} onclick="changePage(1)">|<</button>
                <button class="btn btn-wl mx-1" ${page == 1 ? "disabled" : ""} onclick="changePage(${page - 1})"><</button>
                <g:each in="${pageNumbers}" var="pageLink">
                    <g:if test="${pageLink != page}">
                        <a href="#" class="my-auto mx-1" onclick="changePage(${pageLink})">${pageLink}</a>
                    </g:if>
                    <g:else>
                        <div class="my-auto mx-1">${pageLink}</div>
                    </g:else>
                </g:each>
                <button class="btn btn-wl mx-1" ${page == pageCount ? "disabled" : ""} onclick="changePage(${page + 1})">></button>
                <button class="btn btn-wl mx-1" ${page == pageCount ? "disabled" : ""} onclick="changePage(${pageCount})">>|</button>
            </div>
        </div>
    </g:if>
</div>