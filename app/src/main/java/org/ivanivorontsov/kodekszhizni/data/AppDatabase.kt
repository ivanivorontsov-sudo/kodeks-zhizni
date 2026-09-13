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

@Database(entities = [Article::class, Measure::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun measureDao(): MeasureDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE articles ADD COLUMN lockedUntil INTEGER NOT NULL DEFAULT 0"
                )
                val lockMs = TimeUnit.DAYS.toMillis(30)
                db.execSQL(
                    "UPDATE articles SET lockedUntil = createdAt + $lockMs WHERE lockedUntil = 0"
                )
                db.execSQL("DELETE FROM articles WHERE isSeed = 1")
                db.execSQL(
                    "ALTER TABLE measures ADD COLUMN articleId INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // violationCount for «Нарушение» tracking
                db.execSQL(
                    "ALTER TABLE measures ADD COLUMN violationCount INTEGER NOT NULL DEFAULT 0"
                )
                // Existing ACTIVE rows keep running; no status rewrite needed.
                // ARMED is new enum value stored as TEXT via converter — no schema change for status.
            }
        }

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kodeks_zhizni.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("DELETE FROM articles WHERE isSeed = 1")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
