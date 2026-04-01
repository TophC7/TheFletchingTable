# The Fletching Table

Give the vanilla fletching table a proper use with a custom crafting system for arrows.

## How it works

- Right-click any fletching table to open the crafting UI
- Place ingredients in the three input slots to craft arrows
- Sneak + right-click bypasses the menu (for placing blocks against it)

## Recipes

| Slot 1 | Slot 2 | Slot 3 | Result |
|--------|--------|--------|--------|
| Feather | Stick | Flint | 6x Arrow |
| - | 6x Arrow | Glowstone Dust | 6x Spectral Arrow |
| - | 16x Arrow | Potion | 16x Tipped Arrow |
| - | 32x Arrow | Splash Potion | 32x Tipped Arrow |
| - | 64x Arrow | Lingering Potion | 64x Tipped Arrow |

Tipped arrows inherit the potion effect from the input potion, including modded potions.

<details>
<summary><b>Adding custom recipes</b></summary>

### Datapacks

Any datapack or mod can add fletching table recipes by placing a JSON file in:

```
data/<your_namespace>/recipe/<recipe_name>.json
```

Recipe format:

```json
{
    "type": "the_fletching_table:fletching",
    "first": {
        "item": "minecraft:feather"
    },
    "second": {
        "item": "minecraft:stick"
    },
    "third": {
        "item": "minecraft:flint"
    },
    "result": {
        "id": "minecraft:arrow",
        "count": 6
    }
}
```

- `first`, `second`, `third` are standard ingredient fields - use `"item"` for a specific item or `"tag"` for a tag
- `first` is optional - omit it for a two-ingredient recipe (the first slot must be empty to match)
- Each ingredient supports an optional `*_count` field (e.g. `"second_count": 6`) - defaults to 1
- `result` uses the standard item format with `id` and `count`

### Tags

The built-in arrow recipe uses common tags for broader mod compatibility:

```json
"first": { "tag": "c:feathers" },
"second": { "tag": "c:rods/wooden" }
```

Any mod that adds items to these tags will automatically work as arrow ingredients.

</details>

## Compatibility

- Supports [EMI](https://modrinth.com/mod/emi) and [JEI](https://modrinth.com/mod/jei) for recipe browsing
- Tipped arrow crafting works with any modded potion effects
- Recipe system is fully extensible via datapacks
- Requires NeoForge 21.1.219+

## Credits

Inspired by [Better Fletching Table](https://modrinth.com/mod/bft), a datapack by [ElGeroIngles](https://modrinth.com/user/ElGeroIngles) that gives the fletching table crafting functionality using invisible minecarts as containers. This mod is a clean reimplementation as a NeoForge mod with a native crafting UI and recipe system.

## License

GPL-3.0-or-later
