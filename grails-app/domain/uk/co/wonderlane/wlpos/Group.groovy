package uk.co.wonderlane.wlpos

class Group {

    int id
    int retailerId
    String name
    GroupLevel level

    static hasMany = [groups: Group, stores: Store, users: User ]
    static belongsTo = [ parentGroup: Group ]

    static mapping = {
        autowire true
        table '`group`'
        version false

        retailerId column: "retailerId", sqlType: "tinyint"
        name column: "`name`"
        level column: "level"
        parentGroup column: "parentId"
        stores joinTable: [name: 'groupstore', key: 'groupId', column: 'storeId']
        users joinTable: [name: 'groupuser', key: 'groupId', column: 'userId']
    }

    static constraints = {
        id nullable: false
        retailerId nullable: false
        name nullable: false
        level nullable: false
        parentGroup nullable: true
    }
}
