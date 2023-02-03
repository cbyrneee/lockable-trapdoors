package dev.cbyrne.lockabletrapdoors.mixin.compatibility.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cbyrne.lockabletrapdoors.hook.TrapdoorBlockHook;
import dev.cbyrne.lockabletrapdoors.state.Properties;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Adds support for locking trapdoors from the Create mod.
 */
@SuppressWarnings({"UnresolvedMixinReference", "unused"})
@Pseudo
@Mixin(targets = "com.simibubi.create.content.curiosities.deco.TrainTrapdoorBlock")
public class MixinTrainTrapdoorBlock extends TrapdoorBlock {
    @Unique
    private final TrapdoorBlockHook lockableTrapdoors$hook = new TrapdoorBlockHook();

    /**
     * Required for inheriting {@link TrapdoorBlock}, which is required for mappings.
     */
    protected MixinTrainTrapdoorBlock(Settings settings) {
        super(settings);
    }

    /**
     * Cycle the {@link Properties#TRAPDOOR_LOCKED} property when crouch-clicking a trap door.
     */
    @ModifyVariable(
        method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;\n",
        at = @At(
            value = "HEAD"
        ),
        ordinal = 0 // state = state.cycle(...);
    )
    private BlockState lockableTrapdoors$cycleLockedState(
        BlockState state,
        BlockState originalBlockState,
        World world,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hit
    ) {
        return lockableTrapdoors$hook.cycleLockedState(state, player);
    }

    /**
     * Prevent the player from opening trapdoors if it has the {@link Properties#TRAPDOOR_LOCKED} property.
     */
    @WrapOperation(
        method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;\n",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;cycle(Lnet/minecraft/state/property/Property;)Ljava/lang/Object;"
        )
    )
    private Object lockableTrapdoors$onlyOpenIfUnlocked(
        BlockState state,
        Property<?> property,
        Operation<Object> operation,
        // Original method parameters
        BlockState originalBlockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit
    ) {
        // If the trapdoor is locked, or it was just changed from being locked, we don't want to open it.
        if (lockableTrapdoors$hook.shouldBlockTrapdoorInteraction(state)) {
            lockableTrapdoors$hook.trapdoorInteractionBlocked(player);
            return state;
        } else {
            return operation.call(state, property);
        }
    }
}
