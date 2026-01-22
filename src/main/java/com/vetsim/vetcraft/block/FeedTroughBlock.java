package com.vetsim.vetcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FeedTroughBlock extends Block {

    // Seviye: 0 (Boş) - 3 (Dolu)
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    // Bloğun Şekli (Yarım blok yüksekliğinde olsun)
    // Bloğun Şekli (Kazan gibi yaklaşık tam blok)
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public FeedTroughBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // SAĞ TIKLAMA İŞLEMİ (DOLDURMA)
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);
        int currentLevel = state.getValue(LEVEL);

        // Eğer Yemlik tam dolu değilse
        if (currentLevel < 3) {

            // Saman Balyası ile tıklandıysa -> TAM DOLDUR
            if (itemstack.is(Items.HAY_BLOCK)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                level.setBlock(pos, state.setValue(LEVEL, 3), 3);
                level.playSound(player, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            // Buğday ile tıklandıysa -> 1 SEVİYE ARTIR
            if (itemstack.is(Items.WHEAT)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                level.setBlock(pos, state.setValue(LEVEL, currentLevel + 1), 3);
                level.playSound(player, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}