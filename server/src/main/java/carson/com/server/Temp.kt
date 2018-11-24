package carson.com.server


fun main(args: Array<String>) {
    getDatabase().getCollection("users").insertOne(
        createUser("joe","joe@gmail.com","pass".toByteArray()).toDocument())
//    getDatabase().getCollection("etc").deleteOne(Filters.all("_id","snowflake"))
}