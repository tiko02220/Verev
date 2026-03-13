package com.vector.verevcodex.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE `auth_accounts` ADD COLUMN `profilePhotoUri` TEXT NOT NULL DEFAULT ''
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE `transactions` ADD COLUMN `countsAsVisit` INTEGER NOT NULL DEFAULT 1
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `customer_bonus_actions` (
                    `id` TEXT NOT NULL,
                    `customerId` TEXT NOT NULL,
                    `storeId` TEXT,
                    `type` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `details` TEXT NOT NULL,
                    `createdAt` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE INDEX IF NOT EXISTS `index_customer_bonus_actions_customerId`
                ON `customer_bonus_actions` (`customerId`)
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            recreateSettingsTables(database)
        }
    }

    val MIGRATION_1_3 = object : Migration(1, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `customer_credentials` (
                    `id` TEXT NOT NULL,
                    `customerId` TEXT NOT NULL,
                    `loyaltyId` TEXT NOT NULL,
                    `method` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `referenceValue` TEXT,
                    `updatedAt` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS `index_customer_credentials_customerId_method`
                ON `customer_credentials` (`customerId`, `method`)
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_3_7 = object : Migration(3, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `customer_business_relations` ADD COLUMN `tags` TEXT NOT NULL DEFAULT ''")

            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `couponEnabled` INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `purchaseFrequencyEnabled` INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `referralEnabled` INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `pointsSpendStepAmount` INTEGER NOT NULL DEFAULT 1
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `pointsAwardedPerStep` INTEGER NOT NULL DEFAULT 10
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `pointsWelcomeBonus` INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `pointsMinimumRedeem` INTEGER NOT NULL DEFAULT 50
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `cashbackPercent` REAL NOT NULL DEFAULT 5.0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `cashbackMinimumSpendAmount` REAL NOT NULL DEFAULT 10.0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `tierSilverThreshold` INTEGER NOT NULL DEFAULT 300
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `tierGoldThreshold` INTEGER NOT NULL DEFAULT 700
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `tierVipThreshold` INTEGER NOT NULL DEFAULT 1200
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `tierBonusPercent` INTEGER NOT NULL DEFAULT 15
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `couponName` TEXT NOT NULL DEFAULT ''
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `couponPointsCost` INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `couponDiscountAmount` REAL NOT NULL DEFAULT 0.0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `couponMinimumSpendAmount` REAL NOT NULL DEFAULT 0.0
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `checkInVisitsRequired` INTEGER NOT NULL DEFAULT 5
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `checkInRewardPoints` INTEGER NOT NULL DEFAULT 25
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `checkInRewardName` TEXT NOT NULL DEFAULT ''
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `purchaseFrequencyCount` INTEGER NOT NULL DEFAULT 3
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `purchaseFrequencyWindowDays` INTEGER NOT NULL DEFAULT 30
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `purchaseFrequencyRewardPoints` INTEGER NOT NULL DEFAULT 50
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `purchaseFrequencyRewardName` TEXT NOT NULL DEFAULT ''
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `referralReferrerRewardPoints` INTEGER NOT NULL DEFAULT 100
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `referralRefereeRewardPoints` INTEGER NOT NULL DEFAULT 50
                """.trimIndent(),
            )
            database.execSQL(
                """
                ALTER TABLE `reward_programs` ADD COLUMN `referralCodePrefix` TEXT NOT NULL DEFAULT 'REF'
                """.trimIndent(),
            )

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `campaigns_new` (
                    `id` TEXT NOT NULL,
                    `storeId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `startDate` TEXT NOT NULL,
                    `endDate` TEXT NOT NULL,
                    `promotionType` TEXT NOT NULL,
                    `promotionValue` REAL NOT NULL,
                    `promoCode` TEXT,
                    `paymentFlowEnabled` INTEGER NOT NULL,
                    `active` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `campaigns_new`
                (`id`, `storeId`, `name`, `description`, `startDate`, `endDate`, `promotionType`, `promotionValue`, `promoCode`, `paymentFlowEnabled`, `active`)
                SELECT `id`, `storeId`, `name`, `description`, `startDate`, `endDate`, 'POINTS_MULTIPLIER', `rewardMultiplier`, NULL, 0, `active`
                FROM `campaigns`
                """.trimIndent(),
            )
            database.execSQL("DROP TABLE `campaigns`")
            database.execSQL("ALTER TABLE `campaigns_new` RENAME TO `campaigns`")

            recreateSettingsTables(database)
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_3,
        MIGRATION_3_7,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
    )
}

private fun recreateSettingsTables(database: SupportSQLiteDatabase) {
    database.execSQL("DROP TABLE IF EXISTS `branding_settings`")
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `branding_settings` (
            `storeId` TEXT NOT NULL,
            `selectedPaletteId` TEXT NOT NULL,
            `themeMode` TEXT NOT NULL,
            `accentColor` TEXT NOT NULL,
            PRIMARY KEY(`storeId`)
        )
        """.trimIndent(),
    )

    database.execSQL("DROP TABLE IF EXISTS `subscription_plans`")
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `subscription_plans` (
            `id` TEXT NOT NULL,
            `ownerId` TEXT NOT NULL,
            `name` TEXT NOT NULL,
            `monthlyPrice` REAL NOT NULL,
            `currencyCode` TEXT NOT NULL,
            `renewalDate` TEXT NOT NULL,
            `active` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
    )

    database.execSQL("DROP TABLE IF EXISTS `payment_methods`")
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `payment_methods` (
            `id` TEXT NOT NULL,
            `ownerId` TEXT NOT NULL,
            `brand` TEXT NOT NULL,
            `last4` TEXT NOT NULL,
            `expiryMonth` INTEGER NOT NULL,
            `expiryYear` INTEGER NOT NULL,
            `isDefault` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
    )

    database.execSQL("DROP TABLE IF EXISTS `billing_invoices`")
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `billing_invoices` (
            `id` TEXT NOT NULL,
            `ownerId` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `periodLabel` TEXT NOT NULL,
            `amount` REAL NOT NULL,
            `currencyCode` TEXT NOT NULL,
            `status` TEXT NOT NULL,
            `issuedDate` TEXT NOT NULL,
            PRIMARY KEY(`id`)
        )
        """.trimIndent(),
    )

    database.execSQL("DROP TABLE IF EXISTS `branch_configurations`")
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `branch_configurations` (
            `storeId` TEXT NOT NULL,
            `customerSelfEnrollmentEnabled` INTEGER NOT NULL,
            `nfcCardProvisioningEnabled` INTEGER NOT NULL,
            `barcodeScannerEnabled` INTEGER NOT NULL,
            `managerApprovalRequiredForRedemption` INTEGER NOT NULL,
            `managerApprovalRequiredForPointsAdjustment` INTEGER NOT NULL,
            `receiptFooter` TEXT NOT NULL,
            PRIMARY KEY(`storeId`)
        )
        """.trimIndent(),
    )
}
