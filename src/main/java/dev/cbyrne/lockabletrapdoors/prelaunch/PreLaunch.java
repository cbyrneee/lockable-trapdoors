package dev.cbyrne.lockabletrapdoors.prelaunch;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import dev.cbyrne.betterinject.BetterInject;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
        BetterInject.initialize();
    }
}
