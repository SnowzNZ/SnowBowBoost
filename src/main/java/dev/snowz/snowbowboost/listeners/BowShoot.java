package dev.snowz.snowbowboost.listeners;

import dev.snowz.snowbowboost.SnowBowBoost;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

public class BowShoot implements Listener {

    private static final SnowBowBoost plugin = SnowBowBoost.getInstance();


    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Entity projectile = event.getProjectile();
        if (!(livingEntity instanceof Player) || !(projectile instanceof Arrow))
            return;
        Arrow arrow = (Arrow)event.getProjectile();
        Vector shooterLook = livingEntity.getLocation().getDirection().clone();
        double arrowSpeed = arrow.getVelocity().length();
        if (plugin.getConfig().getBoolean("remove-arrow-randomization"))
            arrow.setVelocity(shooterLook.multiply(arrowSpeed));
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkBoost(arrow), 1L);
    }

    public void checkBoost(Arrow arrow) {
        if (arrow.isValid()) {
            if (arrow.outsideLeaveHitbox())
                arrow.setLeftHitbox(true);
            if (arrow.canBoost()) {
                fakeShot(arrow);
                return;
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkBoost(arrow), 1L);
        }
    }

    public static void fakeShot(Arrow arrowInfo) {
        Player player = arrowInfo.getShooter();
        Arrow arrow = arrowInfo.getArrow();
        if (!arrowInfo.isValid())
            return;
        player.setVelocity(arrowInfo.getPunchVelocity());
        damagePlayer(player, arrow);
        damageArmor(player);
        if (arrow.hasCustomEffects())
            for (PotionEffect effect : arrow.getCustomEffects())
                player.addPotionEffect(effect);
        if (LegacyBowBoosting.getInstance().getConfig().getBoolean("burn-booster") &&
                arrow.getFireTicks() > 0) {
            int flameDuration = 100;
            if (flameDuration > player.getFireTicks())
                player.setFireTicks(flameDuration);
        }
        arrow.remove();
    }

    public static void damageArmor(Player player) {
        Random random = new Random();
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                int unbreaking = armor.getEnchantmentLevel(Enchantment.DURABILITY);
                float chanceOfDamaging = 1.0F / (unbreaking + 1.0F);
                if (random.nextFloat() < chanceOfDamaging)
                    armor.setDurability((short)(armor.getDurability() - 1));
            }
        }
    }

    public static void damagePlayer(Player player, Arrow arrow) {
        player.damage(arrow.getDamage());
    }
}
