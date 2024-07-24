function productSelected (id, itemCode, description) {
    // Add product to list.
    $.ajax({
        url: addProductUrl,
        data: { productVariantId: id },
        success: function(resp) {
            let productList = $("#productList")
            let warningMessage = $('#warning-message')

            for (const element of productList.children()) {
                if (element.id.toUpperCase() === "PRODUCTVARIANT" + id) {
                    if (warningMessage.length) {
                        warningMessage.text("Product has already been added.")
                        warningMessage.removeClass("hidden")
                    }
                    return
                }
            }

            if (warningMessage.length) {
                warningMessage.addClass("hidden")
            }

            productList.append(resp);

            $('#noResultsRow').hide();

            let i = productList.children().length - 1; // remove hidden noResultsRow
            let row = $('#productVariant' +id);
            row.addClass("wl-striped" +((i-1) % 2));
            row.find('#prod-0-id').attr("id", "prod-" + i + "-id");
            row.find('#prod-0-sku').attr("id", "prod-" + i + "-sku");
            row.find('#prod-0-description').attr("id", "prod-" + i + "-description");
            row.find('#prod-0-colour').attr("id", "prod-" + i + "-colour");
            row.find('#prod-0-size').attr("id", "prod-" + i + "-size");
            row.find('#prod-0-remove-btn').attr("id", "prod-" + i + "-remove-btn");
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