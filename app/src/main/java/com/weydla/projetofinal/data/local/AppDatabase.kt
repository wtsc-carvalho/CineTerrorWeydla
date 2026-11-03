package com.weydla.projetofinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.weydla.projetofinal.data.model.FilmeTerror

/**
 * Configuração do Banco de Dados Local Room (Prática 07)
 */
@Database(entities = [FilmeTerror::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun filmeDao(): FilmeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cineterror_weydla_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
