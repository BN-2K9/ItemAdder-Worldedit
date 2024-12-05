package dev.lone.iaedit.hook.delegate;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import dev.lone.iaedit.hook.CustomBlocksInputParser;
import dev.lone.iaedit.Main;
import dev.lone.iaedit.hook.util.Data;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

import java.util.List;

public abstract class AbstractCustomBlocksDelegate extends AbstractDelegateExtent
{
    protected final EditSessionEvent e;

    private final Main pl;

    public AbstractCustomBlocksDelegate(Main plugin,EditSessionEvent e)
    {
        super(e.getExtent());
        this.e = e;
        this.pl = plugin;
    }

    public interface MyCallback {
        void call(CustomFurniture f) ;
    }

    public <T extends BlockStateHolder<T>> Result setBlock0(int x, int y, int z, T baseBlock) throws WorldEditException
    {
        World world = Bukkit.getWorld(e.getWorld().getName());
        Location loc = new Location(world, x,y,z);
        Bukkit.getLogger().info("Starting SetBlock");
        if(CustomBlock.byAlreadyPlaced(world.getBlockAt(loc)) != null) {
            Bukkit.getLogger().info("Is Custom Block");
            CustomBlock.remove(loc);

            if (CustomBlocksInputParser.isCustomBlockType(baseBlock.getBlockType()))
            {
                BaseBlock bb = baseBlock.toBaseBlock();
                Bukkit.getLogger().info("");
                if(bb.getNbt() != null)
                {
                    Bukkit.getLogger().info("BB Not null");
                    LinCompoundTag nbt = bb.getNbt();
                    // //set
                    if(nbt.value().containsKey("IABlock"))
                    {
                        String namespacedId = nbt.value().get("IABlock").toString();
                        placeCustomBlock(loc, namespacedId);
                        return Result.YES;
                    }
                    // //paste
                    else if(baseBlock.getBlockType() == BlockTypes.SPAWNER)
                    {
                        Bukkit.getLogger().info("CustomBlock");
                        // RETURN ONLY IF IT'S CUSTOM BLOCK
                        if(nbt.value().containsKey("SpawnData"))
                        {
                            LinCompoundTag entityTag = (LinCompoundTag) nbt.getTag("SpawnData", nbt.type()).getTag("entity", nbt.type());
                            if(entityTag != null)
                            {
                                if(entityTag.value().containsKey("id") && entityTag.value().containsKey("ArmorItems")
                                        && entityTag.value().get("id").equals("minecraft:armor_stand"))
                                {
                                    List armorItemsList = (List) entityTag.value().get("ArmorItems").value();
                                    if(armorItemsList.size() == 4)
                                    {
                                        LinCompoundTag helmetItemTag = (LinCompoundTag) armorItemsList.get(3);
                                        LinCompoundTag tagValue = (LinCompoundTag) helmetItemTag.value().get("tag");
                                        LinTag customModelDataTag = tagValue.value().get("CustomModelData");
                                        Integer cmd = Integer.valueOf(customModelDataTag.value().toString());

                                        // Stupidly slow
                                        for (String namespacedId : CustomBlock.getNamespacedIdsInRegistry())
                                        {
                                            CustomBlock customBlock = CustomBlock.getInstance(namespacedId);
                                            if(customBlock.getItemStack().getItemMeta().getCustomModelData() == cmd.intValue())
                                            {
                                                placeCustomBlock(loc, namespacedId);
                                                return Result.YES;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else // //paste
                {
                    Bukkit.getLogger().info("Detected Paste!");
                    // Stupidly slow
                    String bbToString = bb.getAsString();
                    Bukkit.getLogger().info(bbToString);
                    for (String namespacedId : CustomStack.getNamespacedIdsInRegistry())
                    {
                        Bukkit.getLogger().info(namespacedId);
                        BlockData customBlockData = CustomBlock.getBaseBlockData(namespacedId);
                        // check this part out
                        if (customBlockData == null )
                            break;

                        String customBlockDataStr = customBlockData.getAsString(false);
                        if (customBlockDataStr.equals(bbToString))
                        {
                            placeCustomBlock(loc, namespacedId);
                            return Result.YES;
                        }
                    }
                }
            }


        }

        BukkitTask runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getLogger().info("running");

                CustomFurniture furniture = CustomFurniture.byAlreadySpawned(world.getBlockAt(loc));
                BaseBlock bb = baseBlock.toBaseBlock();

                

                if (furniture != null) {

                    Bukkit.getLogger().info("Not null furniture");

                    CustomFurniture.remove(furniture.getEntity(), false);

                    if (CustomBlocksInputParser.isCustomBlockType(baseBlock.getBlockType())) {

                        Bukkit.getLogger().info(bb.getStates().get("axis").toString());
                    }

                }

            }
        }.runTask(Main.instance);

        return Result.FALLBACK;
    }

    public abstract <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T baseBlock) throws WorldEditException;

    public abstract void placeCustomBlock(Location location, String namespacedId);

    public void placeCustomBlock0(Location location, String namespacedId)
    {
        CustomBlock customBlock = CustomBlock.getInstance(namespacedId);
        customBlock.place(location);

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (location.getWorld() != null)
            {
                BlockData blockData = location.getBlock().getBlockData();
                location.getWorld().getPlayers().forEach(player -> {
                    if (player.getLocation().distance(location) <= 64)
                        player.sendBlockChange(location, blockData);
                });
            }
        }, 5L);
    }

    public void placeCustomFurniture0(Location location, String namespacedId)
    {
        CustomFurniture.spawn(namespacedId, location.getBlock());

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (location.getWorld() != null)
            {
                BlockData blockData = location.getBlock().getBlockData();
                location.getWorld().getPlayers().forEach(player -> {
                    if (player.getLocation().distance(location) <= 64)
                        player.sendBlockChange(location, blockData);
                });
            }
        }, 5L);
    }

    public enum Result
    {
        FALLBACK,
        YES,
        NO
    }
}
