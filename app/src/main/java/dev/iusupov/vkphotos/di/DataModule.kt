package dev.iusupov.vkphotos.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.iusupov.vkphotos.repository.Api
import dev.iusupov.vkphotos.repository.ApiImpl
import dev.iusupov.vkphotos.repository.DataSource
import dev.iusupov.vkphotos.repository.Repository
import dev.iusupov.vkphotos.utils.NetworkUtils
import java.util.concurrent.Executor

@Module
object DataModule {

    @Provides
    @Reusable
    @JvmStatic
    fun provideDataSource(api: Api, networkUtils: NetworkUtils, executor: Executor): DataSource {
        return Repository(api, networkUtils, executor)
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideApi(): Api {
        return ApiImpl()
    }
}