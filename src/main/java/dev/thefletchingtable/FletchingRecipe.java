package dev.thefletchingtable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Data-driven recipe for the fletching table.
 * Slot layout: first (slot 0) is optional, second (slot 1) and third (slot 2) are required.
 * Each ingredient supports an optional count (defaults to 1).
 * If `first` is null, slot 0 must be empty to match.
 */
public class FletchingRecipe implements Recipe<FletchingInput> {

    @Nullable private final Ingredient first;
    private final Ingredient second;
    private final Ingredient third;
    private final int firstCount;
    private final int secondCount;
    private final int thirdCount;
    private final ItemStack result;

    public FletchingRecipe(@Nullable Ingredient first, Ingredient second,
                           Ingredient third,
                           int firstCount, int secondCount, int thirdCount,
                           ItemStack result) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.firstCount = firstCount;
        this.secondCount = secondCount;
        this.thirdCount = thirdCount;
        this.result = result;
    }

    @Override
    public boolean matches(FletchingInput input, Level level) {
        // slot 0: optional ingredient
        if (first != null) {
            if (!first.test(input.getItem(0))) return false;
            if (input.getItem(0).getCount() < firstCount) return false;
        } else {
            if (!input.getItem(0).isEmpty()) return false;
        }

        // slots 1 and 2: always required
        if (!second.test(input.getItem(1))) return false;
        if (input.getItem(1).getCount() < secondCount) return false;
        if (!third.test(input.getItem(2))) return false;
        if (input.getItem(2).getCount() < thirdCount) return false;

        return true;
    }

    @Override
    public ItemStack assemble(FletchingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TheFletchingTableMod.FLETCHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TheFletchingTableMod.FLETCHING_TYPE.get();
    }

    // ACCESSORS //

    @Nullable public Ingredient getFirst() { return first; }
    public Ingredient getSecond() { return second; }
    public Ingredient getThird() { return third; }
    public int getFirstCount() { return firstCount; }
    public int getSecondCount() { return secondCount; }
    public int getThirdCount() { return thirdCount; }
    public ItemStack getResult() { return result; }

    // SERIALIZER //

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {

        private static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Ingredient.CODEC_NONEMPTY.optionalFieldOf("first")
                                .forGetter(r -> Optional.ofNullable(r.getFirst())),
                        Ingredient.CODEC_NONEMPTY.fieldOf("second")
                                .forGetter(FletchingRecipe::getSecond),
                        Ingredient.CODEC_NONEMPTY.fieldOf("third")
                                .forGetter(FletchingRecipe::getThird),
                        Codec.INT.optionalFieldOf("first_count", 1)
                                .forGetter(FletchingRecipe::getFirstCount),
                        Codec.INT.optionalFieldOf("second_count", 1)
                                .forGetter(FletchingRecipe::getSecondCount),
                        Codec.INT.optionalFieldOf("third_count", 1)
                                .forGetter(FletchingRecipe::getThirdCount),
                        ItemStack.STRICT_CODEC.fieldOf("result")
                                .forGetter(FletchingRecipe::getResult)
                ).apply(inst, (first, second, third, fc, sc, tc, result) ->
                        new FletchingRecipe(first.orElse(null), second, third,
                                fc, sc, tc, result))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public FletchingRecipe decode(RegistryFriendlyByteBuf buf) {
                        boolean hasFirst = buf.readBoolean();
                        Ingredient first = hasFirst
                                ? Ingredient.CONTENTS_STREAM_CODEC.decode(buf)
                                : null;
                        Ingredient second = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                        Ingredient third = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                        int firstCount = buf.readVarInt();
                        int secondCount = buf.readVarInt();
                        int thirdCount = buf.readVarInt();
                        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                        return new FletchingRecipe(first, second, third,
                                firstCount, secondCount, thirdCount, result);
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buf, FletchingRecipe recipe) {
                        buf.writeBoolean(recipe.first != null);
                        if (recipe.first != null) {
                            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.first);
                        }
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.second);
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.third);
                        buf.writeVarInt(recipe.firstCount);
                        buf.writeVarInt(recipe.secondCount);
                        buf.writeVarInt(recipe.thirdCount);
                        ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                    }
                };

        @Override
        public MapCodec<FletchingRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
