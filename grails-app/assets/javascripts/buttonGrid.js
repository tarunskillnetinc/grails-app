function productSelected (id, itemCode, description) {
    console.log(id);
    console.log(itemCode);
    console.log(description);

    $("#buttonProductId").val(id);
    $("#itemCode").val(itemCode);
    $("#productDescription").val(description);
}