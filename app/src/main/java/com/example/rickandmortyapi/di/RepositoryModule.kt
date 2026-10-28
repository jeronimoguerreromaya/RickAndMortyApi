package com.example.rickandmortyapi.di

import com.example.rickandmortyapi.data.repository.CharacterRepositoryImpl
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Vincula la interfaz de dominio con su implementacion en la capa de datos.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository
}
