package dev.thefletchingtable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;

public class FletchingMenu extends AbstractContainerMenu {

    private static final int RESULT_SLOT = 3;
    private static final int INV_START = 4;
    private static final int HOTBAR_START = 31;
    private static final int INV_END = 40;

    private final ContainerLevelAccess access;
    private final Player player;
    private final ResultContainer resultContainer = new ResultContainer();

    // input costs for the current recipe (updated each time the result slot refreshes)
    private int[] inputCosts = {1, 1, 1};

    private final SimpleContainer inputContainer = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            FletchingMenu.this.slotsChanged(this);
        }
    };

    // SERVER //

    public FletchingMenu(int containerId, Inventory playerInv, ContainerLevelAccess access) {
        super(TheFletchingTableMod.FLETCHING_MENU.get(), containerId);
        this.access = access;
        this.player = playerInv.player;

        // input slots, filtered by recipe validity
        for (int i = 0; i < 3; i++) {
            final int slotIndex = i;
            addSlot(new Slot(inputContainer, i, 26 + i * 24, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FletchingMenu.this.isValidForSlot(slotIndex, stack);
                }
            });
        }

        // result slot
        addSlot(new Slot(resultContainer, 0, 134, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack stack) {
                FletchingMenu.this.onResultTaken(player);
            }
        });

        // player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    // CLIENT //

    public FletchingMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, ContainerLevelAccess.NULL);
    }

    // SLOT FILTERING //

    /** Check if an item belongs in the given slot position based on all loaded recipes + tipped arrow logic. */
    private boolean isValidForSlot(int slotIndex, ItemStack stack) {
        // tipped arrow special case
        // slot order: [(empty), arrow, potion]
        if (slotIndex == 1 && stack.is(Items.ARROW)) {
            return true;
        }
        if (slotIndex == 2 && (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION))) {
            return true;
        }

        // check data-driven recipes
        List<RecipeHolder<FletchingRecipe>> recipes = player.level().getRecipeManager()
                .getAllRecipesFor(TheFletchingTableMod.FLETCHING_TYPE.get());

        // fallback: if no recipes loaded yet, allow any item
        if (recipes.isEmpty()) return true;

        for (RecipeHolder<FletchingRecipe> holder : recipes) {
            FletchingRecipe r = holder.value();
            switch (slotIndex) {
                case 0 -> { if (r.getFirst() != null && r.getFirst().test(stack)) return true; }
                case 1 -> { if (r.getSecond().test(stack)) return true; }
                case 2 -> { if (r.getThird().test(stack)) return true; }
            }
        }

        return false;
    }

    // RECIPE MATCHING //

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputContainer) {
            setupResultSlot();
            broadcastChanges();
        }
    }

    private void setupResultSlot() {
        FletchingInput input = new FletchingInput(
                inputContainer.getItem(0),
                inputContainer.getItem(1),
                inputContainer.getItem(2));

        Level level = player.level();

        // data-driven recipes first
        Optional<RecipeHolder<FletchingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(TheFletchingTableMod.FLETCHING_TYPE.get(), input, level);

        if (recipe.isPresent()) {
            FletchingRecipe r = recipe.get().value();
            inputCosts = new int[]{
                    r.getFirst() != null ? r.getFirstCount() : 0,
                    r.getSecondCount(),
                    r.getThirdCount()
            };
            resultContainer.setItem(0, r.assemble(input, level.registryAccess()));
            return;
        }

        // tipped arrow special case
        // (empty) + arrow + potion → tipped arrows
        ItemStack tipped = checkTippedArrowRecipe(input);
        if (!tipped.isEmpty()) {
            inputCosts = new int[]{0, tipped.getCount(), 1};
        }
        resultContainer.setItem(0, tipped);
    }

    /**
     * Arrow + Potion → Tipped Arrows (hardcoded, not data-driven; potion data must transfer to result).
     * Slot layout: [(empty), arrow, potion]. Yields: normal=16, splash=32, lingering=64 per craft.
     */
    private ItemStack checkTippedArrowRecipe(FletchingInput input) {
        if (!input.getItem(0).isEmpty()) return ItemStack.EMPTY;

        ItemStack potionStack = input.getItem(2);

        boolean isPotion = potionStack.is(Items.POTION)
                || potionStack.is(Items.SPLASH_POTION)
                || potionStack.is(Items.LINGERING_POTION);
        if (!isPotion) return ItemStack.EMPTY;

        PotionContents contents = potionStack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return ItemStack.EMPTY;

        ItemStack arrowStack = input.getItem(1);
        if (!arrowStack.is(Items.ARROW)) return ItemStack.EMPTY;

        int count = 16;
        if (potionStack.is(Items.SPLASH_POTION)) count = 32;
        else if (potionStack.is(Items.LINGERING_POTION)) count = 64;

        // require enough arrows to match the output count
        if (arrowStack.getCount() < count) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(Items.TIPPED_ARROW, count);
        result.set(DataComponents.POTION_CONTENTS, contents);
        return result;
    }

    // CRAFTING //

    private void onResultTaken(Player player) {
        for (int i = 0; i < 3; i++) {
            if (inputCosts[i] > 0) {
                inputContainer.removeItem(i, inputCosts[i]);
            }
        }

        access.execute((level, pos) -> {
            level.playSound(null, pos,
                    SoundEvents.VILLAGER_WORK_FLETCHER, SoundSource.BLOCKS, 1.0f, 1.0f);
        });
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, inputContainer));
    }

    // VALIDATION //

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Blocks.FLETCHING_TABLE);
    }

    // SHIFT-CLICK //

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack current = slot.getItem();
            original = current.copy();

            if (index == RESULT_SLOT) {
                // result → player inventory
                if (!moveItemStackTo(current, INV_START, INV_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(current, original);
            } else if (index >= INV_START) {
                // player inventory → matching input slot (moveItemStackTo respects mayPlace)
                if (!moveItemStackTo(current, 0, 3, false)) {
                    // if no valid input slot swap between inventory and hotbar
                    if (index < HOTBAR_START) {
                        if (!moveItemStackTo(current, HOTBAR_START, INV_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!moveItemStackTo(current, INV_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else {
                // input slots → player inventory
                if (!moveItemStackTo(current, INV_START, INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (current.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (current.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, current);
        }

        return original;
    }
}
