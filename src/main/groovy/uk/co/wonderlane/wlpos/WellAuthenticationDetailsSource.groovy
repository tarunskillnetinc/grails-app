package uk.co.wonderlane.wlpos

import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource

import javax.servlet.http.HttpServletRequest

class WellAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    @Override
    WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        def details = new WellAuthenticationDetails(context)

        details.storeId = context.getParameter('storeId')

        return details
    }
}