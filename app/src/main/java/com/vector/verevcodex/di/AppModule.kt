package com.vector.verevcodex.di

import android.content.Context
import androidx.room.Room
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.repository.AnalyticsRepositoryImpl
import com.vector.verevcodex.data.repository.CustomerRepositoryImpl
import com.vector.verevcodex.data.repository.DatabaseSeeder
import com.vector.verevcodex.data.repository.LoyaltyRepositoryImpl
import com.vector.verevcodex.data.repository.ReportRepositoryImpl
import com.vector.verevcodex.data.repository.StaffRepositoryImpl
import com.vector.verevcodex.data.repository.StoreRepositoryImpl
import com.vector.verevcodex.data.repository.TransactionRepositoryImpl
import com.vector.verevcodex.data.repository.auth.AuthRepositoryImpl
import com.vector.verevcodex.data.repository.scan.ScanPreferencesRepositoryImpl
import com.vector.verevcodex.domain.repository.AnalyticsRepository
import com.vector.verevcodex.domain.repository.CustomerRepository
import com.vector.verevcodex.domain.repository.LoyaltyRepository
import com.vector.verevcodex.domain.repository.ReportRepository
import com.vector.verevcodex.domain.repository.ScanPreferencesRepository
import com.vector.verevcodex.domain.repository.StaffRepository
import com.vector.verevcodex.domain.repository.StoreRepository
import com.vector.verevcodex.domain.repository.TransactionRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import com.vector.verevcodex.domain.usecase.auth.LoginUseCase
import com.vector.verevcodex.domain.usecase.auth.ActivateSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveCurrentSecurityConfigUseCase
import com.vector.verevcodex.domain.usecase.auth.ObserveSessionUseCase
import com.vector.verevcodex.domain.usecase.auth.LogoutUseCase
import com.vector.verevcodex.domain.usecase.auth.RegisterBusinessUseCase
import com.vector.verevcodex.domain.usecase.auth.ResetPasswordUseCase
import com.vector.verevcodex.domain.usecase.auth.ResetQuickPinUseCase
import com.vector.verevcodex.domain.usecase.auth.SaveSecuritySetupUseCase
import com.vector.verevcodex.domain.usecase.auth.SendPasswordResetCodeUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyQuickPinUseCase
import com.vector.verevcodex.domain.usecase.auth.VerifyPasswordResetCodeUseCase
import com.vector.verevcodex.domain.usecase.AddStaffMembersUseCase
import com.vector.verevcodex.domain.usecase.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.CreateCustomerUseCase
import com.vector.verevcodex.domain.usecase.CreateProgramUseCase
import com.vector.verevcodex.domain.usecase.ExportReportUseCase
import com.vector.verevcodex.domain.usecase.ClearScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.DeleteProgramUseCase
import com.vector.verevcodex.domain.usecase.FindCustomerByLoyaltyIdUseCase
import com.vector.verevcodex.domain.usecase.ObserveCampaignsUseCase
import com.vector.verevcodex.domain.usecase.ObserveActiveScanActionsUseCase
import com.vector.verevcodex.domain.usecase.ObserveCustomerCredentialsUseCase
import com.vector.verevcodex.domain.usecase.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.ObserveDashboardUseCase
import com.vector.verevcodex.domain.usecase.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.ObserveScanPreferencesUseCase
import com.vector.verevcodex.domain.usecase.ObserveRewardsUseCase
import com.vector.verevcodex.domain.usecase.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.ObserveStaffAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.ObserveStaffUseCase
import com.vector.verevcodex.domain.usecase.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.QuickRegisterCustomerUseCase
import com.vector.verevcodex.domain.usecase.UpsertCustomerCredentialUseCase
import com.vector.verevcodex.domain.usecase.RecordTransactionUseCase
import com.vector.verevcodex.domain.usecase.SaveScanPreferenceUseCase
import com.vector.verevcodex.domain.usecase.SetProgramEnabledUseCase
import com.vector.verevcodex.domain.usecase.SelectStoreUseCase
import com.vector.verevcodex.domain.usecase.UpdateProgramUseCase
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
        Room.databaseBuilder(context, AppDatabase::class.java, "verev_merchant.db").fallbackToDestructiveMigration().build()

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
    @Provides fun provideLoginUseCase(repository: AuthRepository) = LoginUseCase(repository)
    @Provides fun provideRegisterBusinessUseCase(repository: AuthRepository) = RegisterBusinessUseCase(repository)
    @Provides fun provideSaveSecuritySetupUseCase(repository: AuthRepository) = SaveSecuritySetupUseCase(repository)
    @Provides fun provideVerifyQuickPinUseCase(repository: AuthRepository) = VerifyQuickPinUseCase(repository)
    @Provides fun provideSendPasswordResetCodeUseCase(repository: AuthRepository) = SendPasswordResetCodeUseCase(repository)
    @Provides fun provideVerifyPasswordResetCodeUseCase(repository: AuthRepository) = VerifyPasswordResetCodeUseCase(repository)
    @Provides fun provideResetPasswordUseCase(repository: AuthRepository) = ResetPasswordUseCase(repository)
    @Provides fun provideResetQuickPinUseCase(repository: AuthRepository) = ResetQuickPinUseCase(repository)
    @Provides fun provideActivateSessionUseCase(repository: AuthRepository) = ActivateSessionUseCase(repository)
    @Provides fun provideLogoutUseCase(repository: AuthRepository) = LogoutUseCase(repository)
    @Provides fun provideObserveDashboardUseCase(repository: AnalyticsRepository) = ObserveDashboardUseCase(repository)
    @Provides fun provideObserveStoresUseCase(repository: StoreRepository) = ObserveStoresUseCase(repository)
    @Provides fun provideObserveSelectedStoreUseCase(repository: StoreRepository) = ObserveSelectedStoreUseCase(repository)
    @Provides fun provideSelectStoreUseCase(repository: StoreRepository) = SelectStoreUseCase(repository)
    @Provides fun provideObserveCustomersUseCase(repository: CustomerRepository) = ObserveCustomersUseCase(repository)
    @Provides fun provideObserveCustomerUseCase(repository: CustomerRepository) = ObserveCustomerUseCase(repository)
    @Provides fun provideObserveCustomerCredentialsUseCase(repository: CustomerRepository) = ObserveCustomerCredentialsUseCase(repository)
    @Provides fun provideFindCustomerByLoyaltyIdUseCase(repository: CustomerRepository) = FindCustomerByLoyaltyIdUseCase(repository)
    @Provides fun provideCreateCustomerUseCase(repository: CustomerRepository) = CreateCustomerUseCase(repository)
    @Provides fun provideQuickRegisterCustomerUseCase(repository: CustomerRepository) = QuickRegisterCustomerUseCase(repository)
    @Provides fun provideUpsertCustomerCredentialUseCase(repository: CustomerRepository) = UpsertCustomerCredentialUseCase(repository)
    @Provides fun provideAdjustCustomerPointsUseCase(repository: CustomerRepository) = AdjustCustomerPointsUseCase(repository)
    @Provides fun provideObserveScanPreferencesUseCase(repository: ScanPreferencesRepository) = ObserveScanPreferencesUseCase(repository)
    @Provides fun provideSaveScanPreferenceUseCase(repository: ScanPreferencesRepository) = SaveScanPreferenceUseCase(repository)
    @Provides fun provideClearScanPreferenceUseCase(repository: ScanPreferencesRepository) = ClearScanPreferenceUseCase(repository)
    @Provides fun provideObserveProgramsUseCase(repository: LoyaltyRepository) = ObserveProgramsUseCase(repository)
    @Provides fun provideObserveRewardsUseCase(repository: LoyaltyRepository) = ObserveRewardsUseCase(repository)
    @Provides fun provideObserveCampaignsUseCase(repository: LoyaltyRepository) = ObserveCampaignsUseCase(repository)
    @Provides fun provideObserveActiveScanActionsUseCase(repository: LoyaltyRepository) = ObserveActiveScanActionsUseCase(repository)
    @Provides fun provideCreateProgramUseCase(repository: LoyaltyRepository) = CreateProgramUseCase(repository)
    @Provides fun provideUpdateProgramUseCase(repository: LoyaltyRepository) = UpdateProgramUseCase(repository)
    @Provides fun provideSetProgramEnabledUseCase(repository: LoyaltyRepository) = SetProgramEnabledUseCase(repository)
    @Provides fun provideDeleteProgramUseCase(repository: LoyaltyRepository) = DeleteProgramUseCase(repository)
    @Provides fun provideObserveStaffUseCase(repository: StaffRepository) = ObserveStaffUseCase(repository)
    @Provides fun provideAddStaffMembersUseCase(repository: StaffRepository) = AddStaffMembersUseCase(repository)
    @Provides fun provideObserveStaffAnalyticsUseCase(repository: StaffRepository) = ObserveStaffAnalyticsUseCase(repository)
    @Provides fun provideObserveTransactionsUseCase(repository: TransactionRepository) = ObserveTransactionsUseCase(repository)
    @Provides fun provideRecordTransactionUseCase(repository: TransactionRepository) = RecordTransactionUseCase(repository)
    @Provides fun provideExportReportUseCase(repository: ReportRepository) = ExportReportUseCase(repository)
}
