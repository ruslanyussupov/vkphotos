package dev.iusupov.vkphotos.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance fun applicationContext(applicationContext: Context): Builder
        fun build(): ApplicationComponent
    }

    fun dataComponentBuilder(): DataComponent.Builder
}