package dev.cbyrne.lockabletrapdoors.mixin.plugin;

import dev.cbyrne.lockabletrapdoors.LockableTrapdoors;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ConfigPlugin implements IMixinConfigPlugin {
    private static final Set<String> CREATE_COMPATIBILITY_MIXINS = Set.of(
        "dev.cbyrne.lockabletrapdoors.mixin.compatibility.create.MixinTrainTrapdoorBlock"
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var isCreateCompatibilityMixin = CREATE_COMPATIBILITY_MIXINS.contains(mixinClassName);
        if (!isCreateCompatibilityMixin) {
            // The code below only relates to create compatibility mixins, we should apply all other mixins.
            return true;
        }

        if (FabricLoader.getInstance().isModLoaded("create")) {
            LockableTrapdoors.LOGGER.info("Applying compatibility mixin for Create...");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
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
