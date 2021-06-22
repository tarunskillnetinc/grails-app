package uk.co.wonderlane.wlpos

class GroupLevel {

    int id
    int retailerId
    int level
    String name

    static mapping = {
        autowire true
        table 'grouplevel'
        version false

        retailerId column: "retailerId"
        level column: "level"
        name column: "`name`"
    }

    static constraints = {
        id nullable: false
        retailerId nullable: false
        level nullable: false
        name nullable: false
    }
}