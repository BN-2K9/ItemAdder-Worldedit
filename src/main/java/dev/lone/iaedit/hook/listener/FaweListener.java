package dev.lone.iaedit.hook.listener;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import dev.lone.iaedit.hook.WorldEditHook;
import dev.lone.iaedit.hook.delegate.FaweCustomBlocksDelegate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

public class FaweListener implements Listener
{
    public FaweListener(Plugin plugin)
    {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Subscribe(priority = com.sk89q.worldedit.util.eventbus.EventHandler.Priority.VERY_LATE)
    public void onEditSessionEvent(EditSessionEvent e)
    {
        Bukkit.getLogger().info("Fired Event");
        if(e.getStage() != EditSession.Stage.BEFORE_CHANGE)
            return;

        e.setExtent(new FaweCustomBlocksDelegate(e));
    }

    @EventHandler
    private void command(PlayerCommandPreprocessEvent e)
    {
        String command = e.getMessage();
        if(!command.startsWith("//replace") && !command.startsWith("//replacenear"))
            return;

        WorldEditHook.handleReplaceCommand(e, command.startsWith("//replacenear"));
    }
}
