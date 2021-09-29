function productSelected (id, sku, description) {
    $("#buttonSku").val(sku);
    $("#sku").text(sku);
    $("#productDescription").text(description);
}