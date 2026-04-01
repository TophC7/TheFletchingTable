package dev.thefletchingtable.compat;

import dev.thefletchingtable.FletchingRecipe;
import dev.thefletchingtable.TheFletchingTableMod;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Shared display wrapper for JEI / EMI integration.
 * Converts both data-driven FletchingRecipes and hardcoded tipped arrow logic
 * into a uniform representation for recipe viewers.
 */
public record FletchingDisplay(
        List<ItemStack> slot0,
        List<ItemStack> slot1,
        List<ItemStack> slot2,
        ItemStack result
) {

    // DATA-DRIVEN //

    public static FletchingDisplay from(FletchingRecipe recipe) {
        return new FletchingDisplay(
                recipe.getFirst() != null
                        ? stacksOf(recipe.getFirst(), recipe.getFirstCount())
                        : List.of(),
                stacksOf(recipe.getSecond(), recipe.getSecondCount()),
                stacksOf(recipe.getThird(), recipe.getThirdCount()),
                recipe.getResult()
        );
    }

    private static List<ItemStack> stacksOf(Ingredient ingredient, int count) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : ingredient.getItems()) {
            ItemStack copy = stack.copy();
            copy.setCount(count);
            stacks.add(copy);
        }
        return stacks;
    }

    // TIPPED ARROWS //

    /**
     * Generate one display per potion per potion type (normal, splash, lingering).
     * Mirrors the hardcoded logic in FletchingMenu.checkTippedArrowRecipe().
     */
    public static List<FletchingDisplay> tippedArrowDisplays() {
        List<FletchingDisplay> displays = new ArrayList<>();

        for (Holder<Potion> potion : BuiltInRegistries.POTION.holders().toList()) {
            PotionContents contents = new PotionContents(Optional.of(potion),
                    Optional.empty(), List.of());

            // normal potion → 16 tipped arrows
            addTippedDisplay(displays, Items.POTION.getDefaultInstance(),
                    contents, 16);
            // splash potion → 32
            addTippedDisplay(displays, Items.SPLASH_POTION.getDefaultInstance(),
                    contents, 32);
            // lingering potion → 64
            addTippedDisplay(displays, Items.LINGERING_POTION.getDefaultInstance(),
                    contents, 64);
        }

        return displays;
    }

    private static void addTippedDisplay(List<FletchingDisplay> list,
                                          ItemStack potionBase, PotionContents contents,
                                          int count) {
        ItemStack potion = potionBase.copy();
        potion.set(DataComponents.POTION_CONTENTS, contents);

        ItemStack result = new ItemStack(Items.TIPPED_ARROW, count);
        result.set(DataComponents.POTION_CONTENTS, contents);

        list.add(new FletchingDisplay(
                List.of(),
                List.of(new ItemStack(Items.ARROW, count)),
                List.of(potion),
                result
        ));
    }

    // COLLECTION //

    /** Gather all displays (data-driven + tipped arrows) from the recipe manager. */
    public static List<FletchingDisplay> allDisplays(RecipeManager manager) {
        List<FletchingDisplay> all = new ArrayList<>();

        for (RecipeHolder<FletchingRecipe> holder :
                manager.getAllRecipesFor(TheFletchingTableMod.FLETCHING_TYPE.get())) {
            all.add(FletchingDisplay.from(holder.value()));
        }

        all.addAll(tippedArrowDisplays());
        return all;
    }
}
