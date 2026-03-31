package dev.thefletchingtable;

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
 * Supports 2 or 3 ingredients
 * if `third` is null, the third slot must be empty to match.
 */
public class FletchingRecipe implements Recipe<FletchingInput> {

    private final Ingredient first;
    private final Ingredient second;
    @Nullable private final Ingredient third;
    private final ItemStack result;

    public FletchingRecipe(Ingredient first, Ingredient second,
                           @Nullable Ingredient third, ItemStack result) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.result = result;
    }

    @Override
    public boolean matches(FletchingInput input, Level level) {
        if (!first.test(input.getItem(0))) return false;
        if (!second.test(input.getItem(1))) return false;
        if (third != null) {
            return third.test(input.getItem(2));
        }
        return input.getItem(2).isEmpty();
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

    public Ingredient getFirst() { return first; }
    public Ingredient getSecond() { return second; }
    @Nullable public Ingredient getThird() { return third; }
    public ItemStack getResult() { return result; }

    // SERIALIZER //

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {

        private static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("first")
                                .forGetter(FletchingRecipe::getFirst),
                        Ingredient.CODEC_NONEMPTY.fieldOf("second")
                                .forGetter(FletchingRecipe::getSecond),
                        Ingredient.CODEC_NONEMPTY.optionalFieldOf("third")
                                .forGetter(r -> Optional.ofNullable(r.getThird())),
                        ItemStack.STRICT_CODEC.fieldOf("result")
                                .forGetter(FletchingRecipe::getResult)
                ).apply(inst, (first, second, third, result) ->
                        new FletchingRecipe(first, second, third.orElse(null), result))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public FletchingRecipe decode(RegistryFriendlyByteBuf buf) {
                        Ingredient first = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                        Ingredient second = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                        boolean hasThird = buf.readBoolean();
                        Ingredient third = hasThird
                                ? Ingredient.CONTENTS_STREAM_CODEC.decode(buf)
                                : null;
                        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                        return new FletchingRecipe(first, second, third, result);
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buf, FletchingRecipe recipe) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.first);
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.second);
                        buf.writeBoolean(recipe.third != null);
                        if (recipe.third != null) {
                            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.third);
                        }
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
