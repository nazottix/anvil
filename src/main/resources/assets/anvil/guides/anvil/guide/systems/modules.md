---
navigation:
  title: Module System
  icon: anvil:core_box_station
  parent: index.md
  position: 3
item_ids:
  - anvil:core_box_station
---

# Module System

The Grid Module System is a customization system with Tetris-like puzzle elements.
Place modules on tools to grant additional effects.

## Core Box

Each tool has a grid space called a **Core Box**.

### Grid Sizes
| Size | Grid | Unlock Condition |
|------|------|------------------|
| Small | 3×3 | Initial |
| Medium | 4×4 | Level 50 |
| Large | 5×5 | Level 200 |
| Extra Large | 6×6 | Level 500 |

## Module Placement

### Placement Rules
1. Modules can be rotated
2. Modules cannot overlap each other
3. Modules cannot extend outside the grid
4. Placed modules can be removed

### Weight System
Each module has a **weight**, and the Core Box has a **maximum weight** limit.

| Core Box Size | Max Weight |
|---------------|------------|
| Small | 10 |
| Medium | 20 |
| Large | 35 |
| Extra Large | 50 |

## Module Types

### Attack Modules
| Module | Shape | Weight | Effect |
|--------|-------|--------|--------|
| Damage Up I | 1×1 | 1 | Attack +2 |
| Damage Up II | 2×1 | 3 | Attack +5 |
| Critical | L-shape | 4 | Critical Rate +5% |

### Mining Modules
| Module | Shape | Weight | Effect |
|--------|-------|--------|--------|
| Speed Up I | 1×1 | 1 | Mining Speed +5% |
| Speed Up II | 2×1 | 3 | Mining Speed +12% |
| Area Expansion | T-shape | 5 | 3×3 Area Mining |

### Durability Modules
| Module | Shape | Weight | Effect |
|--------|-------|--------|--------|
| Sturdy I | 1×1 | 1 | Durability +50 |
| Sturdy II | 2×2 | 4 | Durability +200 |
| Repair | Cross | 6 | Durability regeneration over time |

### Utility Modules
| Module | Shape | Weight | Effect |
|--------|-------|--------|--------|
| Fortune | 2×1 | 3 | Drop Rate +10% |
| Silk Touch | 2×2 | 5 | Silk Touch effect |
| Experience | L-shape | 4 | Experience Gain +20% |

## Obtaining Modules

### Crafting
Craft modules from materials at the MOD Station.

### Drops
Rare modules can be obtained from monsters and treasure chests.

## Core Box Station

Use the Core Box Station to place and remove modules.

### Controls
1. Place tool in slot
2. Drag modules to the grid
3. Right-click to rotate
4. Confirm placement

## Related Topics

- [Skill Tree](skill-tree.md)
- [Tool Station](../stations/tool-station.md)
