package com.vector.verevcodex.di

import android.content.Context
import androidx.room.Room
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.AppDatabaseMigrations
import com.vector.verevcodex.data.db.DatabaseSeeder
import com.vector.verevcodex.data.repository.settings.BusinessSettingsRepositoryImpl
import com.vector.verevcodex.data.repository.analytics.AnalyticsRepositoryImpl
import com.vector.verevcodex.data.repository.auth.AuthRepositoryImpl
import com.vector.verevcodex.data.repository.customer.CustomerRepositoryImpl
import com.vector.verevcodex.data.repository.loyalty.LoyaltyRepositoryImpl
import com.vector.verevcodex.data.repository.reports.ReportRepositoryImpl
import com.vector.verevcodex.data.repository.scan.ScanPreferencesRepositoryImpl
import com.vector.verevcodex.data.repository.staff.StaffRepositoryImpl
import com.vector.verevcodex.data.repository.store.StoreRepositoryImpl
import com.vector.verevcodex.data.repository.transactions.TransactionRepositoryImpl
import com.vector.verevcodex.domain.repository.analytics.AnalyticsRepository
import com.vector.verevcodex.domain.repository.settings.BusinessSettingsRepository
import com.vector.verevcodex.domain.repository.customer.CustomerRepository
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository
import com.vector.verevcodex.domain.repository.reports.ReportRepository
import com.vector.verevcodex.domain.repository.scan.ScanPreferencesRepository
import com.vector.verevcodex.domain.repository.staff.StaffRepository
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import com.vector.verevcodex.domain.usecase.auth.LoginUseCase
import com.vector.verevcodex.domain.usecase.auth.ActivateSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.ChangeCurrentPasswordUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveCurrentSecurityConfigUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveEmailNotificationSettingsUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.LogoutUseCase
import com.vector.verevcodex.domain.usecase.auth.RegisterBusinessUseCase
import com.vector.verevcodex.domain.usecase.auth.ResetPasswordUseCase
import com.vector.verevcodex.domain.usecase.auth.ResetQuickPinUseCase
import com.vector.verevcodex.domain.usecase.auth.SaveSecuritySetupUseCase
import com.vector.verevcodex.domain.usecase.auth.SendPasswordResetCodeUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateCurrentBiometricEnabledUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateCurrentProfileUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateCurrentQuickPinUseCase
import com.vector.verevcodex.domain.usecase.auth.UpdateEmailNotificationSettingsUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyQuickPinUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyPasswordResetCodeUseCase
import com.vector.verevcodex.domain.usecase.settings.AddPaymentMethodUseCase
import com.vector.verevcodex.domain.usecase.staff.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.customer.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.customer.CreateCustomerUseCase
import com.vector.verevcodex.domain.usecase.promotions.CreatePromotionUseCase
import com.vector.verevcodex.domain.usecase.loyalty.CreateProgramUseCase
import com.vector.verevcodex.domain.usecase.reports.ExportReportUseCase
import com.vector.verevcodex.domain.usecase.scan.ClearScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.loyalty.DeleteProgramUseCase
import com.vector.verevcodex.domain.usecase.promotions.DeletePromotionUseCase
import com.vector.verevcodex.domain.usecase.customer.FindCustomerByLoyaltyIdUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveBrandingSettingsUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveAvailableSubscriptionPlansUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveBranchConfigurationUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveActiveScanActionsUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObserveBusinessAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObserveCustomerAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerPointsLedgerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsByStoreUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObserveDashboardUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveInvoicesUseCase
import com.vector.verevcodex.domain.usecase.settings.ObservePaymentMethodsUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObserveProgramAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObservePromotionAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.analytics.ObserveRevenueAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.reports.ObserveAutoReportSettingsUseCase
import com.vector.verevcodex.domain.usecase.scan.ObserveScanPreferencesUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveRewardsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.settings.ObserveSubscriptionPlanUseCase
import com.vector.verevcodex.domain.usecase.settings.UpdateSubscriptionPlanUseCase
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionUseCase
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.customer.QuickRegisterCustomerUseCase
import com.vector.verevcodex.domain.usecase.customer.UpsertCustomerCredentialUseCase
import com.vector.verevcodex.domain.usecase.transactions.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.settings.RemovePaymentMethodUseCase
import com.vector.verevcodex.domain.usecase.reports.SaveAutoReportSettingsUseCase
import com.vector.verevcodex.domain.usecase.settings.SaveBranchConfigurationUseCase
import com.vector.verevcodex.domain.usecase.settings.SaveBrandingSettingsUseCase
import com.vector.verevcodex.domain.usecase.scan.SaveScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.settings.SetDefaultPaymentMethodUseCase
import com.vector.verevcodex.domain.usecase.promotions.SetPromotionEnabledUseCase
import com.vector.verevcodex.domain.usecase.loyalty.SetProgramEnabledUseCase
import com.vector.verevcodex.domain.usecase.store.SelectStoreUseCase
import com.vector.verevcodex.domain.usecase.store.CreateStoreUseCase
import com.vector.verevcodex.domain.usecase.store.UpdateStoreUseCase
import com.vector.verevcodex.domain.usecase.store.SetStoreActiveUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerContactUseCase
import com.vector.verevcodex.domain.usecase.customer.UpdateCustomerNotesAndTagsUseCase
import com.vector.verevcodex.domain.usecase.promotions.UpdatePromotionUseCase
import com.vector.verevcodex.domain.usecase.loyalty.UpdateProgramUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "verev_merchant.db")
            .addMigrations(*AppDatabaseMigrations.ALL)
            .build()

    @Provides
    @Singleton
    fun provideSeeder(database: AppDatabase): DatabaseSeeder = DatabaseSeeder(database)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository
    @Binds abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository
    @Binds abstract fun bindBusinessSettingsRepository(impl: BusinessSettingsRepositoryImpl): BusinessSettingsRepository
    @Binds abstract fun bindScanPreferencesRepository(impl: ScanPreferencesRepositoryImpl): ScanPreferencesRepository
    @Binds abstract fun bindLoyaltyRepository(impl: LoyaltyRepositoryImpl): LoyaltyRepository
    @Binds abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
    @Binds abstract fun bindStaffRepository(impl: StaffRepositoryImpl): StaffRepository
    @Binds abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
    @Binds abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides fun provideObserveSessionUseCase(repository: AuthRepository) = ObserveSessionUseCase(repository)
    @Provides fun provideObserveCurrentSecurityConfigUseCase(repository: AuthRepository) = ObserveCurrentSecurityConfigUseCase(repository)
    @Provides fun provideObserveEmailNotificationSettingsUseCase(repository: AuthRepository) = ObserveEmailNotificationSettingsUseCase(repository)
    @Provides fun provideLoginUseCase(repository: AuthRepository) = LoginUseCase(repository)
    @Provides fun provideRegisterBusinessUseCase(repository: AuthRepository) = RegisterBusinessUseCase(repository)
    @Provides fun provideSaveSecuritySetupUseCase(repository: AuthRepository) = SaveSecuritySetupUseCase(repository)
    @Provides fun provideUpdateCurrentProfileUseCase(repository: AuthRepository) = UpdateCurrentProfileUseCase(repository)
    @Provides fun provideChangeCurrentPasswordUseCase(repository: AuthRepository) = ChangeCurrentPasswordUseCase(repository)
    @Provides fun provideUpdateCurrentQuickPinUseCase(repository: AuthRepository) = UpdateCurrentQuickPinUseCase(repository)
    @Provides fun provideUpdateCurrentBiometricEnabledUseCase(repository: AuthRepository) = UpdateCurrentBiometricEnabledUseCase(repository)
    @Provides fun provideUpdateEmailNotificationSettingsUseCase(repository: AuthRepository) = UpdateEmailNotificationSettingsUseCase(repository)
    @Provides fun provideVerifyQuickPinUseCase(repository: AuthRepository) = VerifyQuickPinUseCase(repository)
    @Provides fun provideSendPasswordResetCodeUseCase(repository: AuthRepository) = SendPasswordResetCodeUseCase(repository)
    @Provides fun provideVerifyPasswordResetCodeUseCase(repository: AuthRepository) = VerifyPasswordResetCodeUseCase(repository)
    @Provides fun provideResetPasswordUseCase(repository: AuthRepository) = ResetPasswordUseCase(repository)
    @Provides fun provideResetQuickPinUseCase(repository: AuthRepository) = ResetQuickPinUseCase(repository)
    @Provides fun provideActivateSessionUseCase(repository: AuthRepository) = ActivateSessionUseCase(repository)
    @Provides fun provideLogoutUseCase(repository: AuthRepository) = LogoutUseCase(repository)
    @Provides fun provideObserveDashboardUseCase(repository: AnalyticsRepository) = ObserveDashboardUseCase(repository)
    @Provides fun provideObserveBusinessAnalyticsUseCase(repository: AnalyticsRepository) = ObserveBusinessAnalyticsUseCase(repository)
    @Provides fun provideObserveCustomerAnalyticsUseCase(repository: AnalyticsRepository) = ObserveCustomerAnalyticsUseCase(repository)
    @Provides fun provideObserveRevenueAnalyticsUseCase(repository: AnalyticsRepository) = ObserveRevenueAnalyticsUseCase(repository)
    @Provides fun provideObservePromotionAnalyticsUseCase(repository: AnalyticsRepository) = ObservePromotionAnalyticsUseCase(repository)
    @Provides fun provideObserveProgramAnalyticsUseCase(repository: AnalyticsRepository) = ObserveProgramAnalyticsUseCase(repository)
    @Provides fun provideObserveStoresUseCase(repository: StoreRepository) = ObserveStoresUseCase(repository)
    @Provides fun provideObserveSelectedStoreUseCase(repository: StoreRepository) = ObserveSelectedStoreUseCase(repository)
    @Provides fun provideSelectStoreUseCase(repository: StoreRepository) = SelectStoreUseCase(repository)
    @Provides fun provideCreateStoreUseCase(repository: StoreRepository) = CreateStoreUseCase(repository)
    @Provides fun provideUpdateStoreUseCase(repository: StoreRepository) = UpdateStoreUseCase(repository)
    @Provides fun provideSetStoreActiveUseCase(repository: StoreRepository) = SetStoreActiveUseCase(repository)
    @Provides fun provideObserveBrandingSettingsUseCase(repository: BusinessSettingsRepository) = ObserveBrandingSettingsUseCase(repository)
    @Provides fun provideSaveBrandingSettingsUseCase(repository: BusinessSettingsRepository) = SaveBrandingSettingsUseCase(repository)
    @Provides fun provideObserveSubscriptionPlanUseCase(repository: BusinessSettingsRepository) = ObserveSubscriptionPlanUseCase(repository)
    @Provides fun provideObserveAvailableSubscriptionPlansUseCase(repository: BusinessSettingsRepository) = ObserveAvailableSubscriptionPlansUseCase(repository)
    @Provides fun provideObservePaymentMethodsUseCase(repository: BusinessSettingsRepository) = ObservePaymentMethodsUseCase(repository)
    @Provides fun provideAddPaymentMethodUseCase(repository: BusinessSettingsRepository) = AddPaymentMethodUseCase(repository)
    @Provides fun provideSetDefaultPaymentMethodUseCase(repository: BusinessSettingsRepository) = SetDefaultPaymentMethodUseCase(repository)
    @Provides fun provideRemovePaymentMethodUseCase(repository: BusinessSettingsRepository) = RemovePaymentMethodUseCase(repository)
    @Provides fun provideObserveInvoicesUseCase(repository: BusinessSettingsRepository) = ObserveInvoicesUseCase(repository)
    @Provides fun provideUpdateSubscriptionPlanUseCase(repository: BusinessSettingsRepository) = UpdateSubscriptionPlanUseCase(repository)
    @Provides fun provideObserveBranchConfigurationUseCase(repository: BusinessSettingsRepository) = ObserveBranchConfigurationUseCase(repository)
    @Provides fun provideSaveBranchConfigurationUseCase(repository: BusinessSettingsRepository) = SaveBranchConfigurationUseCase(repository)
    @Provides fun provideObserveCustomersUseCase(repository: CustomerRepository) = ObserveCustomersUseCase(repository)
    @Provides fun provideObserveCustomerUseCase(repository: CustomerRepository) = ObserveCustomerUseCase(repository)
    @Provides fun provideObserveCustomerRelationsByStoreUseCase(repository: CustomerRepository) = ObserveCustomerRelationsByStoreUseCase(repository)
    @Provides fun provideObserveCustomerRelationsUseCase(repository: CustomerRepository) = ObserveCustomerRelationsUseCase(repository)
    @Provides fun provideObserveCustomerCredentialsUseCase(repository: CustomerRepository) = ObserveCustomerCredentialsUseCase(repository)
    @Provides fun provideObserveCustomerPointsLedgerUseCase(repository: CustomerRepository) = ObserveCustomerPointsLedgerUseCase(repository)
    @Provides fun provideFindCustomerByLoyaltyIdUseCase(repository: CustomerRepository) = FindCustomerByLoyaltyIdUseCase(repository)
    @Provides fun provideCreateCustomerUseCase(repository: CustomerRepository) = CreateCustomerUseCase(repository)
    @Provides fun provideQuickRegisterCustomerUseCase(repository: CustomerRepository) = QuickRegisterCustomerUseCase(repository)
    @Provides fun provideUpdateCustomerContactUseCase(repository: CustomerRepository) = UpdateCustomerContactUseCase(repository)
    @Provides fun provideUpdateCustomerNotesAndTagsUseCase(repository: CustomerRepository) = UpdateCustomerNotesAndTagsUseCase(repository)
    @Provides fun provideUpsertCustomerCredentialUseCase(repository: CustomerRepository) = UpsertCustomerCredentialUseCase(repository)
    @Provides fun provideAdjustCustomerPointsUseCase(repository: CustomerRepository) = AdjustCustomerPointsUseCase(repository)
    @Provides fun provideObserveScanPreferencesUseCase(repository: ScanPreferencesRepository) = ObserveScanPreferencesUseCase(repository)
    @Provides fun provideSaveScanPreferenceUseCase(repository: ScanPreferencesRepository) = SaveScanPreferenceUseCase(repository)
    @Provides fun provideClearScanPreferenceUseCase(repository: ScanPreferencesRepository) = ClearScanPreferenceUseCase(repository)
    @Provides fun provideObserveProgramsUseCase(repository: LoyaltyRepository) = ObserveProgramsUseCase(repository)
    @Provides fun provideObserveRewardsUseCase(repository: LoyaltyRepository) = ObserveRewardsUseCase(repository)
    @Provides fun provideObserveCampaignsUseCase(repository: LoyaltyRepository) = ObserveCampaignsUseCase(repository)
    @Provides fun provideObserveActiveScanActionsUseCase(repository: LoyaltyRepository) = ObserveActiveScanActionsUseCase(repository)
    @Provides fun provideCreatePromotionUseCase(repository: LoyaltyRepository) = CreatePromotionUseCase(repository)
    @Provides fun provideUpdatePromotionUseCase(repository: LoyaltyRepository) = UpdatePromotionUseCase(repository)
    @Provides fun provideSetPromotionEnabledUseCase(repository: LoyaltyRepository) = SetPromotionEnabledUseCase(repository)
    @Provides fun provideDeletePromotionUseCase(repository: LoyaltyRepository) = DeletePromotionUseCase(repository)
    @Provides fun provideCreateProgramUseCase(repository: LoyaltyRepository) = CreateProgramUseCase(repository)
    @Provides fun provideUpdateProgramUseCase(repository: LoyaltyRepository) = UpdateProgramUseCase(repository)
    @Provides fun provideSetProgramEnabledUseCase(repository: LoyaltyRepository) = SetProgramEnabledUseCase(repository)
    @Provides fun provideDeleteProgramUseCase(repository: LoyaltyRepository) = DeleteProgramUseCase(repository)
    @Provides fun provideObserveStaffUseCase(repository: StaffRepository) = ObserveStaffUseCase(repository)
    @Provides fun provideAddStaffMembersUseCase(repository: StaffRepository) = AddStaffMembersUseCase(repository)
    @Provides fun provideObserveStaffAnalyticsUseCase(repository: StaffRepository) = ObserveStaffAnalyticsUseCase(repository)
    @Provides fun provideObserveTransactionsUseCase(repository: TransactionRepository) = ObserveTransactionsUseCase(repository)
    @Provides fun provideObserveTransactionUseCase(repository: TransactionRepository) = ObserveTransactionUseCase(repository)
    @Provides fun provideRecordTransactionUseCase(repository: TransactionRepository) = RecordTransactionUseCase(repository)
    @Provides fun provideObserveAutoReportSettingsUseCase(repository: ReportRepository) = ObserveAutoReportSettingsUseCase(repository)
    @Provides fun provideSaveAutoReportSettingsUseCase(repository: ReportRepository) = SaveAutoReportSettingsUseCase(repository)
    @Provides fun provideExportReportUseCase(repository: ReportRepository) = ExportReportUseCase(repository)
}
