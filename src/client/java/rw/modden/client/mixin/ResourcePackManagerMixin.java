package rw.modden.client.mixin;

import net.minecraft.client.resource.DefaultClientResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rw.modden.client.resource.ResourcePackProvider;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin {
    @Shadow @Final @Mutable
    private Set<net.minecraft.resource.ResourcePackProvider> providers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void runemodels$addProvider(net.minecraft.resource.ResourcePackProvider[] initial, CallbackInfo ci) {
        boolean clientManager = this.providers.stream()
                .anyMatch(p -> p instanceof DefaultClientResourcePackProvider);
        if (!clientManager) return;

        Set<net.minecraft.resource.ResourcePackProvider> copy = new LinkedHashSet<>(this.providers);
        copy.add(new ResourcePackProvider());
        this.providers = copy;
    }
}