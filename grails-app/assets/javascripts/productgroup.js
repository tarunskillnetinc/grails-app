function productSelected (id, sku, description) {
    // Add product variant to list.
    $.ajax({
        url: addProductUrl,
        data: { productVariantId: id, sku: sku, productDescription: description },
        success: function(resp) {
            $("#productList").append(resp);

            $('#noResultsRow').hide();

            let i = $('#productList').children().length - 1; // remove hidden noResultsRow
            let row = $('#productVariant' +id);
            row.addClass("wl-striped" +((i-1) % 2));
            row.find('#productGroup-product-0-id').attr("id", "productGroup-product-" + i + "-id");
            row.find('#productGroup-product-0-sku').attr("id", "productGroup-product-" + i + "-sku");
            row.find('#productGroup-product-0-description').attr("id", "productGroup-product-" + i + "-description");
            row.find('#productGroup-product-0-remove-btn').attr("id", "productGroup-product-" + i + "-remove-btn");
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