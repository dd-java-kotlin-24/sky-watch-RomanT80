package edu.cnm.deepdive.skywatch.app.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.cnm.deepdive.skywatch.app.model.entity.Favorite
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindRepository(repository: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    fun bindsFavRepository(repository: FavoriteRepositoryImpl): FavoriteRepository

}