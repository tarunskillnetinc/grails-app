package uk.co.wonderlane.wlpos

import grails.util.Environment

class BootStrap {
    def grailsApplication  // Inject Grails application config

    def init = { servletContext ->
        switch (Environment.current.name) {
            case 'development':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            case 'production':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            case 'hades':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            case 'persephone':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            case 'cerberus':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            case 'zagreus':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            case 'preprod':
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))  // London time
                break
            default:
                TimeZone.setDefault(TimeZone.getTimeZone("UTC"))  // Default to UTC
        }
    }
    def destroy = {
    }
}
