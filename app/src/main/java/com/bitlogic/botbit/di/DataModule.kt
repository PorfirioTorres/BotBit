package com.bitlogic.botbit.di

import android.content.Context
import com.bitlogic.botbit.data.MissionStore
import com.bitlogic.botbit.data.ProgressStore
import com.bitlogic.botbit.data.repository.SSORepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideProgressStore(@ApplicationContext context: Context): ProgressStore {
        return ProgressStore(context)
    }

    @Provides
    @Singleton
    fun provideMissionStore(@ApplicationContext context: Context): MissionStore {
        return MissionStore(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideSSORepository(firebaseAuth: FirebaseAuth): SSORepositoryImpl {
        return SSORepositoryImpl(firebaseAuth)
    }
}
