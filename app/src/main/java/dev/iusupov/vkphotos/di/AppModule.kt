package dev.iusupov.vkphotos.di

import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideExecutor(): Executor {
        return Executors.newFixedThreadPool(5)
    }
}