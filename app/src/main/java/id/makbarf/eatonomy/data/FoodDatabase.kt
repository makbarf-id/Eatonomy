package id.makbarf.eatonomy.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodItem::class, HouseholdMember::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun householdMemberDao(): HouseholdMemberDao

    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "food_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS household_members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        dailyCalorieGoal INTEGER NOT NULL,
                        proteinGoal REAL NOT NULL,
                        carbsGoal REAL NOT NULL,
                        fatsGoal REAL NOT NULL,
                        notes TEXT
                    )
                """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // If you need to make any changes to existing tables
                // Add migration SQL here
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns with default values
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN height REAL NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN weight REAL NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN age INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN isMale INTEGER NOT NULL DEFAULT 1
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN activityLevel TEXT NOT NULL DEFAULT 'MODERATELY_ACTIVE'
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN weightGoalType TEXT NOT NULL DEFAULT 'MAINTAIN'
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN targetWeight REAL
                """)
                database.execSQL("""
                    ALTER TABLE household_members 
                    ADD COLUMN targetDate INTEGER
                """)
            }
        }
    }
} 