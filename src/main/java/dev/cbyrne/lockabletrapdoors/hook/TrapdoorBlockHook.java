package dev.cbyrne.lockabletrapdoors.hook;

import dev.cbyrne.lockabletrapdoors.state.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TrapdoorBlockHook {
    /**
     * The 'unlocked' text with its styling
     */
    private static final Text UNLOCKED_TEXT = Text.literal("unlocked")
        .setStyle(Style.EMPTY
            .withColor(Formatting.GREEN)
            .withBold(true)
        );

    /**
     * The 'locked' text with its styling
     */
    private static final Text LOCKED_TEXT = Text.literal("locked")
        .setStyle(Style.EMPTY
            .withColor(Formatting.RED)
            .withBold(true)
        );

    /**
     * If the locked state has just been cycled by {@link #cycleLockedState(BlockState, PlayerEntity)}.
     */
    private boolean justCycledLockedState = false;

    /**
     * Cycle the {@link Properties#TRAPDOOR_LOCKED} property.
     */
    public BlockState cycleLockedState(BlockState state, PlayerEntity player) {
        if (!player.isSneaking()) {
            return state;
        }

        var wasLocked = this.isTrapdoorLocked(state);
        state = state.cycle(Properties.TRAPDOOR_LOCKED);

        this.justCycledLockedState = true;

        var stateText = Text.literal("This trapdoor is now ").append(!wasLocked ? LOCKED_TEXT : UNLOCKED_TEXT);
        player.sendMessage(stateText, true);

        return state;
    }

    /**
     * If true, the block state has been overriden.
     */
    public boolean overrideIronTrapdoorState(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        // Only modify the return value if we just cycled the locked state.
        if (!justCycledLockedState) {
            return false;
        }

        // Iron trapdoors don't do anything else when right-clicked, so they return early.
        // We need to update the block state manually here, it won't get set otherwise.
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
        trapdoorInteractionBlocked(player);

        // This is required (for some reason) for the block state to be updated.
        return true;
    }

    /**
     * If the trapdoor is allowed to be opened by redstone or the player.
     * NOTE: If the {@link Properties#TRAPDOOR_LOCKED} property has just been cycled, this will be true.
     */
    public boolean shouldBlockTrapdoorInteraction(BlockState state) {
        return this.isTrapdoorLocked(state) || justCycledLockedState;
    }

    /**
     * A convenience method for checking if a {@link BlockState} has the {@link Properties#TRAPDOOR_LOCKED} property.
     * Mostly exists to stop IntelliJ complaining about unboxing everywhere.
     */
    @SuppressWarnings("UnnecessaryUnboxing")
    public boolean isTrapdoorLocked(BlockState state) {
        return state.get(Properties.TRAPDOOR_LOCKED).booleanValue();
    }

    /**
     * Sets {@link #justCycledLockedState} to false. This will allow {@link #shouldBlockTrapdoorInteraction(BlockState)} to
     * return true if the trapdoor isn't locked.
     * <p></p>
     * If the state wasn't just cycled, a message saying 'This trapdoor is locked' will be shown.
     */
    public void trapdoorInteractionBlocked(PlayerEntity player) {
        if (justCycledLockedState) {
            justCycledLockedState = false;
            return;
        }

        var text = Text
            .literal("This trapdoor is ")
            .append(LOCKED_TEXT);

        player.sendMessage(text, true);
    }
}
