package dev.snowz.snowbowboost.listeners;

import dev.snowz.snowbowboost.CustomArrow;
import dev.snowz.snowbowboost.SnowBowBoost;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import java.util.Random;

public class BowShoot implements Listener {

    private static final SnowBowBoost plugin = SnowBowBoost.getInstance();
    private static final Random random = new Random();

    public static void fakeShot(CustomArrow arrowInfo) {
        Player player = arrowInfo.getShooter();
        Arrow arrow = arrowInfo.getArrow();

        if (!arrowInfo.isValid()) {
            return;
        }

        player.setVelocity(arrowInfo.getPunchVelocity());
        applyDamage(player, arrow);
        applyArmorDamage(player);

        arrow.getCustomEffects().forEach(player::addPotionEffect);

        if (shouldBurnBooster(arrow)) {
            setPlayerOnFire(player);
        }

        arrow.remove();
    }

    private static boolean shouldBurnBooster(Arrow arrow) {
        return plugin.getConfig().getBoolean("flame") && arrow.getFireTicks() > 0;
    }

    private static void setPlayerOnFire(Player player) {
        int flameDuration = 100;
        if (flameDuration > player.getFireTicks()) {
            player.setFireTicks(flameDuration);
        }
    }

    private static void applyArmorDamage(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                int unbreakingLevel = armor.getEnchantmentLevel(Enchantment.UNBREAKING);
                if (random.nextFloat() < calculateDamageChance(unbreakingLevel)) {
                    Damageable damageable = (Damageable) armor.getItemMeta();
                    damageable.setDamage(damageable.getDamage() - 1);
                }
            }
        }
    }

    private static float calculateDamageChance(int unbreakingLevel) {
        return 1.0F / (unbreakingLevel + 1.0F);
    }

    private static void applyDamage(Player player, Arrow arrow) {
        player.damage(arrow.getDamage());
    }

    private void scheduleBoostCheck(CustomArrow arrow) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkBoost(arrow), 1L);
    }

    public void checkBoost(CustomArrow arrow) {
        if (!arrow.isValid()) {
            return;
        }

        if (arrow.outsideLeaveHitbox()) {
            arrow.setLeftHitbox(true);
        }

        if (arrow.canBoost()) {
            fakeShot(arrow);
        } else {
            scheduleBoostCheck(arrow);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        ItemStack bow = event.getBow();

        removeArrowRandomizationIfNeeded(event.getEntity(), arrow);

        scheduleBoostCheck(new CustomArrow(bow, arrow));
    }

    private void removeArrowRandomizationIfNeeded(LivingEntity shooter, Arrow arrow) {
        if (!plugin.getConfig().getBoolean("arrow-randomization")) {
            Vector shooterDirection = shooter.getLocation().getDirection().clone();
            double arrowSpeed = arrow.getVelocity().length();
            arrow.setVelocity(shooterDirection.multiply(arrowSpeed));
        }
    }
}
