package com.vector.verevcodex.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun tearDown() {
        deleteDatabase(TEST_DB_V1)
        deleteDatabase(TEST_DB_V3)
    }

    @Test
    fun migrateFromVersion1_createsCustomerCredentialsTable() {
        createVersion1Database()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_V1)
            .addMigrations(*AppDatabaseMigrations.ALL)
            .build()

        database.openHelper.writableDatabase

        val hasCredentialsTable = database.openHelper.writableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name='customer_credentials'")
            .use { it.moveToFirst() }

        assertTrue(hasCredentialsTable)
        database.close()
    }

    @Test
    fun migrateFromVersion3_preservesDataAndCreatesSettingsTables() {
        createVersion3Database()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_V3)
            .addMigrations(*AppDatabaseMigrations.ALL)
            .build()

        val writableDatabase = database.openHelper.writableDatabase

        val relationTags = writableDatabase.query(
            "SELECT tags FROM customer_business_relations WHERE id = 'rel-1'"
        ).use {
            assertTrue(it.moveToFirst())
            it.getString(0)
        }
        assertEquals("", relationTags)

        writableDatabase.query(
            "SELECT promotionType, promotionValue, paymentFlowEnabled FROM campaigns WHERE id = 'campaign-1'"
        ).use {
            assertTrue(it.moveToFirst())
            assertEquals("POINTS_MULTIPLIER", it.getString(0))
            assertEquals(2.0, it.getDouble(1), 0.0)
            assertEquals(0, it.getInt(2))
        }

        writableDatabase.query(
            "SELECT couponEnabled, referralEnabled FROM reward_programs WHERE id = 'program-1'"
        ).use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
            assertEquals(0, it.getInt(1))
        }

        val hasBrandingTable = writableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name='branding_settings'")
            .use { it.moveToFirst() }
        val hasBranchConfigTable = writableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name='branch_configurations'")
            .use { it.moveToFirst() }

        assertTrue(hasBrandingTable)
        assertTrue(hasBranchConfigTable)

        database.close()
    }

    private fun createVersion1Database() {
        createDatabase(TEST_DB_V1, 1) { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `owners` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `stores` (`id` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `contactInfo` TEXT NOT NULL, `category` TEXT NOT NULL, `workingHours` TEXT NOT NULL, `logoUrl` TEXT NOT NULL, `primaryColor` TEXT NOT NULL, `secondaryColor` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `locations` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `address` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `staff` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, `permissionsSummary` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `customers` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `email` TEXT NOT NULL, `nfcId` TEXT NOT NULL, `enrolledDate` TEXT NOT NULL, `totalVisits` INTEGER NOT NULL, `totalSpent` REAL NOT NULL, `currentPoints` INTEGER NOT NULL, `loyaltyTier` TEXT NOT NULL, `lastVisit` TEXT, `favoriteStoreId` TEXT, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `customer_business_relations` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `storeId` TEXT NOT NULL, `joinedAt` TEXT NOT NULL, `notes` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `storeId` TEXT NOT NULL, `staffId` TEXT NOT NULL, `amount` REAL NOT NULL, `pointsEarned` INTEGER NOT NULL, `pointsRedeemed` INTEGER NOT NULL, `timestamp` TEXT NOT NULL, `metadata` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transaction_items` (`id` TEXT NOT NULL, `transactionId` TEXT NOT NULL, `name` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `unitPrice` REAL NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `reward_programs` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `type` TEXT NOT NULL, `rulesSummary` TEXT NOT NULL, `active` INTEGER NOT NULL, `earningEnabled` INTEGER NOT NULL, `rewardRedemptionEnabled` INTEGER NOT NULL, `visitCheckInEnabled` INTEGER NOT NULL, `cashbackEnabled` INTEGER NOT NULL, `tierTrackingEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `rewards` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `pointsRequired` INTEGER NOT NULL, `rewardType` TEXT NOT NULL, `expirationDate` TEXT, `usageLimit` INTEGER NOT NULL, `activeStatus` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `points_ledger` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `transactionId` TEXT, `delta` INTEGER NOT NULL, `reason` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `campaigns` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `rewardMultiplier` REAL NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `campaign_targets` (`id` TEXT NOT NULL, `campaignId` TEXT NOT NULL, `segment` TEXT NOT NULL, `description` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `type` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `auth_accounts` (`id` TEXT NOT NULL, `relatedEntityId` TEXT NOT NULL, `fullName` TEXT NOT NULL, `email` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `password` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
        }
    }

    private fun createVersion3Database() {
        createDatabase(TEST_DB_V3, 3) { db ->
            createVersion1DatabaseSchema(db)
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `customer_credentials` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `loyaltyId` TEXT NOT NULL, `method` TEXT NOT NULL, `status` TEXT NOT NULL, `referenceValue` TEXT, `updatedAt` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_customer_credentials_customerId_method` ON `customer_credentials` (`customerId`, `method`)"
            )
            db.execSQL(
                "INSERT INTO customer_business_relations (`id`, `customerId`, `storeId`, `joinedAt`, `notes`) VALUES ('rel-1', 'customer-1', 'store-1', '2026-01-01T09:00:00', 'VIP customer')"
            )
            db.execSQL(
                "INSERT INTO campaigns (`id`, `storeId`, `name`, `description`, `startDate`, `endDate`, `rewardMultiplier`, `active`) VALUES ('campaign-1', 'store-1', 'Weekend Double', 'Double points', '2026-01-01', '2026-01-31', 2.0, 1)"
            )
            db.execSQL(
                "INSERT INTO reward_programs (`id`, `storeId`, `name`, `description`, `type`, `rulesSummary`, `active`, `earningEnabled`, `rewardRedemptionEnabled`, `visitCheckInEnabled`, `cashbackEnabled`, `tierTrackingEnabled`) VALUES ('program-1', 'store-1', 'Base Program', 'Core points', 'POINTS', '10 points per dollar', 1, 1, 1, 0, 0, 0)"
            )
        }
    }

    private fun createVersion1DatabaseSchema(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `owners` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `stores` (`id` TEXT NOT NULL, `ownerId` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `contactInfo` TEXT NOT NULL, `category` TEXT NOT NULL, `workingHours` TEXT NOT NULL, `logoUrl` TEXT NOT NULL, `primaryColor` TEXT NOT NULL, `secondaryColor` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `locations` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `address` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `staff` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, `permissionsSummary` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `customers` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `email` TEXT NOT NULL, `nfcId` TEXT NOT NULL, `enrolledDate` TEXT NOT NULL, `totalVisits` INTEGER NOT NULL, `totalSpent` REAL NOT NULL, `currentPoints` INTEGER NOT NULL, `loyaltyTier` TEXT NOT NULL, `lastVisit` TEXT, `favoriteStoreId` TEXT, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `customer_business_relations` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `storeId` TEXT NOT NULL, `joinedAt` TEXT NOT NULL, `notes` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `storeId` TEXT NOT NULL, `staffId` TEXT NOT NULL, `amount` REAL NOT NULL, `pointsEarned` INTEGER NOT NULL, `pointsRedeemed` INTEGER NOT NULL, `timestamp` TEXT NOT NULL, `metadata` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `transaction_items` (`id` TEXT NOT NULL, `transactionId` TEXT NOT NULL, `name` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `unitPrice` REAL NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `reward_programs` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `type` TEXT NOT NULL, `rulesSummary` TEXT NOT NULL, `active` INTEGER NOT NULL, `earningEnabled` INTEGER NOT NULL, `rewardRedemptionEnabled` INTEGER NOT NULL, `visitCheckInEnabled` INTEGER NOT NULL, `cashbackEnabled` INTEGER NOT NULL, `tierTrackingEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `rewards` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `pointsRequired` INTEGER NOT NULL, `rewardType` TEXT NOT NULL, `expirationDate` TEXT, `usageLimit` INTEGER NOT NULL, `activeStatus` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `points_ledger` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `transactionId` TEXT, `delta` INTEGER NOT NULL, `reason` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `campaigns` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `rewardMultiplier` REAL NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `campaign_targets` (`id` TEXT NOT NULL, `campaignId` TEXT NOT NULL, `segment` TEXT NOT NULL, `description` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `storeId` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `type` TEXT NOT NULL, `createdAt` TEXT NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `auth_accounts` (`id` TEXT NOT NULL, `relatedEntityId` TEXT NOT NULL, `fullName` TEXT NOT NULL, `email` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `password` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
    }

    private fun createDatabase(
        name: String,
        version: Int,
        createSchema: (SQLiteDatabase) -> Unit,
    ) {
        deleteDatabase(name)
        val database = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name), null)
        createSchema(database)
        database.version = version
        database.close()
    }

    private fun deleteDatabase(name: String) {
        context.deleteDatabase(name)
        File(context.getDatabasePath(name).path).delete()
    }

    private companion object {
        const val TEST_DB_V1 = "migration-test-v1.db"
        const val TEST_DB_V3 = "migration-test-v3.db"
    }
}
