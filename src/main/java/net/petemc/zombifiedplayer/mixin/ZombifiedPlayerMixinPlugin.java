package net.petemc.zombifiedplayer.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies the compat mixins only if the target mod is installed,
 * so Mixin does not log "@Mixin target ... was not found" without Corpse or Gravestone.
 */
public class ZombifiedPlayerMixinPlugin implements IMixinConfigPlugin {

    private static final Map<String, String> REQUIRED_MODS = Map.of(
            "net.petemc.zombifiedplayer.mixin.CorpseDeathEventsMixin", "corpse",
            "net.petemc.zombifiedplayer.mixin.GravestoneDeathEventsMixin", "gravestone"
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String modId = REQUIRED_MODS.get(mixinClassName);
        return modId == null || isModLoaded(modId);
    }

    private static boolean isModLoaded(String modId) {
        LoadingModList modList = LoadingModList.get();
        // Unknown state: apply, Mixin only warns if the target is missing
        return modList == null || modList.getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
