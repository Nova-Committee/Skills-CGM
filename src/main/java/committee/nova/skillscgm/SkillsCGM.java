package committee.nova.skillscgm;

import com.mrcrayfish.guns.entity.DamageSourceProjectile;
import com.mrcrayfish.guns.entity.EntityProjectile;
import committee.nova.skillful.api.skill.ISkill;
import committee.nova.skillful.impl.skill.Skill;
import committee.nova.skillful.impl.skill.instance.SkillInstance;
import committee.nova.skillful.storage.SkillfulStorage.SkillRegisterEvent;
import committee.nova.skillful.util.Utilities;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Mod(modid = SkillsCGM.MODID)
public class SkillsCGM {
    public static final String MODID = "skillscgm";
    private static final String DISPERSION = "dispersionSet";
    public static final ISkill FIREARM_CGM = new Skill(new ResourceLocation(MODID, "firearm"), 100, BossInfo.Color.BLUE, (int i) -> i * 200);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onSkillRegister(SkillRegisterEvent event) {
        event.addSkill(FIREARM_CGM);
    }

    @SubscribeEvent
    public void onDamageModifier(LivingHurtEvent event) {
        if (!(event.getSource() instanceof DamageSourceProjectile)) return;
        final DamageSourceProjectile s = (DamageSourceProjectile) event.getSource();
        if (!(s.getTrueSource() instanceof EntityPlayerMP)) return;
        final EntityPlayerMP player = (EntityPlayerMP) s.getTrueSource();
        final SkillInstance firearm = Utilities.getPlayerSkillStat(player, FIREARM_CGM);
        event.setAmount(event.getAmount() * (1.0F + Math.max(.0F, (firearm.getCurrentLevel() - 10.0F) / 50.0F)));
        firearm.addXp(player, 1);
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        if (!(event.getSource() instanceof DamageSourceProjectile)) return;
        final DamageSourceProjectile s = (DamageSourceProjectile) event.getSource();
        if (!(s.getTrueSource() instanceof EntityPlayerMP)) return;
        final EntityPlayerMP player = (EntityPlayerMP) s.getTrueSource();
        Utilities.getPlayerSkillStat(player, FIREARM_CGM).addXp(player, 5);
    }

    @SubscribeEvent
    public void onProjectileSpawn(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile)) return;
        final EntityProjectile bullet = (EntityProjectile) event.getEntity();
        if (!(bullet.getShooter() instanceof EntityPlayerMP)) return;
        final EntityPlayerMP shooter = (EntityPlayerMP) bullet.getShooter();
        final SkillInstance firearm = Utilities.getPlayerSkillStat(shooter, FIREARM_CGM);
        if (firearm.getCurrentLevel() > 20) return;
        final int dispersion = 21 - firearm.getCurrentLevel();
        final NBTTagCompound data = bullet.getEntityData();
        if (data.getBoolean(DISPERSION)) return;
        final Random rand = shooter.getRNG();
        if (rand.nextInt(Math.max(dispersion, 11)) < 5) return;
        bullet.motionX += dispersion * (rand.nextDouble() - 0.5) * 0.025;
        bullet.motionY += dispersion * (rand.nextDouble() - 0.5) * 0.025;
        bullet.motionZ += dispersion * (rand.nextDouble() - 0.5) * 0.025;
        data.setBoolean(DISPERSION, true);
    }
}
