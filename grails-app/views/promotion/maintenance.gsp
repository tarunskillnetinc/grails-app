<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />

        <title>WonderLane Promotion Maintenance</title>

        <asset:stylesheet src="bootstrap-datepicker3.min.css" />
        <asset:javascript src="bootstrap-datepicker.min.js" />
        <asset:javascript src="moment-with-locales.min.js"/>
        <script type='text/javascript'>
            $(function() {
                $('.input-group.date.startDate').datepicker({
                    format: "DD dd MM yyyy",
                    weekStart: 1,
                    endDate: $('.promo-endDate').val(),
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });
                $('.input-group.date.endDate').datepicker({
                    format: "DD dd MM yyyy",
                    weekStart: 1,
                    startDate: $('.promo-startDate').val(),
                    todayHighlight: true,
                    autoclose: true,
                    todayBtn: "linked",
                    orientation: "bottom auto"
                });
            });

            $(document).on("keypress", "input", function (e) {
                var code = e.keyCode || e.which;
                if (code === 13) {
                    e.preventDefault();
                    return false;
                }
            });

            $(document).ready(function () {
                $(".wl-noExpire").click(function() {
                    $('.wl-noExpire').prop("checked", this.checked);
                    $('.promo-endDate').prop('disabled', this.checked);
                    $('.input-group.date.startDate').datepicker('setEndDate', (this.checked ? "" : $('.promo-endDate').val()));
                    if (!this.checked) {
                        $('.promo-endDate').val(moment($('.promo-startDate').val(), "dddd DD MMMM YYYY").add(7, 'days').format("dddd DD MMMM YYYY"));
                    }
                });

                $('.promo-desc').on("change", function() {
                    $('.promo-desc').val(this.value);
                    if ( $('.promo-receiptDesc').val() === "") {
                        $('.promo-receiptDesc').val(this.value);
                        $('.promo-receiptDesc').removeClass("is-invalid");
                    }
                    $('.promo-desc').removeClass("is-invalid");
                });

                $('.promo-receiptDesc').on("change", function() {
                    $('.promo-receiptDesc').val(this.value);
                    if ( $('.promo-desc').val() === "") {
                        $('.promo-desc').val(this.value);
                        $('.promo-desc').removeClass("is-invalid");
                    }

                    $('.promo-receiptDesc').removeClass("is-invalid");
                });

                $('.promo-startDate').on("change", function() {
                    $('.promo-startDate').val(this.value);
                    $('.promo-startDate').removeClass("is-invalid");
                    $('.input-group.date.endDate').datepicker('setStartDate', this.value);
                });

                $('.promo-endDate').on("change", function() {
                    $('.promo-endDate').val(this.value);
                    $('.promo-endDate').removeClass("is-invalid");
                    $('.input-group.date.startDate').datepicker('setEndDate', this.value);
                });

                $('.promo-id').on("change", function() {
                    $('.promo-id').val(this.value);
                    $('.promo-id').removeClass("is-invalid");
                });

                $('.promo-active').click(function() {
                    $('.promo-active').prop("checked", this.checked);
                });

                $('.promo-amount').on("change", function() {
                    $('.promo-amount').removeClass("is-invalid");
                });
            });

            function productSelected(id, sku, description) {
                var promoType = $('#productModal-currentPromotionType').val();
                var countRequiredDOM = $('#' + promoType + '-count-required');

                switch (promoType) {
                    case 'bogof':
                        $('#bogof-productsRequiredContainer').append("<div id=\"bogof-product1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                            <input type=\"hidden\" name=\"bogof-product-required-1-sku\" value=\"" + sku + "\"/>\n" +
                            "                            Quantity 1 x " + sku + " - " + description + " \n" +
                            "                            <a href=\"#\" onclick=\"return deleteThis(this, 'bogof', 'required');\" class=\"ml-3 text-dark\"><sup>X</sup></a>\n" +
                            "                        </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#bogof-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'xfory':
                        $('#xfory-productsRequiredContainer').append("<div id=\"xfory-product-required-1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"xfory-product-required-1-sku\" value=\"" + sku + "\"/>\n" +
                            "                                <label for=\"xfory-product-required-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"xfory-product-required-1-quantity\" value=\"2\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'xfory');\"/>\n" +
                            "                                <label for=\"xfory-product-required-1-quantity\" class=\"mr-3\"> x " + sku + " - " + description + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'xfory', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        $('#xfory-productsOfferContainer').append("<div id=\"xfory-product1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"xfory-product-offer-1-sku\" value=\"" + sku + "\"/>\n" +
                            "                                <label for=\"xfory-product-offer-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"xfory-product-offer-1-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'xfory');\"/>\n" +
                            "                                <label for=\"xfory-product-offer-1-quantity\" class=\"mr-3\"> x " + sku +" - " + description + "</label>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);

                        $('#xfory-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'percentage':
                        $('#percentage-productsRequiredContainer').append("<div id=\"percentage-product1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"percentage-product-required-1-sku\" value=\"" + sku + "\"/>\n" +
                            "                                <label for=\"percentage-product-required-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"percentage-product-required-1-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'percentage');\"/>\n" +
                            "                                <label for=\"percentage-product-required-1-quantity\" class=\"mr-3\"> x " + sku + " - " + description + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'xfory', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#percentage-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'fixedAmount':
                        $('#fixedAmount-productsRequiredContainer').append("<div id=\"fixedAmount-product1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"fixedAmount-product-required-1-sku\" value=\"" + sku + "\"/>\n" +
                            "                                <label for=\"fixedAmount-product-required-1-value\" class=\"\">Value</label>\n" +
                            "                                <input type=\"number\" name=\"fixedAmount-product-required-1-value\" value=\"\" step=\"0.01\" class=\"py-1 pl-1 mx-1 form-control promo-value\" onChange=\"quantityValueChange(this, 'value');\"/>\n" +
                            "                                <label for=\"fixedAmount-product-required-1-quantity\" class=\"\"> or Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"fixedAmount-product-required-1-quantity\" value=\"1\" step=\"1\" class=\"py-1 pl-1 mx-1 form-control promo-quantity\" onChange=\"quantityValueChange(this, 'quantity');\"/>\n" +
                            "                                <label for=\"fixedAmount-product-required-1-quantity\" class=\"mr-3\"> x " + sku + " - " + description + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'fixedAmount', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#fixedAmount-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'fixedPrice':

                        var itemAlreadyExists = false;
                        var promoItemIds = $('#fixedPrice-productsRequiredContainer').find(".promo-itemId");
                        for (var i = 0; i < promoItemIds.length; i++)  {
                            if (parseInt(promoItemIds.get(i).value) === parseInt(id)) {
                                itemAlreadyExists = true;
                            }
                        }

                        if (itemAlreadyExists) {
                            $('#fixedPrice-duplicateAlert').fadeIn();
                            setTimeout(function() {
                                $('#fixedPrice-duplicateAlert').fadeOut();
                            }, 3000);
                        } else {
                            var nextValidIndex = 1;

                            while (true) {
                                if ($('#fixedPrice-product' + nextValidIndex).length) {
                                    nextValidIndex++;
                                } else {
                                    break;
                                }
                            }

                            $('#fixedPrice-productsRequiredContainer').append("<div id=\"fixedPrice-product" + nextValidIndex + "\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                                "                                    <input type=\"hidden\" name=\"fixedPrice-product-required-" + nextValidIndex + "-sku\" value=\"" + sku + "\"  class=\"promo-itemId\"/>\n" +
                                "                                    <label for=\"fixedPrice-product-required-" + nextValidIndex + "-quantity\" class=\"\">Quantity</label>\n" +
                                "                                    <input type=\"number\" name=\"fixedPrice-product-required-" + nextValidIndex + "-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'fixedPrice');\"/>\n" +
                                "                                    <label for=\"fixedPrice-product-required-" + nextValidIndex + "-quantity\" class=\"mr-3\"> x " + sku + " - " + description + "</label>\n" +
                                "                                    <a href=\"#\" onclick=\"return deleteThis(this, 'fixedPrice', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                                "                                </div>");

                            countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                            $('#fixedPrice-noItemChange').val("false");

                            $('#' + promoType + '-category-required-btn').attr("disabled", true);
                            $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        }
                        break;
                }
                $('#' + promoType + '-promotionItemsType').val("product");
                $('#' + promoType + '-productsRequiredSection').removeClass("is-invalid");
                $('#' + promoType + '-productsOfferSection').removeClass("is-invalid");
            }

            function addPromotionCategory(id, description, categoryCode) {
                var promoType = $('#categoryModal-currentPromotionType').val();
                var countRequiredDOM = $('#' + promoType + '-count-required');

                switch (promoType) {
                    case 'bogof':
                        $('#bogof-productsRequiredContainer').append("<div id=\"bogof-category1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                            <input type=\"hidden\" name=\"bogof-category-required-1-categoryId\" value=\"" + id + "\"/>\n" +
                            "                            Quantity 1 x " + description + (categoryCode != null ? " - " + categoryCode : "") + " \n" +
                            "                            <a href=\"#\" onclick=\"return deleteThis(this, 'bogof', 'required');\" class=\"ml-3 text-dark\"><sup>X</sup></a>\n" +
                            "                        </div>");

                        $('#bogof-noItemChange').val("false");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'xfory':
                        $('#xfory-productsRequiredContainer').append("<div id=\"xfory-category-required-1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"xfory-category-required-1-categoryId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"xfory-category-required-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"xfory-category-required-1-quantity\" value=\"2\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'xfory');\"/>\n" +
                            "                                <label for=\"xfory-category-required-1-quantity\" class=\"mr-3\"> x " + description + (categoryCode != null ? " - " + categoryCode : "") + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'xfory', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        $('#xfory-productsOfferContainer').append("<div id=\"xfory-category1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"xfory-category-offer-1-categoryId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"xfory-category-offer-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"xfory-category-offer-1-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'xfory');\"/>\n" +
                            "                                <label for=\"xfory-category-offer-1-quantity\" class=\"mr-3\"> x " + description + (categoryCode != null ? " - " + categoryCode : "") + "</label>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);

                        $('#xfory-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'percentage':
                        $('#percentage-productsRequiredContainer').append("<div id=\"percentage-category1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"percentage-category-required-1-categoryId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"percentage-category-required-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"percentage-category-required-1-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'percentage');\"/>\n" +
                            "                                <label for=\"percentage-category-required-1-quantity\" class=\"mr-3\"> x " + description + (categoryCode != null ? " - " + categoryCode : "") + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'xfory', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#percentage-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'fixedAmount':
                        $('#fixedAmount-productsRequiredContainer').append("<div id=\"fixedAmount-category1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"fixedAmount-category-required-1-categoryId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"fixedAmount-category-required-1-value\" class=\"\">Value</label>\n" +
                            "                                <input type=\"number\" name=\"fixedAmount-category-required-1-value\" value=\"\" step=\"0.01\" class=\"py-1 pl-1 mx-1 form-control promo-value\" onChange=\"quantityValueChange(this, 'value');\"/>\n" +
                            "                                <label for=\"fixedAmount-category-required-1-quantity\" class=\"\"> or Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"fixedAmount-category-required-1-quantity\" value=\"1\" step=\"1\" class=\"py-1 pl-1 mx-1 form-control promo-quantity\" onChange=\"quantityValueChange(this, 'quantity');\"/>\n" +
                            "                                <label for=\"fixedAmount-category-required-1-quantity\" class=\"mr-3\"> x " + description + (categoryCode != null ? " - " + categoryCode : "") + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'fixedAmount', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#fixedAmount-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'fixedPrice':
                        var itemAlreadyExists = false;
                        var promoItemIds = $('#fixedPrice-productsRequiredContainer').find(".promo-itemId");
                        for (var i = 0; i < promoItemIds.length; i++)  {
                            if (parseInt(promoItemIds.get(i).value) === parseInt(id)) {
                                itemAlreadyExists = true;
                            }
                        }

                        if (itemAlreadyExists) {
                            $('#fixedPrice-duplicateAlert').fadeIn();
                            setTimeout(function() {
                                $('#fixedPrice-duplicateAlert').fadeOut();
                            }, 3000);
                        } else {
                            var nextValidIndex = 1;
                            while (true) {
                                if ($('#fixedPrice-category' + nextValidIndex).length) {
                                    nextValidIndex++;
                                } else {
                                    break;
                                }
                            }

                            $('#fixedPrice-productsRequiredContainer').append("<div id=\"fixedPrice-category" + nextValidIndex + "\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                                "                                    <input type=\"hidden\" name=\"fixedPrice-category-required-" + nextValidIndex + "-categoryId\" value=\"" + id + "\"  class=\"promo-itemId\"/>\n" +
                                "                                    <label for=\"fixedPrice-category-required-" + nextValidIndex + "-quantity\" class=\"\">Quantity</label>\n" +
                                "                                    <input type=\"number\" name=\"fixedPrice-category-required-" + nextValidIndex + "-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'fixedPrice');\"/>\n" +
                                "                                    <label for=\"fixedPrice-category-required-" + nextValidIndex + "-quantity\" class=\"mr-3\"> x " + description + (categoryCode != null ? categoryCode : "") + "</label>\n" +
                                "                                    <a href=\"#\" onclick=\"return deleteThis(this, 'fixedPrice', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                                "                                </div>");

                            countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                            $('#fixedPrice-noItemChange').val("false");

                            $('#' + promoType + '-product-required-btn').attr("disabled", true);
                            $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        }
                        break;
                }

                $('#' + promoType + '-promotionItemsType').val("category");
                $('#' + promoType + '-productsRequiredSection').removeClass("is-invalid");
                $('#' + promoType + '-productsOfferSection').removeClass("is-invalid");
            }

            function addPromotionTag(id, description) {
                var promoType = $('#tagModal-currentPromotionType').val();
                var countRequiredDOM = $('#' + promoType + '-count-required');

                switch (promoType) {
                    case 'bogof':
                        $('#bogof-productsRequiredContainer').append("<div id=\"bogof-tag1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                            <input type=\"hidden\" name=\"bogof-tag-required-1-tagId\" value=\"" + id + "\"/>\n" +
                            "                            Quantity 1 x " + description + " \n" +
                            "                            <a href=\"#\" onclick=\"return deleteThis(this, 'bogof', 'required');\" class=\"ml-3 text-dark\"><sup>X</sup></a>\n" +
                            "                        </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);

                        $('#bogof-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'xfory':
                        $('#xfory-productsRequiredContainer').append("<div id=\"xfory-tag-required-1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"xfory-tag-required-1-tagId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"xfory-tag-required-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"xfory-tag-required-1-quantity\" value=\"2\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'xfory');\"/>\n" +
                            "                                <label for=\"xfory-tag-required-1-quantity\" class=\"mr-3\"> x " + description + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'xfory', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        $('#xfory-productsOfferContainer').append("<div id=\"xfory-tag1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"xfory-tag-offer-1-tagId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"xfory-tag-offer-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"xfory-tag-offer-1-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'xfory');\"/>\n" +
                            "                                <label for=\"xfory-tag-offer-1-quantity\" class=\"mr-3\"> x " + description + "</label>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);

                        $('#xfory-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'percentage':
                        $('#percentage-productsRequiredContainer').append("<div id=\"percentage-tag1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"percentage-tag-required-1-tagId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"percentage-tag-required-1-quantity\" class=\"\">Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"percentage-tag-required-1-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'percentage');\"/>\n" +
                            "                                <label for=\"percentage-tag-required-1-quantity\" class=\"mr-3\"> x " + description + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'xfory', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#percentage-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'fixedAmount':
                        $('#fixedAmount-productsRequiredContainer').append("<div id=\"fixedAmount-tag1\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                            "                                <input type=\"hidden\" name=\"fixedAmount-tag-required-1-tagId\" value=\"" + id + "\"/>\n" +
                            "                                <label for=\"fixedAmount-tag-required-1-value\" class=\"\">Value</label>\n" +
                            "                                <input type=\"number\" name=\"fixedAmount-tag-required-1-value\" value=\"\" step=\"0.01\" class=\"py-1 pl-1 mx-1 form-control promo-value\" onChange=\"quantityValueChange(this, 'value');\"/>\n" +
                            "                                <label for=\"fixedAmount-tag-required-1-quantity\" class=\"\"> or Quantity</label>\n" +
                            "                                <input type=\"number\" name=\"fixedAmount-tag-required-1-quantity\" value=\"1\" step=\"1\" class=\"py-1 pl-1 mx-1 form-control promo-quantity\" onChange=\"quantityValueChange(this, 'quantity');\"/>\n" +
                            "                                <label for=\"fixedAmount-tag-required-1-quantity\" class=\"mr-3\"> x " + description + "</label>\n" +
                            "                                <a href=\"#\" onclick=\"return deleteThis(this, 'fixedAmount', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                            "                            </div>");

                        countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);
                        $('#fixedAmount-noItemChange').val("false");

                        $('#' + promoType + '-product-required-btn').attr("disabled", true);
                        $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        $('#' + promoType + '-tag-required-btn').attr("disabled", true);
                        break;
                    case 'fixedPrice':
                        var itemAlreadyExists = false;
                        var promoItemIds = $('#fixedPrice-productsRequiredContainer').find(".promo-itemId");
                        for (var i = 0; i < promoItemIds.length; i++)  {
                            if (parseInt(promoItemIds.get(i).value) === parseInt(id)) {
                                itemAlreadyExists = true;
                            }
                        }

                        if (itemAlreadyExists) {
                            $('#fixedPrice-duplicateAlert').fadeIn();
                            setTimeout(function() {
                                $('#fixedPrice-duplicateAlert').fadeOut();
                            }, 3000);
                        } else {
                            var nextValidIndex = 1;
                            while (true) {
                                if ($('#fixedPrice-tag' + nextValidIndex).length) {
                                    nextValidIndex++;
                                } else {
                                    break;
                                }
                            }

                            $('#fixedPrice-productsRequiredContainer').append("<div id=\"fixedPrice-tag" + nextValidIndex + "\" class=\"offset-1 promotion-product-container form-inline mt-3\">\n" +
                                "                                    <input type=\"hidden\" name=\"fixedPrice-tag-required-" + nextValidIndex + "-tagId\" value=\"" + id + "\"  class=\"promo-itemId\"/>\n" +
                                "                                    <label for=\"fixedPrice-tag-required-" + nextValidIndex + "-quantity\" class=\"\">Quantity</label>\n" +
                                "                                    <input type=\"number\" name=\"fixedPrice-tag-required-" + nextValidIndex + "-quantity\" value=\"1\" class=\"py-1 pl-1 mx-1 form-control\" onChange=\"quantityChange(this, 'fixedPrice');\"/>\n" +
                                "                                    <label for=\"fixedPrice-tag-required-" + nextValidIndex + "-quantity\" class=\"mr-3\"> x " + description + "</label>\n" +
                                "                                    <a href=\"#\" onclick=\"return deleteThis(this, 'fixedPrice', 'required');\" class=\"text-dark\"><sup>X</sup></a>\n" +
                                "                                </div>");

                            countRequiredDOM.val(parseInt(countRequiredDOM.val()) + 1);

                            $('#fixedPrice-noItemChange').val("false");

                            $('#' + promoType + '-product-required-btn').attr("disabled", true);
                            $('#' + promoType + '-category-required-btn').attr("disabled", true);
                        }
                        break;
                }
                $('#' + promoType + '-promotionItemsType').val("tag");
                $('#' + promoType + '-productsRequiredSection').removeClass("is-invalid");
                $('#' + promoType + '-productsOfferSection').removeClass("is-invalid");
            }

            function deleteThis(DOM, promoType, groupType) {
                DOM.parentElement.remove();

                var countDOM = $('#' + promoType + '-count-' + groupType);
                countDOM.val(parseInt(countDOM.val()) - 1);

                switch (promoType) {
                    case 'bogof':
                        $('#bogof-noItemChange').val("false");
                        break;
                    case 'xfory':
                        $('#xfory-productsOfferContainer').html("");
                        $('#xfory-noItemChange').val("false");
                        break;
                    case 'percentage':
                        $('#percentage-noItemChange').val("false");
                        break;
                    case 'fixedAmount':
                        $('#fixedAmount-noItemChange').val("false");
                        break;
                    case 'fixedPrice':
                        $('#fixedPrice-noItemChange').val("false");
                        break;
                }

                if (countDOM.val() === '0') {
                    $('#' + promoType + '-product-' + groupType + '-btn').attr("disabled", false);
                    $('#' + promoType + '-category-' + groupType + '-btn').attr("disabled", false);
                    $('#' + promoType + '-tag-' + groupType + '-btn').attr("disabled", false);
                }
                return false
            }

            function quickValidateSubmit(promoType) {
                var error = false;
                var errorString = "";

                if ($('#' + promoType + '-description').val() !== "")  {
                    if ($('#' + promoType + '-description').length > 200) {
                        error = true;
                        errorString = errorString.concat("\n<li>Description cannot be longer than 200 characters</li>");
                        $('#' + promoType + '-description').addClass("is-invalid");
                    }
                } else {
                    error = true;
                    errorString = errorString.concat("\n<li>Please enter a description</li>");

                    $('#' + promoType + '-description').addClass("is-invalid");
                }

                if ($('#' + promoType + '-receiptDescription').val() !== "")  {
                    if ($('#' + promoType + '-receiptDescription').length > 50) {
                        error = true;
                        errorString = errorString.concat("\n<li>Receipt Description cannot be longer than 50 characters</li>");
                        $('#' + promoType + '-receiptDescription').addClass("is-invalid");
                    }
                } else {
                    error = true;
                    errorString = errorString.concat("\n<li>Please enter a receipt description</li>");

                    $('#' + promoType + '-receiptDescription').addClass("is-invalid");
                }

                if (!moment($('#' + promoType + '-startDate').val(), "dddd DD MMMM YYYY", true).isValid()) {
                    error = true;
                    errorString = errorString.concat("\n<li>Start date invalid</li>");
                    $('#' + promoType + '-startDate').addClass("is-invalid");
                }

                if (!moment($('#' + promoType + '-endDate').val(), "dddd DD MMMM YYYY", true).isValid()) {
                    error = true;
                    errorString = errorString.concat("\n<li>End date invalid</li>");
                    $('#' + promoType + '-endDate').addClass("is-invalid");
                }

                if ($('#' + promoType + '-amount').length !== 0) {
                    if ($('#' + promoType + '-amount').val() !== "") {
                        if (parseFloat($('#' + promoType + '-amount').val()) > parseFloat($('#' + promoType + '-amount').prop("max"))) {
                            error = true;
                            errorString = errorString.concat("\n<li>Amount should be less than or equal to " + $('#' + promoType + '-amount').prop("max") + "</li>");
                            $('#' + promoType + '-amount').addClass("is-invalid");
                        } else if (parseFloat($('#' + promoType + '-amount').val()) < parseFloat($('#' + promoType + '-amount').prop("min"))) {
                            error = true;
                            errorString = errorString.concat("\n<li>Amount should be greater than or equal to " + $('#' + promoType + '-amount').prop("min") + "</li>");
                            $('#' + promoType + '-amount').addClass("is-invalid");
                        }
                    } else {
                        error = true;
                        errorString = errorString.concat("\n<li>Please enter an amount</li>");
                        $('#' + promoType + '-amount').addClass("is-invalid");
                    }
                }

                var countDOM = $('#' + promoType + '-count-required');
                if (parseInt(countDOM.val()) < 1) {
                    error = true;
                    errorString = errorString.concat("\n<li>At least one promotion item should be added</li>");
                    $('#' + promoType + '-productsRequiredSection').addClass("is-invalid");
                    $('#' + promoType + '-productsOfferSection').addClass("is-invalid");
                } else {
                    if ($('#' + promoType + '-productsRequiredSection').find(".is-invalid").length > 0) {
                        error = true;
                        errorString = errorString.concat("\n<li>An error is present in the items section</li>");
                        $('#' + promoType + '-productsRequiredSection').addClass("is-invalid");
                        $('#' + promoType + '-productsOfferSection').addClass("is-invalid");
                    }
                }

                if (!error) {
                    $('#' + promoType + '-form').submit();
                } else {
                    $('#maintenance-errors').html("<ul>" + errorString + "\n</ul>");
                    $('#maintenance-errors').prop("hidden", false);
                }
            }

            function quantityChange(DOM, promoType) {
                if(parseInt($(DOM).val()) > 0 && parseInt($(DOM).val()) < 99) {
                    $(DOM).removeClass("is-invalid");
                    $('#' + promoType + '-productsRequiredSection').removeClass("is-invalid");
                    $('#' + promoType + '-productsOfferSection').removeClass("is-invalid");
                } else {
                    $(DOM).addClass("is-invalid");
                }
                $('#' + promoType + '-noItemChange').val('false');

            }

            function quantityValueChange(DOM, domType) {
                if (String(domType).valueOf() === "quantity") {
                    if (parseInt($(DOM).val()) > 0 && parseInt($(DOM).val() < 99)) {
                        $(DOM).removeClass("is-invalid");
                        $('#fixedPrice-productsRequiredSection').removeClass("is-invalid");
                    } else {
                        $(DOM).addClass("is-invalid");
                    }
                } else {
                    if (parseFloat($(DOM).val()) > 0 && parseFloat($(DOM).val()) < 9999.99) {
                        $(DOM).removeClass("is-invalid");
                        $('#fixedPrice-productsRequiredSection').removeClass("is-invalid");
                    } else {
                        $(DOM).addClass("is-invalid");
                    }
                }
                $('#fixedAmount-noItemChange').val('false');

                $(DOM).parent().find(String(domType).valueOf() === "quantity" ? ".promo-value" : ".promo-quantity").val("");
            }

        </script>
    </head>

    <body>
        <section id="breadcrumb-container" class="container-fluid">
            <nav aria-label="breadcrumb">
                <div class="row mt-4">
                    <div class="col">
                        <ol class="breadcrumb">
                            <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                            <li id="breadcrumb-2" class="breadcrumb-item" aria-current="page"><g:link controller="promotion" action="index">Promotion Search</g:link></li>
                            <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">${promotion?.description ?: "Add Promotion"}</li>
                        </ol>
                    </div>
                </div>
            </nav>
        </section>

        <section id="maintenance-section" class="container-fluid">
            <div class="header-wl mt-3">
                <h2 class="mx-auto">Promotion Maintenance</h2>
            </div>

            <g:hasErrors bean="${promotion}">
                <div id="maintenance-errors-list" class="alert alert-danger alert-wl mx-0" role="alert">
                    <g:renderErrors bean="${promotion}" as="list" />
                </div>
            </g:hasErrors>

            <g:if test="${flash.message}">
                <div id="maintenance-message" class="alert alert-success alert-wl mx-0" role="alert">${flash.message}</div>
            </g:if>

            <g:if test="${flash.badPromoMessage}">
                <div id="maintenance-bad-promo-message" class="alert alert-danger alert-wl mx-0" role="alert"><g:message code="${flash.badPromoMessage}"/></div>
            </g:if>

            <div id="maintenance-errors" class="alert alert-danger alert-wl mx-0" role="alert" hidden>

            </div>

            <g:render template="maintenanceForm" model="[promotion: promotion,
                                                         promoType: promoType,
                                                         productsRequired: productsRequired,
                                                         productsOffer: productsOffer,
                                                         categoriesRequired: categoriesRequired,
                                                         categoriesOffer: categoriesOffer,
                                                         tagsRequired: tagsRequired,
                                                         tagsOffer: tagsOffer,
                                                         productItemType: productItemType]" />
        </section>

        <g:render template="productSearch"/>
        <g:render template="categorySearch"/>
        <g:render template="tagSearch"/>
    </body>
</html>