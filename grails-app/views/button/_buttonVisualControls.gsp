<div class="form-group row margin-top-2rem">
    <label for="bgColourInput" class="col-4 col-lg-3 offset-lg-1 col-form-label text-right pr-4">Background Colour</label>
    <div class="col-8 col-lg-1">
        <div class="colourBox">
            <g:field name="bgColourInput" type="color" value="${button?.getBgColour()}"/>
        </div>
    </div>
    <label for="textColourInput" class="col-4 col-lg-2 offset-lg-1 col-form-label text-right pr-4">Text Colour</label>
    <div class="col-8 col-lg-1">
        <div class="colourBox">
            <g:field name="textColourInput" type="color" value="${button?.getTextColour()}"/>
        </div>
    </div>
</div>
<g:if test="${notFixed}">
    <div class="form-group row margin-top-2rem">
        <label for="textDisplayInput" class="col-4 col-lg-2 offset-lg-2 col-form-label text-right pr-4"><g:message code="button.displayText.label"/></label>
        <div id="buttonTextCheck" class="col-8 col-lg-1 align-content-center">
            <g:checkBox name="textDisplayInput" value="${button?.textDisplay}" class="wl-checkbox"/>
        </div>

        <div class="offset-2 col-10 offset-lg-0 col-lg-2 ">
            <label class="btn btn-wl">
                <button id="imageInput" hidden onclick="$('#image').click();"></button>
                <g:message code="button.upload.button"/>
            </label>
        </div>

        <div class="offset-2 col-10 offset-lg-0 col-lg-2">
            <div id="imageRemoveBtn" class="btn btn-wl" ${button?.imageDisplay? '' : 'disabled="disabled"'}>
                <g:message code="button.remove.button"/>
            </div>
        </div>
    </div>
</g:if>
<div class="form-group row margin-top-2rem">
    <div class="col-12 d-flex justify-content-center">
        <div class="button-example">
            <g:if test="${buttonImage}">
                <img src="data:image/png;base64,${buttonImage.encodeBase64()}" class="justify-content-center button-image" />
            </g:if>
            <g:else>
                <img src="" hidden class="justify-content-center button-image"/>
            </g:else>
            <p id="example-text" class="button-example-text" ${button?.textDisplay? "" : "hidden"}>${button?.description ?: 'Example'}</p>
        </div>
    </div>
</div>