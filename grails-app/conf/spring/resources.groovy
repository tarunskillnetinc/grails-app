import uk.co.wonderlane.wlpos.WellUserDetailsService
import uk.co.wonderlane.wlpos.WellAuthenticationProvider
import uk.co.wonderlane.wlpos.WellAuthenticationDetailsSource
import uk.co.wonderlane.wlpos.StoreNumberValidatorService
import uk.co.wonderlane.wlpos.ProductService
import uk.co.wonderlane.wlpos.ShiftService
import uk.co.wonderlane.wlpos.UserPasswordEncoderListener

// Place your Spring DSL code here
beans = {
    userDetailsService(WellUserDetailsService)

    wellAuthenticationProvider(WellAuthenticationProvider) {
        storeNumberValidator = ref('storeNumberValidator')
        userDetailsService = ref('userDetailsService')
        passwordEncoder = ref('passwordEncoder')
        userCache = ref('userCache')
        preAuthenticationChecks = ref('preAuthenticationChecks')
        postAuthenticationChecks = ref('postAuthenticationChecks')
        authoritiesMapper = ref('authoritiesMapper')
        hideUserNotFoundExceptions = true
    }

    userPasswordEncoderListener(UserPasswordEncoderListener)

    authenticationDetailsSource(WellAuthenticationDetailsSource)
    storeNumberValidator(StoreNumberValidatorService)

    productService(ProductService,
                   grailsApplication.config.getProperty('mysql.wlpos.host'),
                   grailsApplication.config.getProperty('mysql.wlpos.port'),
                   grailsApplication.config.getProperty('mysql.wlpos.database'),
                   grailsApplication.config.getProperty('mysql.wlpos.username'),
                   grailsApplication.config.getProperty('mysql.wlpos.password')) {

        springSecurityService = ref('springSecurityService')
    }

    shiftService(ShiftService,
            grailsApplication.config.getProperty('mysql.transactions.host'),
            grailsApplication.config.getProperty('mysql.transactions.port'),
            grailsApplication.config.getProperty('mysql.transactions.database'),
            grailsApplication.config.getProperty('mysql.transactions.username'),
            grailsApplication.config.getProperty('mysql.transactions.password')) {

        springSecurityService = ref('springSecurityService')
    }
}