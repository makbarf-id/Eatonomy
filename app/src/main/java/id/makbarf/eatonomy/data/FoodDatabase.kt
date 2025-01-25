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
    entities = [
        FoodItem::class,
        HouseholdMember::class,
        Budget::class,
        GroceryPlan::class,
        PlannedGroceryItem::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun householdMemberDao(): HouseholdMemberDao
    abstract fun budgetDao(): BudgetDao
    abstract fun groceryPlanDao(): GroceryPlanDao
    abstract fun plannedGroceryItemDao(): PlannedGroceryItemDao

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        spentAmount REAL NOT NULL DEFAULT 0,
                        remainingAmount REAL NOT NULL,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS grocery_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        budgetId INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        plannedDate INTEGER NOT NULL,
                        completedDate INTEGER,
                        estimatedTotal REAL NOT NULL DEFAULT 0,
                        actualTotal REAL NOT NULL DEFAULT 0,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(budgetId) REFERENCES budgets(id) ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS planned_grocery_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planId INTEGER NOT NULL,
                        foodItemId INTEGER NOT NULL,
                        quantity REAL NOT NULL,
                        unit TEXT NOT NULL,
                        estimatedCost REAL NOT NULL,
                        actualCost REAL,
                        isPurchased INTEGER NOT NULL DEFAULT 0,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(planId) REFERENCES grocery_plans(id) ON DELETE CASCADE,
                        FOREIGN KEY(foodItemId) REFERENCES food_items(id) ON DELETE RESTRICT
                    )
                """)
                
                // Create indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_planned_grocery_items_planId ON planned_grocery_items(planId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_planned_grocery_items_foodItemId ON planned_grocery_items(foodItemId)")
            }
        }
    }
} 