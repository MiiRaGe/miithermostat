package miithermostat

import org.ktorm.database.Database
import java.sql.*;

private val DB_HOST = System.getenv("DB_HOST")
private val DB_NAME = System.getenv("DB_NAME")
private val DB_USER = System.getenv("DB_USER")
private val DB_PASSWORD = System.getenv("DB_PASSWORD")
private val DB_CONNECTION_STRING = System.getenv("DB_OVERRIDE")

private var database = Database.connect(
    url = if (DB_CONNECTION_STRING != null) DB_CONNECTION_STRING else "jdbc:postgresql://${DB_HOST}/${DB_NAME}",
    user = DB_USER,
    password = DB_PASSWORD
)

fun getDb(): Database {
    return database;
}

fun setDb(db: Database) {
    database = db;
}
