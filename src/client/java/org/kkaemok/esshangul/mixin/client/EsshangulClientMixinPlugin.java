package org.kkaemok.esshangul.mixin.client;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class EsshangulClientMixinPlugin implements IMixinConfigPlugin {
    private static final String ESSENTIAL_INPUT_CLASS = "gg.essential.gui.common.input.AbstractTextInput";
    private static final String ESSENTIAL_INPUT_MIXIN =
            "org.kkaemok.esshangul.mixin.client.EssentialAbstractTextInputMixin";

    private boolean essentialLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        FabricLoader loader = FabricLoader.getInstance();
        this.essentialLoaded = loader.isModLoaded("essential") || loader.isModLoaded("essential-container");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!ESSENTIAL_INPUT_MIXIN.equals(mixinClassName)) {
            return true;
        }
        if (!this.essentialLoaded) {
            return false;
        }
        try {
            Class.forName(ESSENTIAL_INPUT_CLASS, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
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
