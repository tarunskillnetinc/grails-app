import uk.co.wonderlane.wlpos.WonderLaneUserDetailsService
import uk.co.wonderlane.wlpos.WonderLaneAuthenticationProvider
import uk.co.wonderlane.wlpos.WonderLaneAuthenticationDetailsSource
import uk.co.wonderlane.wlpos.StoreNumberValidatorService
import uk.co.wonderlane.wlpos.ProductService
import uk.co.wonderlane.wlpos.ShiftService
import uk.co.wonderlane.wlpos.GroupService
import uk.co.wonderlane.wlpos.BackOfficeRabbitService
import uk.co.wonderlane.wlpos.UserPasswordEncoderListener
import uk.co.wonderlane.wlpos.GsonProvider

// Place your Spring DSL code here
beans = {
    userDetailsService(WonderLaneUserDetailsService)

    wonderLaneAuthenticationProvider(WonderLaneAuthenticationProvider) {
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

    authenticationDetailsSource(WonderLaneAuthenticationDetailsSource)
    storeNumberValidator(StoreNumberValidatorService)

    productService(ProductService,
                   grailsApplication.config.getProperty('mysql.wlpos.host'),
                   grailsApplication.config.getProperty('mysql.wlpos.port'),
                   grailsApplication.config.getProperty('mysql.wlpos.database'),
                   grailsApplication.config.getProperty('mysql.wlpos.username'),
                   grailsApplication.config.getProperty('mysql.wlpos.password')) {

        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
    }

    shiftService(ShiftService,
            grailsApplication.config.getProperty('mysql.transactions.host'),
            grailsApplication.config.getProperty('mysql.transactions.port'),
            grailsApplication.config.getProperty('mysql.transactions.database'),
            grailsApplication.config.getProperty('mysql.transactions.username'),
            grailsApplication.config.getProperty('mysql.transactions.password')) {

        springSecurityService = ref('springSecurityService')
    }

    rabbitService(BackOfficeRabbitService,
            grailsApplication.config.getProperty('rabbitmq.host'),
            grailsApplication.config.getProperty('rabbitmq.port'),
            grailsApplication.config.getProperty('rabbitmq.apiPort'),
            grailsApplication.config.getProperty('rabbitmq.username'),
            grailsApplication.config.getProperty('rabbitmq.password')) {

        springSecurityService = ref('springSecurityService')
    }

    groupService(GroupService) {
        springSecurityService = ref('springSecurityService')
    }

    gsonProvider(GsonProvider)
}