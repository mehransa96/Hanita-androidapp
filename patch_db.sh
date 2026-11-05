sed -i 's/version = 4/version = 5/' app/src/main/java/com/example/data/AppDatabase.kt
sed -i '/val MIGRATION_3_4/i \        val MIGRATION_4_5 = object : Migration(4, 5) {\n            override fun migrate(db: SupportSQLiteDatabase) {\n                db.execSQL("ALTER TABLE daily_logs ADD COLUMN waterIntake INTEGER NOT NULL DEFAULT 0")\n            }\n        }\n' app/src/main/java/com/example/data/AppDatabase.kt
sed -i 's/\.addMigrations(MIGRATION_2_3, MIGRATION_3_4)/\.addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)/' app/src/main/java/com/example/data/AppDatabase.kt
