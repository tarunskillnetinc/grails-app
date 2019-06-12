import uk.co.wonderlane.wlpos.WellUserDetailsService
import uk.co.wonderlane.wlpos.WellAuthenticationProvider
import uk.co.wonderlane.wlpos.WellAuthenticationDetailsSource
import uk.co.wonderlane.wlpos.StoreNumberValidatorService

// Place your Spring DSL code here
beans = {
    userDetailsService(WellUserDetailsService)

    wellAuthenticationProvider(WellAuthenticationProvider) {
        storeNumberValidator = ref('storeNumberValidator')
        userDetailsService = ref('userDetailsService')
        passwordEncoder = ref('passwordEncoder')
        userCache = ref('userCache')
        saltSource = ref('saltSource')
        preAuthenticationChecks = ref('preAuthenticationChecks')
        postAuthenticationChecks = ref('postAuthenticationChecks')
        authoritiesMapper = ref('authoritiesMapper')
        hideUserNotFoundExceptions = true
    }

    authenticationDetailsSource(WellAuthenticationDetailsSource)
    storeNumberValidator(StoreNumberValidatorService)
}