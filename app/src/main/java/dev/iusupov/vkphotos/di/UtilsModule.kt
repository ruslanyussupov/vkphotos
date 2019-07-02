package dev.iusupov.vkphotos.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dev.iusupov.vkphotos.utils.StorageUtils
import dev.iusupov.vkphotos.utils.StorageUtilsImpl


@Module
object UtilsModule {

    @Provides
    @Reusable
    @JvmStatic
    fun provideStorageUtils(context: Context): StorageUtils {
        return StorageUtilsImpl(context)
    }
}