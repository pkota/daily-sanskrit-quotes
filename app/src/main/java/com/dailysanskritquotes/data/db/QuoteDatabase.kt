package com.dailysanskritquotes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [QuoteEntity::class, ShownQuoteEntity::class, CustomTagEntity::class],
    version = 3,
    exportSchema = false
)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun shownQuoteDao(): ShownQuoteDao
    abstract fun customTagDao(): CustomTagDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quotes ADD COLUMN transliteration TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE quotes ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_tags (
                        tagName TEXT NOT NULL,
                        quoteId TEXT NOT NULL,
                        PRIMARY KEY(tagName, quoteId),
                        FOREIGN KEY(quoteId) REFERENCES quotes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_custom_tags_quoteId ON custom_tags(quoteId)")
            }
        }

        fun getInstance(context: Context): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quotes.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
