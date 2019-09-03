grails.plugin.springsecurity.password.algorithm = 'bcrypt'
grails.plugin.springsecurity.password.bcrypt.logrounds = 8

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['isAuthenticated()']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']],
	[pattern: '/storeSettings/**', access: 'isAuthenticated()'],
	[pattern: '/button/**', access: 'isAuthenticated()'],
	[pattern: '/buttonGrid/**', access: 'isAuthenticated()'],
	[pattern: '/product/**', access: 'isAuthenticated()'],
	[pattern: '/reporting/**', access: 'isAuthenticated()']
]

grails.plugin.springsecurity.filterChain.chainMap = [
	[pattern: '/assets/**',      filters: 'none'],
	[pattern: '/**/js/**',       filters: 'none'],
	[pattern: '/**/css/**',      filters: 'none'],
	[pattern: '/**/images/**',   filters: 'none'],
	[pattern: '/**/favicon.ico', filters: 'none'],
	[pattern: '/**',             filters: 'JOINED_FILTERS']
]

grails.plugin.springsecurity.providerNames = [
	'wellAuthenticationProvider',
	'anonymousAuthenticationProvider',
	'rememberMeAuthenticationProvider'
]

/*grails.plugin.springsecurity.filterChain.filterNames = [
		'securityContextPersistenceFilter',
		'logoutFilter',
		'wellUsernamePasswordAuthenticationFilter',
		'authenticationProcessingFilter',
		'rememberMeAuthenticationFilter',
		'anonymousAuthenticationFilter',
		'exceptionTranslationFilter',
		'filterInvocationInterceptor'
]*/