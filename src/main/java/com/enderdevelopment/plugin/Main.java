package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener {
    private final Random random = new Random();
    private BukkitTask encounterTask;
    private BukkitTask stalkingTask;
    private Player target;
    private Location apparition;
    private int stalkingTicks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        long interval = Math.max(20L, getConfig().getLong("check-interval-seconds", 60L) * 20L);
        encounterTask = Bukkit.getScheduler().runTaskTimer(this, this::tryEncounter, interval, interval);
    }

    @Override
    public void onDisable() {
        if (encounterTask != null) encounterTask.cancel();
        endEncounter(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text(target == null ? "Herobrine is dormant." : "Herobrine is stalking " + target.getName() + ".", NamedTextColor.GRAY));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("summon")) {
            Player chosen = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            if (chosen == null) {
                sender.sendMessage(Component.text("No player is online.", NamedTextColor.RED));
            } else {
                startEncounter(chosen);
                sender.sendMessage(Component.text("The white eyes have opened.", NamedTextColor.DARK_RED));
            }
            return true;
        }
        sender.sendMessage(Component.text("Usage: /herobrine <status|summon>", NamedTextColor.GRAY));
        return true;
    }

    private void tryEncounter() {
        if (target != null || Bukkit.getOnlinePlayers().isEmpty()) return;
        if (random.nextDouble() > getConfig().getDouble("encounter-chance", 0.012)) return;
        List<Player> eligible = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            long time = world.getTime() % 24000L;
            if (time >= 13000L && time <= 23000L && !player.isDead() && world.getEnvironment() != World.Environment.THE_END) eligible.add(player);
        }
        if (!eligible.isEmpty()) startEncounter(eligible.get(random.nextInt(eligible.size())));
    }

    private void startEncounter(Player player) {
        endEncounter(false);
        Location spot = findApparitionSpot(player);
        if (spot == null) return;
        target = player;
        apparition = spot;
        stalkingTicks = 0;
        buildEerieShrine(spot);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false, true));
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.45f);
        player.sendActionBar(Component.text("You feel watched.", NamedTextColor.DARK_RED));
        stalkingTask = Bukkit.getScheduler().runTaskTimer(this, this::tickStalking, 20L, 20L);
    }

    private Location findApparitionSpot(Player player) {
        int min = getConfig().getInt("minimum-player-distance", 24);
        int max = getConfig().getInt("maximum-player-distance", 48);
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int distance = min + random.nextInt(Math.max(1, max - min + 1));
            Location candidate = player.getLocation().clone().add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
            int y = player.getWorld().getHighestBlockYAt(candidate);
            candidate.setY(y + 1);
            if (candidate.getBlock().getType().isAir() && candidate.clone().add(0, 1, 0).getBlock().getType().isAir()) return candidate;
        }
        return null;
    }

    private void buildEerieShrine(Location center) {
        World world = center.getWorld();
        int radius = Math.max(2, getConfig().getInt("structure-radius", 3));
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;
                Block block = center.clone().add(x, -1, z).getBlock();
                if (block.getType().isAir() || block.getType() == Material.GRASS_BLOCK || block.getType() == Material.TALL_GRASS) block.setType(Material.SOUL_SOIL, false);
            }
        }
        for (int y = 0; y < 4; y++) {
            setIfReplaceable(center.clone().add(0, y, 0), Material.DEEPSLATE);
            if (y < 3) {
                setIfReplaceable(center.clone().add(-2, y, 0), Material.DEEPSLATE);
                setIfReplaceable(center.clone().add(2, y, 0), Material.DEEPSLATE);
            }
        }
        setIfReplaceable(center.clone().add(0, 4, 0), Material.SOUL_LANTERN);
        setIfReplaceable(center.clone().add(-1, 0, 0), Material.RED_CANDLE);
        setIfReplaceable(center.clone().add(1, 0, 0), Material.RED_CANDLE);
        world.spawnParticle(Particle.SOUL, center.clone().add(0, 1, 0), 35, 1.5, 1.5, 1.5, 0.02);
        world.playSound(center, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.8f, 0.5f);
    }

    private void setIfReplaceable(Location location, Material material) {
        Block block = location.getBlock();
        if (block.getType().isAir() || block.getType() == Material.GRASS_BLOCK || block.getType() == Material.TALL_GRASS || block.getType() == Material.SNOW) block.setType(material, false);
    }

    private void tickStalking() {
        if (target == null || !target.isOnline() || target.isDead()) {
            endEncounter(false);
            return;
        }
        stalkingTicks++;
        World world = target.getWorld();
        world.spawnParticle(Particle.ASH, target.getLocation().add(0, 1, 0), 12, 2.0, 0.8, 2.0, 0.01);
        if (stalkingTicks % 5 == 0) {
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.45f, 0.5f);
            target.sendActionBar(Component.text("Something is behind you.", NamedTextColor.DARK_GRAY));
        }
        int duration = getConfig().getInt("stalking-duration-seconds", 45);
        if (stalkingTicks >= duration) endEncounter(true);
    }

    private void endEncounter(boolean message) {
        if (stalkingTask != null) {
            stalkingTask.cancel();
            stalkingTask = null;
        }
        if (message && target != null) {
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.35f);
            target.sendMessage(Component.text("The silence returns. The shrine remains.", NamedTextColor.GRAY));
        }
        target = null;
        apparition = null;
    }
}
