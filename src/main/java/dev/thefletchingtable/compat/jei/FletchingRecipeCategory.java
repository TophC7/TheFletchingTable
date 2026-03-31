package dev.thefletchingtable.compat.jei;

import dev.thefletchingtable.TheFletchingTableMod;
import dev.thefletchingtable.compat.FletchingDisplay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FletchingRecipeCategory implements IRecipeCategory<FletchingDisplay> {

    public static final RecipeType<FletchingDisplay> TYPE = RecipeType.create(
            TheFletchingTableMod.MOD_ID, "fletching", FletchingDisplay.class);

    private static final Component TITLE =
            Component.translatable("container.the_fletching_table.fletching");

    private final IDrawable icon;

    public FletchingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(Items.FLETCHING_TABLE));
    }

    @Override
    public RecipeType<FletchingDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    // LAYOUT //

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FletchingDisplay recipe,
                          IFocusGroup focuses) {
        // slot 0 — first ingredient
        if (!recipe.slot0().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .addItemStacks(recipe.slot0());
        }

        // slot 1 — second ingredient
        if (!recipe.slot1().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 25, 1)
                    .addItemStacks(recipe.slot1());
        }

        // slot 2 — third ingredient
        if (!recipe.slot2().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 49, 1)
                    .addItemStacks(recipe.slot2());
        }

        // result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 1)
                .addItemStack(recipe.result());
    }
}
