import grails.util.Environment
import uk.co.wonderlane.wlpos.AmazonBrandAssetsService
import uk.co.wonderlane.wlpos.BrandAssetsService
import uk.co.wonderlane.wlpos.HardwareService
import uk.co.wonderlane.wlpos.ImageService
import uk.co.wonderlane.wlpos.RetailerService
import uk.co.wonderlane.wlpos.StoreService
import uk.co.wonderlane.wlpos.WonderLaneUserDetailsService
import uk.co.wonderlane.wlpos.WonderLaneAuthenticationProvider
import uk.co.wonderlane.wlpos.WonderLaneAuthenticationDetailsSource
import uk.co.wonderlane.wlpos.StoreNumberValidatorService
import uk.co.wonderlane.wlpos.ProductService
import uk.co.wonderlane.wlpos.ProductListService
import uk.co.wonderlane.wlpos.ShelfEdgeLabelService
import uk.co.wonderlane.wlpos.SupplierService
import uk.co.wonderlane.wlpos.OrderService
import uk.co.wonderlane.wlpos.NisaService
import uk.co.wonderlane.wlpos.ShiftService
import uk.co.wonderlane.wlpos.SnapshotService
import uk.co.wonderlane.wlpos.GroupService
import uk.co.wonderlane.wlpos.BackOfficeRabbitService
import uk.co.wonderlane.wlpos.AmazonImageService
import uk.co.wonderlane.wlpos.UserPasswordEncoderListener
import uk.co.wonderlane.wlpos.GsonProvider
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

// Place your Spring DSL code here
beans = {
    userDetailsService(WonderLaneUserDetailsService)

    wonderLaneAuthenticationProvider(WonderLaneAuthenticationProvider) {
        storeNumberValidator = ref('storeNumberValidator')
        retailerProvider = ref('retailerProvider')
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
    retailerProvider(RetailerService)

    storeNumberValidator(StoreNumberValidatorService) {
        storeService = ref('storeService')
    }

    productService(ProductService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
        gsonProvider = ref("gsonProvider")
        rabbitService = ref('rabbitService')
    }

    productListService(ProductListService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
    }

    shelfEdgeLabelService(ShelfEdgeLabelService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
    }

    supplierService(SupplierService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
    }

    shiftService(ShiftService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.transactions.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.transactions.port')),
                    grailsApplication.config.getProperty('mysql.transactions.username'),
                    grailsApplication.config.getProperty('mysql.transactions.password'),
                    grailsApplication.config.getProperty('mysql.transactions.database'))) {

        springSecurityService = ref('springSecurityService')
        gsonProvider = ref("gsonProvider")
    }

    snapshotService(SnapshotService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.transactions.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.transactions.port')),
                    grailsApplication.config.getProperty('mysql.transactions.username'),
                    grailsApplication.config.getProperty('mysql.transactions.password'),
                    grailsApplication.config.getProperty('mysql.transactions.database'))) {

        springSecurityService = ref('springSecurityService')
        gsonProvider = ref("gsonProvider")
    }

    rabbitService(BackOfficeRabbitService,
            grailsApplication.config.getProperty('rabbitmq.host'),
            Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')),
            grailsApplication.config.getProperty('rabbitmq.apiProtocol'),
            Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.apiPort')),
            grailsApplication.config.getProperty('rabbitmq.username'),
            grailsApplication.config.getProperty('rabbitmq.password'),
            Boolean.parseBoolean(grailsApplication.config.getProperty('rabbitmq.useSsl'))) {

        springSecurityService = ref('springSecurityService')
    }

    orderService(OrderService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
        userService = ref('userService')
        nisaService = ref('nisaService')
    }

    nisaService(NisaService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {
        springSecurityService = ref('springSecurityService')
    }

    groupService(GroupService) {
        springSecurityService = ref('springSecurityService')
    }

    storeService(StoreService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
    }

    hardwareService(HardwareService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {
        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
    }

    gsonProvider(GsonProvider)

    Environment.executeForCurrentEnvironment {
        environments {
            development {
                imageService(ImageService, grailsApplication.config.getProperty('wlpos.customerDisplayImageDirectory'), grailsApplication.config.getProperty('wlpos.receiptImageDirectory'), grailsApplication.config.getProperty('wlpos.buttonImageDirectory')) {
                    springSecurityService = ref('springSecurityService')
                }
                brandAssetsService(BrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsDirectory')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            test {
                imageService(AmazonImageService, grailsApplication.config.getProperty('wlpos.customerDisplayImageBucket'), grailsApplication.config.getProperty('wlpos.receiptImageBucket'), grailsApplication.config.getProperty('wlpos.buttonImageBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            production {
                imageService(AmazonImageService, grailsApplication.config.getProperty('wlpos.customerDisplayImageBucket'), grailsApplication.config.getProperty('wlpos.receiptImageBucket'), grailsApplication.config.getProperty('wlpos.buttonImageBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            prestage {
                imageService(ImageService, grailsApplication.config.getProperty('wlpos.customerDisplayImageDirectory'), grailsApplication.config.getProperty('wlpos.receiptImageDirectory'), grailsApplication.config.getProperty('wlpos.buttonImageDirectory')) {
                    springSecurityService = ref('springSecurityService')
                }
                brandAssetsService(BrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsDirectory')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            stage {
                imageService(ImageService, grailsApplication.config.getProperty('wlpos.customerDisplayImageDirectory'), grailsApplication.config.getProperty('wlpos.receiptImageDirectory'), grailsApplication.config.getProperty('wlpos.buttonImageDirectory')) {
                    springSecurityService = ref('springSecurityService')
                }
                brandAssetsService(BrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsDirectory')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
        }
    }
}