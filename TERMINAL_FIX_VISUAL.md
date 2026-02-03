# Terminal Trigger System - Before and After Fix

## BEFORE FIX ❌

```
┌─────────────────────────────────────────────────────┐
│  Terminal Initialization (OnInitialize)             │
├─────────────────────────────────────────────────────┤
│  1. createTriggerVolume(2.0f radius) ✓              │
│  2. Set disk objvar                   ✗ MISSING!   │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  Player Approaches Terminal                          │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  OnTriggerVolumeEntered                             │
├─────────────────────────────────────────────────────┤
│  String template = getStringObjVar(self, "disk");   │
│  → Returns NULL (objvar not set)                    │
│                                                      │
│  if (template != null && ...)                       │
│  → FALSE - entire check fails                       │
└─────────────────────────────────────────────────────┘
                    ↓
        ❌ NOTHING HAPPENS ❌
```

**Result**: Proximity trigger completely non-functional. Players cannot receive quest items.

---

## AFTER FIX ✅

```
┌─────────────────────────────────────────────────────┐
│  Terminal Initialization (OnInitialize)             │
├─────────────────────────────────────────────────────┤
│  1. createTriggerVolume(2.0f radius) ✓              │
│  2. Check template name:                            │
│     - terminal_01 → sean_questn_tdisk.iff    ✓     │
│     - terminal_02 → victor_questp_jregistry  ✓     │
│     - terminal_03 → sean_history_disk.iff    ✓     │
│  3. setObjVar(self, "disk", template)        ✓     │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  Player Approaches Terminal 03                       │
│  (Player has "bestine.find" objvar)                 │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  OnTriggerVolumeEntered                             │
├─────────────────────────────────────────────────────┤
│  String template = getStringObjVar(self, "disk");   │
│  → Returns "sean_history_disk.iff" ✓                │
│                                                      │
│  if (template != null &&                            │
│      template.contains("history") &&       ✓        │
│      isPlayer(breacher) &&                 ✓        │
│      canSearch(self, breacher))            ✓        │
│  → TRUE - all checks pass                           │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  canSearch() Method                                  │
├─────────────────────────────────────────────────────┤
│  if (hasObjVar(self, "disk"))              ✓        │
│  {                                                   │
│    Get gating objvar from datatable        ✓        │
│    → "bestine.find"                                 │
│                                                      │
│    if (hasObjVar(player, "bestine.find"))  ✓        │
│    → TRUE - player has required objvar              │
│  }                                                   │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  Item Creation                                       │
├─────────────────────────────────────────────────────┤
│  1. Check if already searched            ✓          │
│  2. Check inventory space                ✓          │
│  3. createObject(template, inventory)    ✓          │
│  4. Send success message                 ✓          │
│  5. Mark player as searched              ✓          │
└─────────────────────────────────────────────────────┘
                    ↓
        ✅ QUEST ITEM RECEIVED ✅
```

**Result**: System works as designed. Players with correct gating objvar receive quest items when approaching terminals.

---

## Code Comparison

### Before (Line 17-21):
```java
public int OnInitialize(obj_id self) throws InterruptedException
{
    createTriggerVolume(GET_QUEST_ITEM_VOLUME_NAME, 2.0f, true);
    return SCRIPT_CONTINUE;
}
```

### After (Line 17-43):
```java
public int OnInitialize(obj_id self) throws InterruptedException
{
    createTriggerVolume(GET_QUEST_ITEM_VOLUME_NAME, 2.0f, true);
    
    // Initialize the disk objvar based on the terminal's shared template
    if (!hasObjVar(self, "disk"))
    {
        String sharedTemplate = getTemplateName(self);
        if (sharedTemplate != null)
        {
            if (sharedTemplate.contains("shared_terminal_bestine_quests_01"))
            {
                setObjVar(self, "disk", "object/tangible/loot/quest/sean_questn_tdisk.iff");
            }
            else if (sharedTemplate.contains("shared_terminal_bestine_quests_02"))
            {
                setObjVar(self, "disk", "object/tangible/loot/quest/victor_questp_jregistry.iff");
            }
            else if (sharedTemplate.contains("shared_terminal_bestine_quests_03"))
            {
                setObjVar(self, "disk", "object/tangible/loot/quest/sean_history_disk.iff");
            }
        }
    }
    
    return SCRIPT_CONTINUE;
}
```

---

## Terminal Configuration Matrix

| Terminal | Template | Disk Item | Gating Objvar | Trigger Type |
|----------|----------|-----------|---------------|--------------|
| terminal_01 | shared_terminal_bestine_quests_01 | sean_questn_tdisk.iff | bestine.negquests | Menu Only |
| terminal_02 | shared_terminal_bestine_quests_02 | victor_questp_jregistry.iff | bestine.camp | Menu Only |
| terminal_03 | shared_terminal_bestine_quests_03 | sean_history_disk.iff | bestine.find | **Proximity + Menu** |

**Note**: Terminal 03 uses proximity because its disk contains "history" (line 49 check).

---

## Key Insight

The bug was an **incomplete implementation**:
- ✅ Trigger volume system coded
- ✅ Datatable with disk templates created  
- ✅ Trigger logic implemented
- ❌ **Initialization code MISSING** ← The Bug

The fix adds the missing initialization code to complete the system.
