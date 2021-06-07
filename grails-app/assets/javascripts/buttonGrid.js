function productSelected (id, itemCode, description) {
    $("#buttonProductVariantId").val(id);
    $("#itemCode").text(itemCode);
    $("#productDescription").text(description);
}