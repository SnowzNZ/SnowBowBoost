package dev.snowz.snowbowboost;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Arrow {
    private final SnowBowBoost plugin;

    private final org.bukkit.entity.Arrow arrow;

    private final Player shooter;

    private boolean leftHitbox;

    public Arrow(SnowBowBoost plugin, org.bukkit.entity.Arrow arrow) {
        this.plugin = plugin;
        this.arrow = arrow;
        this.shooter = (Player) arrow.getShooter();
        this.leftHitbox = false;
    }

    public org.bukkit.entity.Arrow getArrow() {
        return this.arrow;
    }

    public Player getShooter() {
        return this.shooter;
    }

    public boolean getLeftHitbox() {
        return this.leftHitbox;
    }

    public void setLeftHitbox(boolean leftHitbox) {
        this.leftHitbox = leftHitbox;
    }

    public Vector getPunchVelocity() {
        double horizontalVelocity = this.plugin.getConfig().getDouble("velocity-horizontal");
        double verticalVelocity = this.plugin.getConfig().getDouble("velocity-vertical");
        int punch = this.arrow.getKnockbackStrength();
        horizontalVelocity *= (punch + 1);
        Vector horizontalVelocityScale = new Vector(horizontalVelocity, 0.0D, horizontalVelocity);
        Vector verticalVelocityScale = new Vector(0.0D, verticalVelocity, 0.0D);
        Vector arrowDirection = this.arrow.getVelocity().clone().normalize();
        Vector newVelocity = arrowDirection.multiply(horizontalVelocityScale).add(verticalVelocityScale);
        newVelocity = newVelocity.add(this.shooter.getVelocity());
        return newVelocity;
    }

    public boolean canBoost() {
        if (!this.shooter.getWorld().getName().equals("clanffa"))
            return false;
        if (this.shooter.getGameMode() == GameMode.CREATIVE || this.shooter.getGameMode() == GameMode.SPECTATOR)
            return false;
        if (this.shooter.getNoDamageTicks() > 0)
            return false;
        if (this.shooter.getVelocity().length() == 0.0D)
            return false;
        if (!overlapsEnterHitbox())
            return false;
        if (!getLeftHitbox())
            return false;
        if (!isAliveLongEnough())
            return false;
        return true;
    }

    public boolean outsideLeaveHitbox() {
        double leaveHitboxReduction = this.plugin.getConfig().getDouble("leave-hitbox-reduction");
        BoundingBox leaveHitBox = this.shooter.getBoundingBox().clone().expand(0.1D - leaveHitboxReduction);
        BoundingBox arrowBox = this.arrow.getBoundingBox();
        return !arrowBox.overlaps(leaveHitBox);
    }

    public boolean overlapsEnterHitbox() {
        double enterHitboxExpansion = this.plugin.getConfig().getDouble("enter-hitbox-expansion");
        BoundingBox enterHitBox = this.shooter.getBoundingBox().clone().expand(0.1D + enterHitboxExpansion);
        BoundingBox arrowBox = this.arrow.getBoundingBox();
        return arrowBox.overlaps(enterHitBox);
    }

    public boolean isAliveLongEnough() {
        double minTicks = this.plugin.getConfig().getInt("min-life-ticks");
        double speed = this.arrow.getVelocity().length();
        double maxSpeed = 3.0D;
        double scale = 1.0D - speed / maxSpeed;
        minTicks *= scale;
        return (this.arrow.getTicksLived() >= minTicks);
    }

    public boolean isValid() {
        return (this.shooter != null && this.arrow != null && this.arrow

                .isValid() && this.arrow
                .getTicksLived() < 15 &&
                !this.arrow.isInBlock() && this.shooter
                .isValid());
    }
}