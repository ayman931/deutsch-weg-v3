package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ReviewLog
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord

@Database(
    entities = [VocabularyWord::class, ReviewState::class, ReviewLog::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun reviewStateDao(): ReviewStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add FSRS-6 columns to review_states table
                db.execSQL("ALTER TABLE review_states ADD COLUMN difficulty REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE review_states ADD COLUMN stability REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE review_states ADD COLUMN lapses INTEGER NOT NULL DEFAULT 0")

                // Create review_logs table for audit and history preservation
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `review_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `reviewStateId` INTEGER NOT NULL,
                        `rating` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `difficulty` REAL NOT NULL,
                        `stability` REAL NOT NULL,
                        `elapsedDays` INTEGER NOT NULL,
                        `scheduledDays` INTEGER NOT NULL,
                        `reviewTimestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`reviewStateId`) REFERENCES `review_states`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_logs_reviewStateId` ON `review_logs` (`reviewStateId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_logs_reviewTimestamp` ON `review_logs` (`reviewTimestamp`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deutsch_weg_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
