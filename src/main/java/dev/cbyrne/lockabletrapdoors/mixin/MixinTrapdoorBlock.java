package dev.cbyrne.lockabletrapdoors.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.cbyrne.betterinject.annotations.Arg;
import dev.cbyrne.betterinject.annotations.Inject;
import dev.cbyrne.lockabletrapdoors.hook.TrapdoorBlockHook;
import dev.cbyrne.lockabletrapdoors.state.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings({"ModifyVariableMayBeArgsOnly", "unused"})
@Mixin(TrapdoorBlock.class)
public abstract class MixinTrapdoorBlock extends Block {
    @Unique
    private final TrapdoorBlockHook lockableTrapdoors$hook = new TrapdoorBlockHook();

    /**
     * Required because we are inheriting {@link Block}
     */
    public MixinTrapdoorBlock(Settings settings) {
        super(settings);
    }

    /**
     * Cycle the {@link Properties#TRAPDOOR_LOCKED} property when crouch-clicking a trap door.
     */
    @ModifyVariable(
        method = "onUse",
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
     * Fixes compatibility with iron trapdoors, because if a {@link ActionResult#PASS} is returned,
     * our new BlockState will not be applied.
     */
    @WrapOperation(
        method = "onUse",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/util/ActionResult;PASS:Lnet/minecraft/util/ActionResult;",
            ordinal = 0
        )
    )
    private ActionResult lockableTrapdoors$overrideMetalActionResult(
        Operation<ActionResult> original,
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hit
    ) {
        var shouldOverride = lockableTrapdoors$hook.overrideIronTrapdoorState(state, world, pos, player);
        return shouldOverride ? ActionResult.success(world.isClient) : original.call();
    }

    /**
     * Prevent the player from opening trapdoors if it has the {@link Properties#TRAPDOOR_LOCKED} property.
     */
    @WrapOperation(
        method = "onUse",
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

    /**
     * Prevent redstone from opening trapdoors if it has the {@link Properties#TRAPDOOR_LOCKED} property.
     * This tricks the method into thinking it's not powered by redstone if it is locked.
     */
    @Inject(
        method = "neighborUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"
        ),
        cancellable = true
    )
    private void lockableTrapdoors$ignoreRedstoneIfLocked(@Arg BlockState state, CallbackInfo ci) {
        if (lockableTrapdoors$hook.isTrapdoorLocked(state)) {
            ci.cancel();
        }
    }

    /**
     * Load the {@link Properties#TRAPDOOR_LOCKED} property when getting the default state in the constructor
     */
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/TrapdoorBlock;setDefaultState(Lnet/minecraft/block/BlockState;)V"
        )
    )
    private void lockableTrapdoors$overrideDefaultStateInConstructor(TrapdoorBlock instance, BlockState original) {
        this.setDefaultState(original.with(Properties.TRAPDOOR_LOCKED, false));
    }

    /**
     * Load the {@link Properties#TRAPDOOR_LOCKED} property when getting the placement state
     */
    @ModifyVariable(method = "getPlacementState", at = @At(value = "STORE", ordinal = 0))
    private BlockState lockableTrapdoors$overrideDefaultPlacementState(BlockState original) {
        return original.with(Properties.TRAPDOOR_LOCKED, false);
    }

    /**
     * Append the {@link Properties#TRAPDOOR_LOCKED} property when appending other properties
     */
    @Redirect(
        method = "appendProperties",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/state/StateManager$Builder;add([Lnet/minecraft/state/property/Property;)Lnet/minecraft/state/StateManager$Builder;"
        )
    )
    private StateManager.Builder<Block, BlockState> lockableTrapdoors$appendProperty(
        StateManager.Builder<Block, BlockState> instance,
        Property<?>[] properties
    ) {
        var propertiesList = Arrays
            .stream(properties)
            .collect(Collectors.toCollection(ArrayList::new));

        propertiesList.add(Properties.TRAPDOOR_LOCKED);
        return instance.add(propertiesList.toArray(Property[]::new));
    }
}
