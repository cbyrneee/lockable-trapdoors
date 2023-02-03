package dev.cbyrne.lockabletrapdoors;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockableTrapdoors implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("lockable-trapdoors");

    @Override
    public void onInitialize() {
        LOGGER.info("*cough* *cough* Is this thing on? (Lockable Trapdoors is ready!)");
    }
}