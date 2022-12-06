package uk.co.wonderlane.wlpos

import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import spock.lang.Specification

class LoginControllerSpec extends Specification implements ControllerUnitTest<LoginController>, DataTest{

    def setup() {}

    def cleanup() {}

    //------------------- Calling Auth Fail Callback Action ---------------------------------------------//

    def 'Test auth fail callback method'() {

        given:
        session."${WebAttributes.AUTHENTICATION_EXCEPTION}" = passingException

        controller.springSecurityService = Stub(SpringSecurityService) {
            getPrincipal() >>new HashMap(){{
                put("retailerId", 9);
                put("storeId", 234);
                put("storeNumber", 100)
            }}

            isAjax( _ ) >> ajaxCall
        }

        when: 'The auth fail action is executed'
        controller.authfail()

        then: 'successfully get snapshot'
        if (!ajaxCall){
            assert flash.message == responseException
            assert response.redirectedUrl.startsWith('/login/auth')
        }else {
            assert response.json == [error : responseException]
        }

        where: 'Pass following input parameters'
        passingException                                      || ajaxCall || responseException
        new AccountExpiredException('Expired')                || false    || "Account Expired"
        new CredentialsExpiredException('Credentials expired')|| false    || "Password Expired"
        new DisabledException('Account disabled')             || false    || "Account Disabled"
        new LockedException('Account locked')                 || false    || "Account Locked"
        new SessionAuthenticationException('Session exceeded')|| false    || "Sorry, you have exceeded your maximum number of open sessions."
        new BadCredentialsException('Bad credential')         || false    || "Bad credential"
        new UnsupportedOperationException('Bad stuff')        || false    || "Authentication Failure"
        new AccountExpiredException('Expired')                || true     || "Account Expired"
        new CredentialsExpiredException('Credentials expired')|| true     || "Password Expired"
        new DisabledException('Account disabled')             || true     || "Account Disabled"
        new LockedException('Account locked')                 || true     || "Account Locked"
        new SessionAuthenticationException('Session exceeded')|| true     || "Sorry, you have exceeded your maximum number of open sessions."
        new BadCredentialsException('Bad credential')         || true     || "Bad credential"
        new UnsupportedOperationException('Bad stuff')        || true     || "Authentication Failure"
        null                                                  || true     || ""

    }
}
