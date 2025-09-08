# Block Villager Trades

Block Villager Trades — a plugin that allows you to block or remove villager trades based on a configurable list of items and enchantments.

## ✨ Features

- Full item blocking - specify item/material name you want to block
- Blocking by enchantments - if an item contains one of the specified enchantments, the trade will be blocked. You can also specify level of blocked enchantment. If not set all levels of  the enchantment will be blocked.
- Two modes of operation
  - Block trade — the trade remains visible but cannot be used/restock.
  - Remove trade — the blocked trade disappears from the villager’s interface. **WARNING: This will permanently delete the trade, and it will not be restored even if you remove the item from the blocked list.**



## ⚙️ Configuration example

```yml
# BlockVillagerTrades Config File

# mode property specifies how to handle items from blocked-items. 
# "block" set maxUses for the trade to 0
# "remove" removes the trade completely. WARNING: This will delete trades permanently 
# and not restore them when the item is removed from the blocked-items list.
mode: block

blocked-items:
  # you can block items by their material name
  - EMERALD
  - ENCHANTED_BOOK:
      # you can also block specific enchantments on items
      enchantments:
        # if no level is specified, all levels of the enchantment are blocked
        - MENDING
        # also you can specify levels
        - UNBREAKING: 3
        - EFFICIENCY: 4
        - EFFICIENCY: 5
  - DIAMOND_PICKAXE:
      enchantments:
        - SILK_TOUCH
        - FORTUNE: 3
  - DIAMOND_AXE
  - DIAMOND_SHOVEL
  - DIAMOND_SWORD
```


## 🎯 Use cases

- Prevent trades with rare or overpowered items
- Restrict trades with enchanted books
- **Balance the server economy without altering vanilla mechanics**

## 🔗 Links
- [Source Code: GitHub Repository](https://github.com/nickxgrom/block-villager-trades)
- [Bug Tracker / Issues: GitHub Issues](https://github.com/nickxgrom/block-villager-trades/issues)
- [Email](mailto:nxgr.dev@gmail.com)

--- 
Feel free to suggest some features or report bugs. My email: [nxgr.dev@gmail.com](mailto:nxgr.dev@gmail.com)
