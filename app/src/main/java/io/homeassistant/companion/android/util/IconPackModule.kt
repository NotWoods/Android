package io.homeassistant.companion.android.util

import android.content.Context
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.mdi.createMaterialDesignIconPack
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class IconPackModule {
    @Singleton
    @Provides
    fun provideIconPack(
        @ApplicationContext context: Context
    ): IconPack {
        val loader = IconPackLoader(context)
        return createMaterialDesignIconPack(loader).apply {
            loadDrawables(loader.drawableLoader)
        }
    }

}
