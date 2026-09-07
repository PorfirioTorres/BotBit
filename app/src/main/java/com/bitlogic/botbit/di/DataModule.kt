package com.bitlogic.botbit.di

import android.content.Context
import com.bitlogic.botbit.data.MissionStore
import com.bitlogic.botbit.data.ProgressStore
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
}
