package dev.iusupov.vkphotos.di

import dagger.Subcomponent
import dev.iusupov.vkphotos.ui.friends.FriendsViewModel
import dev.iusupov.vkphotos.ui.photos.PhotosViewModel

@Subcomponent(modules = [DataModule::class, UtilsModule::class])
interface DataComponent {

    @Subcomponent.Builder
    interface Builder {
        fun dataModule(dataModule: DataModule): Builder
        fun utilsModule(utilsModule: UtilsModule): Builder
        fun build(): DataComponent
    }

    fun inject(friendsViewModel: FriendsViewModel)

    fun inject(photosViewModel: PhotosViewModel)
}