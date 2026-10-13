package com.kompsos.system.core.systemmodules;

import com.kompsos.Klient;
import com.kompsos.system.core.config.KlientConfig;
import com.kompsos.system.core.moduleloader.Module;
import com.kompsos.system.core.moduleloader.managers.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandBlockMatrixSystem extends Module {
    private static final int THREADS = 4;
    private static final long FILL_COOLDOWN = TimeUnit.MILLISECONDS.toNanos(500);
    private static final ThreadFactory DAEMON = task -> {
        Thread thread = new Thread(task, "klient-matrix");
        thread.setDaemon(true);
        return thread;
    };

    private record Origin(int x, int z) {}

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(DAEMON);
    private final ThreadPoolExecutor queue;
    private final Set<BlockPos> broken = ConcurrentHashMap.newKeySet();
    private final AtomicInteger cursor = new AtomicInteger();

    private volatile Origin origin;
    private volatile Vec3 anchor;
    private volatile long lastFill = System.nanoTime() - FILL_COOLDOWN;

    public CommandBlockMatrixSystem() {
        super(ModuleManager.ExecutorMode.MULTI, Klient.id("command_block_manager_system"));

        // Bounded by maxQueue; when full the oldest command is dropped.
        queue = new ThreadPoolExecutor(THREADS, THREADS, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(Math.max(1, config().getMaxQueue())),
                DAEMON, new ThreadPoolExecutor.DiscardOldestPolicy());

        ClientChunkEvents.CHUNK_LOAD.register((_, chunk) -> {
            Origin o = origin;
            ChunkPos pos = chunk.getPos();
            if (o != null && pos.getMinBlockX() == o.x() && pos.getMinBlockZ() == o.z()) fill(true);
        });
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !active()) return;

        Vec3 pos = client.player.position();
        Vec3 last = anchor;
        double range = client.options.simulationDistance().get() * 16;
        if (last != null && Math.hypot(pos.x - last.x, pos.z - last.z) <= range) return;

        relocate(pos);
    }

    @Override
    public void worldClose() {
        origin = null;
        anchor = null;
        broken.clear();
    }

    /** Queues a command on the next block of the matrix. */
    public void run(String command) {
        if (command != null && active()) queue.execute(() -> send(command));
    }

    /** True once the matrix is placed, filled (both opposite corners are command blocks) and has no pending repairs. */
    public boolean isMatrixReady() {
        Origin o = origin;
        ClientLevel level = Minecraft.getInstance().level;
        if (o == null || level == null || !active() || !broken.isEmpty()) return false;

        return isCommandBlock(level.getBlockState(new BlockPos(o.x(), 0, o.z())))
                && isCommandBlock(level.getBlockState(new BlockPos(o.x() + area() - 1, height() - 1, o.z() + area() - 1)));
    }

    /** Mixin hook: returns true for the server feedback the matrix generates. */
    public boolean shouldHide(String message) {
        return message.startsWith("Command set:")
                || message.startsWith("Successfully filled")
                || message.startsWith("Must be an opped player in");
    }

    /** Mixin hook: repairs matrix blocks that get replaced or broken. */
    public void blockChanged(BlockPos pos, BlockState state) {
        Origin o = origin;
        if (o == null || !active() || !inside(pos, o)) return;

        if (isCommandBlock(state)) {
            broken.remove(pos);
        } else if (broken.add(pos.immutable())) {
            run("setblock %d %d %d minecraft:command_block".formatted(pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    private void send(String command) {
        Origin o = origin;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (o == null || connection == null) return;

        BlockPos target = next(o);
        if (broken.contains(target)) {
            broken.clear();
            cursor.set(0);
            fill(false);
            target = new BlockPos(o.x(), 0, o.z());
        }

        connection.send(new ServerboundSetCommandBlockPacket(target, command, CommandBlockEntity.Mode.AUTO, false, false, true));
    }

    /** Walks the matrix x first, then y, then z. */
    private BlockPos next(Origin o) {
        int total = area() * height() * area();
        int i = cursor.getAndUpdate(n -> (n + 1) % total) % total;
        return new BlockPos(o.x() + i % area(), i / area() % height(), o.z() + i / (area() * height()));
    }

    private void relocate(Vec3 pos) {
        anchor = pos;
        origin = new Origin(align(pos.x), align(pos.z));
        broken.clear();
        cursor.set(0);
        fill(true);
    }

    private void fill(boolean force) {
        Origin o = origin;
        long now = System.nanoTime();
        if (o == null || !active() || (!force && now - lastFill < FILL_COOLDOWN)) return;
        lastFill = now;

        String command = "fill %d 0 %d %d %d %d command_block{CustomName:'Klient'} replace"
                .formatted(o.x(), o.z(), o.x() + area() - 1, height() - 1, o.z() + area() - 1);

        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            ClientPacketListener connection = client.getConnection();
            if (config().isChatFill() && connection != null) connection.sendCommand(command);
            else singleBlockRun(command);
        });
    }

    private boolean inside(BlockPos pos, Origin o) {
        return pos.getX() >= o.x() && pos.getX() < o.x() + area()
                && pos.getZ() >= o.z() && pos.getZ() < o.z() + area()
                && pos.getY() >= 0 && pos.getY() < height();
    }

    private static boolean isCommandBlock(BlockState state) {
        return state.is(Blocks.COMMAND_BLOCK) || state.is(Blocks.REPEATING_COMMAND_BLOCK);
    }

    private boolean active() {
        KlientConfig.CommandBlockMatrix config = config();
        return config.isEnabled() && (!config.isOnlyCertifiedServers() || certified());
    }

    private boolean certified() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        if (server == null) return false;
        String host = server.ip.split(":")[0].toLowerCase();
        return Arrays.asList(Klient.configLoader.getConfigObject().miscellaneous.getOfficalKaboomServers()).contains(host);
    }

    private static KlientConfig.CommandBlockMatrix config() {
        return Klient.configLoader.getConfigObject().commandBlockMatrix;
    }

    private static int area() {
        return Math.max(1, config().getArea());
    }

    private static int height() {
        return Math.max(1, config().getzHeight());
    }

    private static int align(double coordinate) {
        return ((int) Math.floor(coordinate)) & ~15;
    }

    /** Runs one command through a temporary command block above the player. */
    public void singleBlockRun(String command) {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener connection = client.getConnection();
        if (client.player == null || connection == null) return;

        ItemStack oldItem = client.player.getInventory().getSelectedItem();
        int slot = 36 + client.player.getInventory().getSelectedSlot();
        BlockPos pos = new BlockPos(client.player.getBlockX(), (int) Math.round(client.player.getY()) + 3, client.player.getBlockZ());

        ServerboundPlayerActionPacket breakPacket = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP);

        connection.send(breakPacket);
        connection.send(new ServerboundSetCreativeModeSlotPacket(slot, Items.COMMAND_BLOCK.getDefaultInstance()));
        assert client.gameMode != null;
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        connection.send(new ServerboundSetCommandBlockPacket(pos, command, CommandBlockEntity.Mode.AUTO, false, false, true));

        scheduler.schedule(() -> {
            connection.send(breakPacket);
            connection.send(new ServerboundSetCreativeModeSlotPacket(slot, oldItem));
        }, 50, TimeUnit.MILLISECONDS);
    }
}