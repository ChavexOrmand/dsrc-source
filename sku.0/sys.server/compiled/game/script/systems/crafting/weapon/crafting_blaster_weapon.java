package script.systems.crafting.weapon;

import script.*;
import script.library.craftinglib;
import script.library.weapons;

public class crafting_blaster_weapon extends script.systems.crafting.weapon.crafting_base_weapon
{
    public crafting_blaster_weapon()
    {
    }
    public static final String VERSION = "v0.00.00";
    public static final String[] REQUIRED_SKILLS = 
    {
        "crafting_weaponsmith_novice"
    };
    public static final String[] ASSEMBLY_SKILL_MODS = 
    {
        "weapon_assembly"
    };
    public static final String[] EXPERIMENT_SKILL_MODS = 
    {
        "weapon_experimentation"
    };
    
    // Refactored to use craftinglib utility method - reduces code from ~50 lines to ~5 lines
    public static final resource_weight[] OBJ_ASSEMBLY_ATTRIBUTE_RESOURCES = 
        craftinglib.createResourceWeights(
            craftinglib.COMMON_WEAPON_ATTRIBUTES,
            craftinglib.RESOURCE_CONDUCTIVITY,
            1,
            craftinglib.RESOURCE_QUALITY,
            1
        );
    
    // Simplified: Both arrays use the same configuration
    public static final resource_weight[] OBJ_MAX_ATTRIBUTE_RESOURCES = OBJ_ASSEMBLY_ATTRIBUTE_RESOURCES;
    
    public String[] getRequiredSkills() throws InterruptedException
    {
        return REQUIRED_SKILLS;
    }
    public String[] getAssemblySkillMods() throws InterruptedException
    {
        return ASSEMBLY_SKILL_MODS;
    }
    public String[] getExperimentSkillMods() throws InterruptedException
    {
        return EXPERIMENT_SKILL_MODS;
    }
    public resource_weight[] getResourceMaxResourceWeights() throws InterruptedException
    {
        return OBJ_MAX_ATTRIBUTE_RESOURCES;
    }
    public resource_weight[] getAssemblyResourceWeights() throws InterruptedException
    {
        return OBJ_ASSEMBLY_ATTRIBUTE_RESOURCES;
    }
    public int OnInitialize(obj_id self) throws InterruptedException
    {
        if (!weapons.isVersionCurrent(self))
        {
            messageTo(self, "verifyCraftedVersion", null, 0.1f, false);
        }
        String templateName = getTemplateName(self);
        if (templateName != null && templateName.contains("factory_crate"))
        {
            String schematicName = getDraftSchematic(self);
            if (schematicName != null)
            {
                if (schematicName.contains("/pistol_flechette_fwg5.iff"))
                {
                    fixCrate(self, "powerhandler", 3);
                }
                else if (schematicName.contains("/pistol_scatter.iff"))
                {
                    fixCrate(self, "cartridge_feed_unit", 4);
                }
                else if (schematicName.contains("/pistol_launcher.iff"))
                {
                    fixCrate(self, "powerhandler", 3);
                }
            }
        }
        return SCRIPT_CONTINUE;
    }
    public void fixCrate(obj_id self, String slotName, int slotIndex) throws InterruptedException
    {
        obj_var_list slotList = getObjVarList(self, "ingr." + slotIndex);
        if (slotList == null)
        {
            return;
        }
        string_id slotFullName = getStringIdObjVar(self, "ingr." + slotIndex + ".name");
        if (slotFullName == null || !slotName.equals(slotFullName.getAsciiId()))
        {
            return;
        }
        CustomerServiceLog("Crafting", "Found crate " + self + " belonging to " + getOwner(self) + " which we think is a pistol crate that needs to be fixed");
        int newSlot = slotIndex;
        int oldSlot = newSlot + 1;
        for (; ; ++newSlot, ++oldSlot)
        {
            String oldName = "ingr." + oldSlot;
            String newName = "ingr." + newSlot;
            removeObjVar(self, newName);
            slotList = getObjVarList(self, oldName);
            if (slotList == null)
            {
                break;
            }
            int count = slotList.getNumItems();
            for (int i = 0; i < count; ++i)
            {
                obj_var ov = slotList.getObjVar(i);
                String newObjvarName = newName + "." + ov.getName();
                setObjVar(self, newObjvarName, ov.getData());
            }
        }
        removeObjVar(self, "ingr." + newSlot);
    }
    public int verifyCraftedVersion(obj_id self, dictionary params) throws InterruptedException
    {
        weapons.initalizeWeaponFactoryCrateManufacturingSchematics(self);
        return SCRIPT_CONTINUE;
    }
}
