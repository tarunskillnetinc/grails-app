package uk.co.wonderlane.wlpos

public class Allergen {

    int id
    String name
    String lookupText

    static mapping = {
        autowire true
        table "allergen"
        version false

        id column: "id"
        name column: "name"
        lookupText column: "lookupText"
    }

    static constraints = {

    }
}
