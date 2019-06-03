function productSelected (id, itemCode, description) {
    $("#buttonProductId").val(id);
    $("#itemCode").text(itemCode);
    $("#productDescription").text(description);
}