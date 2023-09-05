package uk.co.wonderlane.wlpos

import groovy.transform.CompileStatic
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
import org.springframework.web.multipart.support.StandardServletMultipartResolver

import javax.servlet.http.HttpServletRequest

@CompileStatic
class MaxFileUploadSizeResolver extends StandardServletMultipartResolver {

    static final String FILE_SIZE_EXCEEDED_ERROR = "fileSizeExceeded"

    @Override
    MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {

        try {
            return super.resolveMultipart(request)
        } catch (MaxUploadSizeExceededException e) {
            request.setAttribute(FILE_SIZE_EXCEEDED_ERROR, e.cause.cause as SizeLimitExceededException)
            return new DefaultMultipartHttpServletRequest(request, new LinkedMultiValueMap<String, MultipartFile>(), new LinkedHashMap<String, String[]>(), new LinkedHashMap<String, String>())
        }
    }
}