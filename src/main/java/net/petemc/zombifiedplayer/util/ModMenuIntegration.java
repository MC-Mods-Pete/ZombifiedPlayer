package net.petemc.zombifiedplayer.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return parent -> AutoConfig.getConfigScreen(ZombifiedPlayerConfig.class, parent).get();
    }
}