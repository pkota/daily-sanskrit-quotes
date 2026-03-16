package com.dailysanskritquotes.di

import android.content.Context
import android.content.SharedPreferences
import com.dailysanskritquotes.data.BundledQuoteImporter
import com.dailysanskritquotes.data.QuoteRepository
import com.dailysanskritquotes.data.db.CustomTagDao
import com.dailysanskritquotes.data.db.QuoteDao
import com.dailysanskritquotes.data.db.QuoteDatabase
import com.dailysanskritquotes.data.db.ShownQuoteDao
import com.dailysanskritquotes.data.sync.QuoteUpdater
import com.dailysanskritquotes.domain.CustomTagManager
import com.dailysanskritquotes.domain.DailyQuoteSelector
import com.dailysanskritquotes.domain.FavoritesManager
import com.dailysanskritquotes.domain.QuoteNotificationManager
import com.dailysanskritquotes.domain.SearchEngine
import com.dailysanskritquotes.domain.ShareHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://raw.githubusercontent.com/example/sanskrit-quotes/main"

    @Provides
    @Singleton
    fun provideQuoteDatabase(@ApplicationContext context: Context): QuoteDatabase {
        return QuoteDatabase.getInstance(context)
    }

    @Provides
    fun provideQuoteDao(database: QuoteDatabase): QuoteDao {
        return database.quoteDao()
    }

    @Provides
    fun provideShownQuoteDao(database: QuoteDatabase): ShownQuoteDao {
        return database.shownQuoteDao()
    }

    @Provides
    fun provideCustomTagDao(database: QuoteDatabase): CustomTagDao {
        return database.customTagDao()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("quote_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideQuoteRepository(
        database: QuoteDatabase,
        prefs: SharedPreferences
    ): QuoteRepository {
        return QuoteRepository(database, prefs)
    }

    @Provides
    @Singleton
    fun provideDailyQuoteSelector(repository: QuoteRepository): DailyQuoteSelector {
        return DailyQuoteSelector(repository)
    }

    @Provides
    @Singleton
    fun provideFavoritesManager(quoteDao: QuoteDao): FavoritesManager {
        return FavoritesManager(quoteDao)
    }

    @Provides
    @Singleton
    fun provideSearchEngine(quoteDao: QuoteDao, customTagDao: CustomTagDao): SearchEngine {
        return SearchEngine(quoteDao, customTagDao)
    }

    @Provides
    @Singleton
    fun provideCustomTagManager(customTagDao: CustomTagDao): CustomTagManager {
        return CustomTagManager(customTagDao)
    }

    @Provides
    @Singleton
    fun provideShareHandler(): ShareHandler {
        return ShareHandler()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Provides
    @Singleton
    fun provideQuoteUpdater(
        repository: QuoteRepository,
        httpClient: HttpClient
    ): QuoteUpdater {
        return QuoteUpdater(repository, httpClient, BASE_URL)
    }

    @Provides
    @Singleton
    fun provideQuoteNotificationManager(
        @ApplicationContext context: Context
    ): QuoteNotificationManager {
        return QuoteNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideBundledQuoteImporter(
        @ApplicationContext context: Context,
        quoteDao: QuoteDao
    ): BundledQuoteImporter {
        return BundledQuoteImporter(context, quoteDao)
    }
}
