function productSelected (id, sku, description) {
    // Add product variant to list.
    $.ajax({
        url: addProductUrl,
        data: { productVariantId: id, sku: sku, productDescription: description },
        success: function(resp) {
            $("#productList").append(resp);

            $('#noResultsRow').hide();

            $('#productVariant' +id).addClass("wl-striped" +($('#productList').children().length % 2));
        }
    });
}

function removeProduct(productVariantId) {
    $('#productVariant' +productVariantId).remove();

    var productList = $('#productList');

    if (productList.children().length === 1) {
        var noResultsRow = $('#noResultsRow');

        noResultsRow.removeClass("wl-striped0");
        noResultsRow.removeClass("wl-striped1");
        noResultsRow.addClass("wl-striped0");
        noResultsRow.show();
    } else {
        for (var i = 1 ; i <= productList.children().length ; i++) {
            var child = $('#productList>div:nth-child(' +i +')');

            child.removeClass("wl-striped0");
            child.removeClass("wl-striped1");

            child.addClass("wl-striped" +(i % 2));
        }
    }
}