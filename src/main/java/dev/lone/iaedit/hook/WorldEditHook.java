package dev.lone.iaedit.hook;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import dev.lone.iaedit.hook.delegate.FaweCustomBlocksDelegate;
import dev.lone.iaedit.hook.delegate.WeCustomBlocksDelegate;
import dev.lone.iaedit.hook.listener.FaweListener;
import dev.lone.iaedit.hook.listener.WorldEditListener;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

public class WorldEditHook
{
    InputParser<BaseBlock> customBlocksInputParser;
    Object listener;

    public WorldEditHook(Plugin plugin)
    {
        boolean isFawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
        if(isFawe)
        {
            listener = new FaweListener(plugin);
            com.fastasyncworldedit.core.configuration.Settings.settings().EXTENT.ALLOWED_PLUGINS.add(WeCustomBlocksDelegate.class.getCanonicalName());
            com.fastasyncworldedit.core.configuration.Settings.settings().EXTENT.ALLOWED_PLUGINS.add(FaweCustomBlocksDelegate.class.getCanonicalName());
        }
        else
        {
            listener = new WorldEditListener(plugin);
        }

        customBlocksInputParser = new CustomBlocksInputParser(WorldEdit.getInstance());
    }

    public void register()
    {
        WorldEdit.getInstance().getEventBus().register(listener);
        WorldEdit.getInstance().getBlockFactory().register(customBlocksInputParser);
    }

    public void unregister()
    {
        WorldEdit.getInstance().getEventBus().unregister(listener);
    }

    public static void handleReplaceCommand(PlayerCommandPreprocessEvent e, boolean replaceNear)
    {
        String command = e.getMessage();

        String newCommand = command;
        String[] words = command.split(" ");

        if(words.length >= 1)
        {
            String blockDataStr = words[replaceNear ? 2 : 1];

            BlockData blockData = CustomBlock.getBaseBlockData(blockDataStr);
            if(blockData == null)
                return;

            newCommand = newCommand.replace(blockDataStr, blockData.getAsString(true));
        }

        e.setMessage(newCommand);
    }
}
