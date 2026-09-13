package org.ivanivorontsov.kodekszhizni.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.TimeUnit

class Converters {
    @TypeConverter fun fromMeasureStatus(v: MeasureStatus): String = v.name
    @TypeConverter fun toMeasureStatus(v: String): MeasureStatus =
        runCatching { MeasureStatus.valueOf(v) }.getOrDefault(MeasureStatus.STOPPED)
}

@Database(entities = [Article::class, Measure::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun measureDao(): MeasureDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // articles: explicit lock end timestamp
                db.execSQL(
                    "ALTER TABLE articles ADD COLUMN lockedUntil INTEGER NOT NULL DEFAULT 0"
                )
                val lockMs = TimeUnit.DAYS.toMillis(30)
                db.execSQL(
                    "UPDATE articles SET lockedUntil = createdAt + $lockMs WHERE lockedUntil = 0"
                )
                // Remove example/seed laws; keep user-created
                db.execSQL("DELETE FROM articles WHERE isSeed = 1")

                // measures: bind to a law; type was already TEXT via converter
                db.execSQL(
                    "ALTER TABLE measures ADD COLUMN articleId INTEGER NOT NULL DEFAULT 0"
                )
                // Normalize any legacy ordinal leftovers (unlikely — converter used names)
                // Keep known names as-is; unknown stay as stored strings.
            }
        }

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kodeks_zhizni.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Safety net for installs that already had v2 schema without cleanup
                            db.execSQL("DELETE FROM articles WHERE isSeed = 1")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
