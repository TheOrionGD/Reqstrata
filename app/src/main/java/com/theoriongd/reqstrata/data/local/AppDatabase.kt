package com.theoriongd.reqstrata.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.theoriongd.reqstrata.data.local.dao.*
import com.theoriongd.reqstrata.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        ProjectMemberEntity::class,
        RequirementEntity::class,
        RequirementVersionEntity::class,
        UseCaseEntity::class,
        ArchitectureComponentEntity::class,
        ArchitectureDecisionEntity::class,
        DatabaseEntityRecord::class,
        ApiEndpointEntity::class,
        TaskEntity::class,
        TestSuiteEntity::class,
        TestCaseEntity::class,
        TestExecutionEntity::class,
        TraceabilityLinkEntity::class,
        DocumentEntity::class,
        NotificationEntity::class,
        ActivityLogEntity::class,
        AiGenerationEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun requirementDao(): RequirementDao
    abstract fun architectureDao(): ArchitectureDao
    abstract fun databaseDesignDao(): DatabaseDesignDao
    abstract fun apiDao(): ApiDao
    abstract fun taskDao(): TaskDao
    abstract fun testDao(): TestDao
    abstract fun traceabilityDao(): TraceabilityDao
    abstract fun documentDao(): DocumentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun activityDao(): ActivityDao
    abstract fun aiGenerationDao(): AiGenerationDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reqstrata.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
