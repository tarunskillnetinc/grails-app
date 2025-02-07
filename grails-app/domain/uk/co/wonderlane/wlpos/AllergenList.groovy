package uk.co.wonderlane.wlpos

public class AllergenList {

    int id;
    String name;

    static mapping = {
        autowire true
        table "allergenlist"
        version false

        id column:"id"
        name column: "name"
    }

    static constraints = {

    }
}
