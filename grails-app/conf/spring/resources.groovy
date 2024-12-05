import grails.util.Environment
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import uk.co.wonderlane.wlpos.*
import uk.co.wonderlane.wlpos.dataaccess.DatabaseCredentials

// Place your Spring DSL code here
beans = {
    userDetailsService(WonderLaneUserDetailsService)
    retailerConfigService(RetailerConfigService)

    wonderLaneAuthenticationProvider(WonderLaneAuthenticationProvider) {
        storeNumberValidator = ref('storeNumberValidator')
        retailerProvider = ref('retailerProvider')
        userDetailsService = ref('userDetailsService')
        passwordEncoder = ref('passwordEncoder')
        userCache = ref('userCache')
        preAuthenticationChecks = ref('preAuthenticationChecks')
        postAuthenticationChecks = ref('postAuthenticationChecks')
        authoritiesMapper = ref('authoritiesMapper')
        retailerConfigService = ref('retailerConfigService')
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
        storeService = ref('storeService')
        supplierService = ref('supplierService')
        productService = ref('productService')
        userService = ref('userService')
        rabbitService = ref('rabbitService')
        gsonProvider = ref('gsonProvider')
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
        storeService = ref('storeService')
        userService = ref('userService')
        cashManagementService = ref('cashManagementService')
        locationService = ref('locationService')
        reportingService = ref('reportingService')
        safeService = ref('safeService')
        safeManagementService = ref("safeManagementService")
        commonService = ref("commonService")
        financialWeekService = ref("financialWeekService")
    }

    rabbitService(BackOfficeRabbitService,
            grailsApplication.config.getProperty('rabbitmq.host'),
            Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.port')),
            grailsApplication.config.getProperty('rabbitmq.apiProtocol'),
            Integer.parseInt(grailsApplication.config.getProperty('rabbitmq.apiPort')),
            grailsApplication.config.getProperty('rabbitmq.username'),
            grailsApplication.config.getProperty('rabbitmq.password'),
            Boolean.parseBoolean(grailsApplication.config.getProperty('rabbitmq.useSsl')),
            grailsApplication.config.getProperty('rabbitmq.senderExchange')
            ) {

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
        productService = ref('productService')
        productListService = ref('productListService')
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

    retailerConfigService(RetailerConfigService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {

        springSecurityService = ref('springSecurityService')
        gsonProvider = ref("gsonProvider")
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

    barcodeSignifierService(BarcodeSignifierService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
                    grailsApplication.config.getProperty('mysql.wlpos.username'),
                    grailsApplication.config.getProperty('mysql.wlpos.password'),
                    grailsApplication.config.getProperty('mysql.wlpos.database'))) {
        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
        messageSource = ref('messageSource')
    }

    imageRecordService(ImageRecordService) {
        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
    }
    financialWeekService(FinancialWeekService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.transactions.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.transactions.port')),
                    grailsApplication.config.getProperty('mysql.transactions.username'),
                    grailsApplication.config.getProperty('mysql.transactions.password'),
                    grailsApplication.config.getProperty('mysql.transactions.database'))) {
        springSecurityService = ref('springSecurityService')
        sessionFactory = ref('sessionFactory')
        commonService = ref("commonService")
    }
    cashManagementService(CashManagementService, new DatabaseCredentials(grailsApplication.config.getProperty('mysql.wlpos.host'),
            Integer.parseInt(grailsApplication.config.getProperty('mysql.wlpos.port')),
            grailsApplication.config.getProperty('mysql.wlpos.username'),
            grailsApplication.config.getProperty('mysql.wlpos.password'),
            grailsApplication.config.getProperty('mysql.wlpos.database'))) {
        springSecurityService = ref('springSecurityService')
        gsonProvider = ref("gsonProvider")
    }

    loyaltyService(LoyaltyService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.loyalty.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.loyalty.port')),
                    grailsApplication.config.getProperty('mysql.loyalty.username'),
                    grailsApplication.config.getProperty('mysql.loyalty.password'),
                    grailsApplication.config.getProperty('mysql.loyalty.database'))) {

        springSecurityService = ref('springSecurityService')
        messageSource = ref('messageSource')
        rabbitService = ref('rabbitService')
    }

    safeService(SafeService) {
        springSecurityService = ref('springSecurityService')
        messageSource = ref('messageSource')
        rabbitService = ref('rabbitService')
        locationService = ref('locationService')
    }


    safeManagementService(SafeManagementService,
            new DatabaseCredentials(grailsApplication.config.getProperty('mysql.transactions.host'),
                    Integer.parseInt(grailsApplication.config.getProperty('mysql.transactions.port')),
                    grailsApplication.config.getProperty('mysql.transactions.username'),
                    grailsApplication.config.getProperty('mysql.transactions.password'),
                    grailsApplication.config.getProperty('mysql.transactions.database'))) {

        springSecurityService = ref('springSecurityService')
        gsonProvider = ref("gsonProvider")
        userService = ref('userService')
        commonService = ref("commonService")
        financialWeekService = ref("financialWeekService")
        cashManagementService = ref("cashManagementService")
        safeService = ref("safeService")

    }

    tenderMovementService(TenderMovementService) {
        springSecurityService = ref('springSecurityService')
        storeService = ref('storeService')
        reportingService = ref('reportingService')
        locationService = ref('locationService')
        safeManagementService = ref('safeManagementService')
        shiftService = ref('shiftService')
        userService = ref('userService')
        safeService = ref('safeService')
    }



    gsonProvider(GsonProvider)

    Environment.executeForCurrentEnvironment {
        environments {
            development {
                //Can be tested with local s3 bucket
                imageService(AmazonImageService) {
                    s3Client = S3Client.builder()
                            .region(Region.US_EAST_1)
                            .endpointOverride(URI.create("http://localhost:" + grailsApplication.config.getProperty('wlpos.localeS3Port')))
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(grailsApplication.config.getProperty('wlpos.localeS3AccessKey'),
                                    grailsApplication.config.getProperty('wlpos.localeS3SecretKey'))))
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                            .build();
                    config = grailsApplication.config
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            hades {
                imageService(AmazonImageService) {
                    s3Client = S3Client.builder().region(Region.EU_WEST_1).build()
                    config = grailsApplication.config
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            persephone {
                imageService(AmazonImageService) {
                    s3Client = S3Client.builder().region(Region.EU_WEST_1).build()
                    config = grailsApplication.config
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            cerberus {
                imageService(AmazonImageService) {
                    s3Client = S3Client.builder().region(Region.EU_WEST_1).build()
                    config = grailsApplication.config
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            preprod {
                imageService(AmazonImageService) {
                    s3Client = S3Client.builder().region(Region.EU_WEST_1).build()
                    config = grailsApplication.config
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
            production {
                imageService(AmazonImageService) {
                    s3Client = S3Client.builder().region(Region.EU_WEST_1).build()
                    config = grailsApplication.config
                }
                brandAssetsService(AmazonBrandAssetsService, grailsApplication.config.getProperty('wlpos.brandAssetsBucket')) {
                    springSecurityService = ref('springSecurityService')
                }
            }
        }
    }

    multipartResolver(MaxFileUploadSizeResolver)
}