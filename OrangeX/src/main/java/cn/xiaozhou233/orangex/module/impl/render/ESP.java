package cn.xiaozhou233.orangex.module.impl.render;

import cn.xiaozhou233.orangex.event.impl.EventRender3D;
import cn.xiaozhou233.orangex.module.Module;
import cn.xiaozhou233.orangex.module.ModuleCategory;
import cn.xiaozhou233.orangex.module.option.impl.BooleanOption;
import cn.xiaozhou233.orangex.utils.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import org.greenrobot.eventbus.Subscribe;

public class ESP extends Module {

    public ESP() {
        super("ESP", "See entities through walls", ModuleCategory.RENDER);

        addOption(new BooleanOption("Render Self", "Render self", this, false));
        addOption(new BooleanOption("Render Players", "Render players", this, true));
        addOption(new BooleanOption("Render Villagers", "Render villagers", this, false));
        addOption(new BooleanOption("Render Mobs", "Render mobs", this, false));
        addOption(new BooleanOption("Render Animals", "Render animals", this, false));
        addOption(new BooleanOption("Render Items", "Render items", this, false));
    }

    @Subscribe
    public void onRender3D(EventRender3D event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        BooleanOption renderSelf = getOption("Render Self", BooleanOption.class);
        BooleanOption renderPlayers = getOption("Render Players", BooleanOption.class);
        BooleanOption renderVillagers = getOption("Render Villagers", BooleanOption.class);
        BooleanOption renderMobs = getOption("Render Mobs", BooleanOption.class);
        BooleanOption renderAnimals = getOption("Render Animals", BooleanOption.class);
        BooleanOption renderItems = getOption("Render Items", BooleanOption.class);

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer && !renderSelf.getValue())
                continue;

            if (entity instanceof EntityPlayer) {
                if (!renderPlayers.getValue()) continue;
            } else if (entity instanceof EntityVillager) {
                if (!renderVillagers.getValue()) continue;
            } else if (entity instanceof EntityAnimal) {
                if (!renderAnimals.getValue()) continue;
            } else if (entity instanceof EntityMob) {
                if (!renderMobs.getValue()) continue;
            } else if (entity instanceof EntityItem) {
                if (!renderItems.getValue()) continue;
            } else {
                continue;
            }

            RenderUtils.drawEntityBox(entity, event.getPartialTicks());
        }
    }
}