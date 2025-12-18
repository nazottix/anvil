---
navigation:
  title: Traits
  icon: minecraft:enchanted_book
  parent: index.md
  position: 2
---

# Traits System

Traits are special abilities that materials provide to tools.
Each material has unique traits that affect combat, mining, and utility.

## Trait Categories

### Combat Traits

| Trait | Effect | Max Level |
|-------|--------|-----------|
| Lifesteal | Recover HP from a portion of damage dealt | 3 |
| Holy | Deal extra damage to undead | 3 |
| Fiery | Add fire damage to attacks | 3 |
| Freezing | Apply slowness on hit | 3 |
| Shocking | Chain damage to nearby enemies | 3 |
| Venomous | Apply poison on hit | 3 |
| Jagged | More damage, less speed when damaged | 3 |
| Critical | Increased critical hit chance | 5 |

### Mining Traits

| Trait | Effect | Max Level |
|-------|--------|-----------|
| Auto Smelt | Automatically smelt drops when mining | 1 |
| Momentum | Speed increases with consecutive use | 3 |
| Stonebound | More speed, less damage when damaged | 3 |
| Ethereal | Break transparent blocks faster | 3 |
| Lucky | Increased drop rates | 3 |

### Defense Traits

| Trait | Effect | Max Level |
|-------|--------|-----------|
| Reinforced | Chance to ignore durability consumption | 5 |
| Unbreaking | Reduced durability consumption | 3 |
| Lightweight | Increased attack and movement speed | 3 |
| Thorns | Reflect a portion of damage taken | 3 |

### Utility Traits

| Trait | Effect | Max Level |
|-------|--------|-----------|
| Magnetic | Automatically attract items | 3 |
| Slimy | Chance to spawn slimes | 3 |
| Ecological | Increased XP gain | 3 |
| Writable | Add extra MOD slots | 3 |
| Mending | Repair durability with XP | 3 |
| Glowing | Grant night vision | 1 |
| Silk Touch | Drop blocks as-is | 1 |
| Angler | Increased fishing efficiency | 3 |
| Shearing | Increased shearing efficiency and drops | 3 |
| Self Repair | Slowly repair durability over time | 3 |
| Take Root | Recover 1 durability every [60/level] seconds | 5 |

### Environmental Traits

| Trait | Effect | Max Level |
|-------|--------|-----------|
| Aquadynamic | Increased speed in water | 3 |
| Nether Affinity | Increased stats in the Nether | 3 |
| End Affinity | Increased stats in the End | 3 |

## Trait Slots

Materials provide traits through three slots:

- **Head Traits**: Applied when material is used for the head/blade
- **Handle Traits**: Applied when material is used for the handle
- **Extra Traits**: Always applied regardless of part type

## Wood-Exclusive Trait: Take Root

The **Take Root** trait is exclusive to Wood materials.
This trait passively regenerates 1 durability every 60 seconds (at level 1).

Higher levels reduce the regeneration interval:
| Level | Interval |
|-------|----------|
| 1 | 60 seconds |
| 2 | 30 seconds |
| 3 | 20 seconds |
| 4 | 15 seconds |
| 5 | 12 seconds |

## Related Topics

- [Materials System](materials.md)
- [Part Forge](../stations/part-forge.md)
