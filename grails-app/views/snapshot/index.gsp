<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main" />
    <title>Snapshot Management</title>

    <asset:stylesheet src="bootstrap-datepicker3.min.css" />
    <asset:javascript src="bootstrap-datepicker.min.js" />
    <asset:javascript src="snapshotUrls.js"/>
    <asset:javascript src="snapshotManagement.js"/>
    <asset:javascript src="date-pickers.js"/>

    <script type='text/javascript'>
        $(function() {
            SnapshotUrls.init("${createLink(controller: 'snapshot', action: 'ajaxGetSafe')}",
                "${createLink(controller: 'snapshot', action: 'ajaxGetSnapshots')}",
                "${createLink(controller: 'snapshot', action: 'ajaxGetSnapshot')}",
                "${createLink(controller: 'snapshot', action: 'ajaxSaveSafeCount')}",
                "${createLink(controller: 'snapshot', action: 'ajaxSaveSnapshot')}");

            initDatePickers(
                'startDate',
                'endDate',
                "${(new Date() - 90).format("dd/MM/yyyy")}",
                "${new Date().format("dd/MM/yyyy")}"
            );
            getSnapshots();
        });

        function showModal(banking) {
            $('#bankingCashInModal').modal({show: true});
            $.ajax({
                url: "${createLink(controller: 'snapshot', action: 'ajaxBankingCashIn')}",
                data: { banking: banking },
                success: function(resp) {
                    $("#bankingCashInContent").html(resp);
                    $(".mask-money").maskMoney({allowZero: false});
                    $(".mask-money").maskMoney('mask');
                }
            });
        }

        function cancelModal() {
            $('#bankingCashInModal').modal('hide')
        }

        function saveModal(banking) {
            var value = $("#cashTotal").val();

            $.ajax({
                method: "POST",
                url: "${createLink(controller: 'snapshot', action: 'ajaxSaveBankingCashIn')}",
                data: { banking: banking, value: value },
                success: function(resp) {
                    if (resp === "OK") {
                        // Exit the Add/Edit till modal and return back to index
                        window.location.href = "/snapshot/index";
                    } else {
                        $("#bankingCashInContent").html(resp);
                        $(".mask-money").maskMoney({allowZero: false});
                        $(".mask-money").maskMoney('mask');
                    }
                }
            });
        }

        //Money-mask.js function used for numerical entry
        !function($){"use strict";function e(e,t){var n="";return e.indexOf("-")>-1&&(e=e.replace("-",""),n="-"),e.indexOf(t.prefix)>-1&&(e=e.replace(t.prefix,"")),e.indexOf(t.suffix)>-1&&(e=e.replace(t.suffix,"")),n+t.prefix+e+t.suffix}function t(e,t){return t.allowEmpty&&""===e?"":t.reverse?a(e,t):n(e,t)}function n(t,n){var a,i,o,l=t.indexOf("-")>-1&&n.allowNegative?"-":"",s=t.replace(/[^0-9]/g,"");return a=r(s.slice(0,s.length-n.precision),l,n),n.precision>0&&(i=s.slice(s.length-n.precision),o=new Array(n.precision+1-i.length).join(0),a+=n.decimal+o+i),e(a,n)}function a(t,n){var a,i=t.indexOf("-")>-1&&n.allowNegative?"-":"",o=t.replace(n.prefix,"").replace(n.suffix,""),l=o.split(n.decimal)[0],s="";if(""===l&&(l="0"),a=r(l,i,n),n.precision>0){var c=o.split(n.decimal);c.length>1&&(s=c[1]),a+=n.decimal+s;var u=Number.parseFloat(l+"."+s).toFixed(n.precision).toString().split(n.decimal)[1];a=a.split(n.decimal)[0]+"."+u}return e(a,n)}function r(e,t,n){return e=e.replace(/^0*/g,""),""===(e=e.replace(/\B(?=(\d{3})+(?!\d))/g,n.thousands))&&(e="0"),t+e}$.browser||($.browser={},$.browser.mozilla=/mozilla/.test(navigator.userAgent.toLowerCase())&&!/webkit/.test(navigator.userAgent.toLowerCase()),$.browser.webkit=/webkit/.test(navigator.userAgent.toLowerCase()),$.browser.opera=/opera/.test(navigator.userAgent.toLowerCase()),$.browser.msie=/msie/.test(navigator.userAgent.toLowerCase()),$.browser.device=/android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(navigator.userAgent.toLowerCase()));var i={prefix:"",suffix:"",affixesStay:!0,thousands:",",decimal:".",precision:2,allowZero:!1,allowNegative:!1,doubleClickSelection:!0,allowEmpty:!1,bringCaretAtEndOnFocus:!0},o={destroy:function(){return $(this).unbind(".maskMoney"),$.browser.msie&&(this.onpaste=null),this},applyMask:function(e){return t(e,$(this).data("settings"))},mask:function(e){return this.each(function(){var t=$(this);return"number"==typeof e&&t.val(e),t.trigger("mask")})},unmasked:function(){return this.map(function(){var e,t=$(this).val()||"0",n=-1!==t.indexOf("-");return $(t.split(/\D/).reverse()).each(function(t,n){if(n)return e=n,!1}),t=t.replace(/\D/g,""),t=t.replace(new RegExp(e+"$"),"."+e),n&&(t="-"+t),parseFloat(t)})},unmaskedWithOptions:function(){return this.map(function(){var e=$(this).val()||"0",t=$(this).data("settings")||i,n=new RegExp(t.thousandsForUnmasked||t.thousands,"g");return e=e.replace(n,""),parseFloat(e)})},init:function(n){return n=$.extend($.extend({},i),n),this.each(function(){function a(){var e,t,n,a,r,i=x.get(0),o=0,l=0;return"number"==typeof i.selectionStart&&"number"==typeof i.selectionEnd?(o=i.selectionStart,l=i.selectionEnd):(t=document.selection.createRange())&&t.parentElement()===i&&(a=i.value.length,e=i.value.replace(/\r\n/g,"\n"),(n=i.createTextRange()).moveToBookmark(t.getBookmark()),(r=i.createTextRange()).collapse(!1),n.compareEndPoints("StartToEnd",r)>-1?o=l=a:(o=-n.moveStart("character",-a),o+=e.slice(0,o).split("\n").length-1,n.compareEndPoints("EndToEnd",r)>-1?l=a:(l=-n.moveEnd("character",-a),l+=e.slice(0,l).split("\n").length-1))),{start:o,end:l}}function r(){var e=!(x.val().length>=x.attr("maxlength")&&x.attr("maxlength")>=0),t=a(),n=t.start,r=t.end,i=!(t.start===t.end||!x.val().substring(n,r).match(/\d/)),o="0"===x.val().substring(0,1);return e||i||o}function i(e){w.formatOnBlur||x.each(function(t,n){if(n.setSelectionRange)n.focus(),n.setSelectionRange(e,e);else if(n.createTextRange){var a=n.createTextRange();a.collapse(!0),a.moveEnd("character",e),a.moveStart("character",e),a.select()}})}function o(e){var n,a=x.val().length;x.val(t(x.val(),w)),n=x.val().length,w.reverse||(e-=a-n),i(e)}function l(){var e=x.val();if(!w.allowEmpty||""!==e){var n=e.indexOf(w.decimal);if(w.precision>0)if(n<0)e+=w.decimal+new Array(w.precision+1).join(0);else{var a=e.slice(0,n),r=e.slice(n+1);e=a+w.decimal+r+new Array(w.precision+1-r.length).join(0)}else n>0&&(e=e.slice(0,n));x.val(t(e,w))}}function s(){var e=x.val();return w.allowNegative?""!==e&&"-"===e.charAt(0)?e.replace("-",""):"-"+e:e}function c(e){e.preventDefault?e.preventDefault():e.returnValue=!1}function u(e){var t=(e=e||window.event).which||e.charCode||e.keyCode,n=w.decimal.charCodeAt(0);return void 0!==t&&(!(t<48||t>57)||t===n&&w.reverse?!!r()&&((t!==n||!d())&&(!!w.formatOnBlur||(c(e),p(e),!1))):g(t,e))}function d(){return!v()&&f()}function v(){var e=x.val().length,t=a();return 0===t.start&&t.end===e}function f(){return x.val().indexOf(w.decimal)>-1}function p(e){var t,n,r,i,l=(e=e||window.event).which||e.charCode||e.keyCode,s="";l>=48&&l<=57&&(s=String.fromCharCode(l)),n=(t=a()).start,r=t.end,i=x.val(),x.val(i.substring(0,n)+s+i.substring(r,i.length)),o(n+1)}function g(e,t){return 45===e?(x.val(s()),!1):43===e?(x.val(x.val().replace("-","")),!1):13===e||9===e||(!(!$.browser.mozilla||37!==e&&39!==e||0!==t.charCode)||(c(t),!0))}function m(){setTimeout(function(){l()},0)}function h(){return(parseFloat("0")/Math.pow(10,w.precision)).toFixed(w.precision).replace(new RegExp("\\.","g"),w.decimal)}var w,b,x=$(this);w=$.extend({},n),w=$.extend(w,x.data()),x.data("settings",w),$.browser.device&&x.attr("type","tel"),x.unbind(".maskMoney"),x.bind("keypress.maskMoney",u),x.bind("keydown.maskMoney",function(e){var t,n,r,i,l,s=(e=e||window.event).which||e.charCode||e.keyCode;return void 0!==s&&(t=a(),n=t.start,r=t.end,8!==s&&46!==s&&63272!==s||(c(e),i=x.val(),n===r&&(8===s?""===w.suffix?n-=1:(l=i.split("").reverse().join("").search(/\d/),r=1+(n=i.length-l-1)):r+=1),x.val(i.substring(0,n)+i.substring(r,i.length)),o(n),!1))}),x.bind("blur.maskMoney",function(t){if($.browser.msie&&u(t),w.formatOnBlur&&x.val()!==b&&p(t),""===x.val()&&w.allowEmpty)x.val("");else if(""===x.val()||x.val()===e(h(),w))w.allowZero?w.affixesStay?x.val(e(h(),w)):x.val(h()):x.val("");else if(!w.affixesStay){var n=x.val().replace(w.prefix,"").replace(w.suffix,"");x.val(n)}x.val()!==b&&x.change()}),x.bind("focus.maskMoney",function(){b=x.val(),l();var e,t=x.get(0);w.selectAllOnFocus?t.select():t.createTextRange&&w.bringCaretAtEndOnFocus&&((e=t.createTextRange()).collapse(!1),e.select())}),x.bind("click.maskMoney",function(){var e,t=x.get(0);w.selectAllOnFocus||(t.setSelectionRange&&w.bringCaretAtEndOnFocus?(e=x.val().length,t.setSelectionRange(e,e)):x.val(x.val()))}),x.bind("dblclick.maskMoney",function(){var e,t,n=x.get(0);n.setSelectionRange&&w.bringCaretAtEndOnFocus?(t=x.val().length,e=w.doubleClickSelection?0:t,n.setSelectionRange(e,t)):x.val(x.val())}),x.bind("cut.maskMoney",m),x.bind("paste.maskMoney",m),x.bind("mask.maskMoney",l)})}};$.fn.maskMoney=function(e){return o[e]?o[e].apply(this,Array.prototype.slice.call(arguments,1)):"object"!=typeof e&&e?void $.error("Method "+e+" does not exist on jQuery.maskMoney"):o.init.apply(this,arguments)}}(window.jQuery||window.Zepto);

    </script>
</head>

<body>
    <section id="breadcrumb-container" class="container-fluid">
        <nav aria-label="breadcrumb">
            <div class="row mt-4">
                <div class="col">
                    <ol class="breadcrumb">
                        <li id="breadcrumb-1" class="breadcrumb-item"><g:link uri="/">Home</g:link></li>
                        <li id="breadcrumb-2" class="breadcrumb-item"><g:link controller="shift" action="index">Shift Viewer</g:link></li>
                        <li id="breadcrumb-3" class="breadcrumb-item active" aria-current="page">Snapshot Viewer</li>
                    </ol>
                </div>
            </div>
        </nav>
    </section>

    <section id="snapshots-container" class="container-fluid">
        <div class="header-wl mt-3">
            <h2 id="page-title" class="mx-auto">Snapshot Viewer</h2>
        </div>

        <div class="row mt-4">
            <div class="col-5">
                <div class="card bg-light border-wl">
                    <div id="filter" class="card-header pointer" data-toggle="collapse" data-target="#filterCollapse" aria-expanded="false" aria-controls="filterCollapse">
                        <div class="row">
                            <div class="col-10">Filters</div>
                            <div class="col-2 text-right">
                                <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-caret-down-fill text-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M7.247 11.14L2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="card-body collapse" id="filterCollapse">
                        <g:form name="filtersForm" id="filtersForm">
                            <div class="form-group row">
                                <label for="startDate" class="col-2 col-form-label text-right">Start Date</label>
                                <div class="col-4">
                                    <g:textField name="startDate" class="form-control bottom-border" value="${startDate.toString("dd/MM/yyyy")}" onkeydown="return false" autocomplete="off" />
                                </div>

                                <label for="endDate" class="col-2 col-form-label text-right">End Date</label>
                                <div class="col-4">
                                    <g:textField name="endDate" class="form-control bottom-border" value="${endDate.toString("dd/MM/yyyy")}" onkeydown="return false" autocomplete="off" />
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-4 offset-6 text-right">
                                    <button id="filter-reset-button" type="button" class="btn btn-danger text-right" onclick="resetSnapshotFilters('${startDate.toString("dd/MM/yyyy")}','${endDate.toString("dd/MM/yyyy")}');">Reset Filters</button>
                                    <button id="filter-submit-button" type="button" class="btn btn-wl text-right" onclick="getSnapshots();">Filter</button>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
            <div class="offset-2 col-5">
                <div class="row">
                    <div class="offset-4 col-4">
                        <button id="count-safe-button" type="button" class="btn btn-wl text-center w-100" onclick="showModal(true)">Banking</button>
                    </div>
                    <div class="col-4">
                        <button id="snapshot-viewer-button" type="button" class="btn btn-wl text-center w-100" onclick="showModal(false)">Cash Incoming</button>
                    </div>
                </div>
            </div>
        </div>

        <div id="results-container" class="align-content-center">
            <g:render template="snapshotViewerResults" />
        </div>
    </section>

    <section id="banking-cash-in-modal" class="container-fluid">
        <div class="modal fade" id="bankingCashInModal" tabindex="-1" role="dialog" aria-labelledby="bankingCashInModalLabel"
            aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div id="bankingCashInContent" class="modal-content"></div>
            </div>
        </div>
    </section>

    <section id="snapshot-modal" class="container-fluid">
        <!-- Snapshot modal -->
        <div class="modal fade" id="snapshotModal" tabindex="-1" role="dialog" aria-labelledby="snapshotModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h2>Snapshot Management</h2>
                    </div>

                    <div id="snapshotModalContent"></div>

                    <div class="modal-footer">
                        <button type="button" id="cancelSnapshotButton" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
    </section>
</body>
</html>