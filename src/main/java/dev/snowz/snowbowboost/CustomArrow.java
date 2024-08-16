package dev.snowz.snowbowboost;

import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Objects;

public class CustomArrow {

    private static final SnowBowBoost plugin = SnowBowBoost.getInstance();

    private final ItemStack bow;
    private final Arrow arrow;
    private final Player shooter;

    private boolean leftHitbox;

    public CustomArrow(ItemStack bow, Arrow arrow) {
        this.bow = bow;
        this.arrow = arrow;
        this.shooter = (Player) arrow.getShooter();
    }

    public ItemStack getBow() {
        return bow;
    }

    public Arrow getArrow() {
        return arrow;
    }

    public Player getShooter() {
        return shooter;
    }

    public boolean hasLeftHitbox() {
        return leftHitbox;
    }

    public void setLeftHitbox(boolean leftHitbox) {
        this.leftHitbox = leftHitbox;
    }

    public Vector getPunchVelocity() {
        double horizontalVelocity = plugin.getConfig().getDouble("velocity-horizontal");
        double verticalVelocity = plugin.getConfig().getDouble("velocity-vertical");

        int punchLevel = bow.getEnchantmentLevel(Enchantment.PUNCH);
        horizontalVelocity *= (punchLevel + 1);

        Vector newVelocity = calculateVelocity(horizontalVelocity, verticalVelocity);
        newVelocity.add(shooter.getVelocity());

        return newVelocity;
    }

    private Vector calculateVelocity(double horizontalVelocity, double verticalVelocity) {
        Vector arrowDirection = arrow.getVelocity().clone().normalize();
        Vector horizontalVelocityScale = new Vector(horizontalVelocity, 0.0D, horizontalVelocity);
        Vector verticalVelocityScale = new Vector(0.0D, verticalVelocity, 0.0D);
        return arrowDirection.multiply(horizontalVelocityScale).add(verticalVelocityScale);
    }

    public boolean canBoost() {
        return isEnabledWorld() && isShooterInSurvival() && isArrowEligible() && hasLeftHitbox() && isArrowAliveLongEnough();
    }

    private boolean isEnabledWorld() {
        return Objects.requireNonNull(plugin.getConfig().getList("enabled-worlds")).contains(shooter.getWorld().getName());
    }

    private boolean isShooterInSurvival() {
        return shooter.getGameMode() != GameMode.CREATIVE && shooter.getGameMode() != GameMode.SPECTATOR;
    }

    private boolean isArrowEligible() {
        return shooter.getNoDamageTicks() <= 0 && shooter.getVelocity().length() > 0.0D && overlapsEnterHitbox();
    }

    public boolean outsideLeaveHitbox() {
        BoundingBox leaveHitBox = shooter.getBoundingBox().clone().expand(getLeaveHitboxExpansion());
        return !arrow.getBoundingBox().overlaps(leaveHitBox);
    }

    public boolean overlapsEnterHitbox() {
        BoundingBox enterHitBox = shooter.getBoundingBox().clone().expand(getEnterHitboxExpansion());
        return arrow.getBoundingBox().overlaps(enterHitBox);
    }

    private double getLeaveHitboxExpansion() {
        return 0.1D - plugin.getConfig().getDouble("leave-hitbox-reduction");
    }

    private double getEnterHitboxExpansion() {
        return 0.1D + plugin.getConfig().getDouble("enter-hitbox-expansion");
    }

    public boolean isArrowAliveLongEnough() {
        int minTicks = plugin.getConfig().getInt("min-life-ticks");
        double speedScale = calculateSpeedScale();
        return arrow.getTicksLived() >= minTicks * speedScale;
    }

    private double calculateSpeedScale() {
        double speed = arrow.getVelocity().length();
        double maxSpeed = 3.0D;
        return 1.0D - speed / maxSpeed;
    }

    public boolean isValid() {
        return shooter != null && arrow != null && arrow.isValid() && arrow.getTicksLived() < 15 && !arrow.isInBlock() && shooter.isValid();
    }
}
