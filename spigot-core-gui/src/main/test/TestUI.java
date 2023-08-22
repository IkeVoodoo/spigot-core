import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.pages.PagePosition;
import me.ikevoodoo.spigotcore.gui.pages.PageType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class TestUI {

    public static void main(String[] args) {
        var screen = new Screen(null, "Player Selection");

        var players = new ArrayList<>(Bukkit.getOnlinePlayers());
        var count = players.size() - 1;
        var perPage = 9 * 5;

        screen.createPages(page -> {
            page.addButton(PagePosition.bottomLeft(), screen.nextButton());
            page.addButton(PagePosition.bottomRight(), screen.previousButton());

            var offset = page.index() * perPage;
            for (int i = 0; i < perPage; i++) {
                var player = players.get(offset + i);

                var item = new ItemStack(Material.PLAYER_HEAD);
                var meta = (SkullMeta) item.getItemMeta();
                assert meta != null;
                meta.setOwningPlayer(player);
                item.setItemMeta(meta);

                page.setItem(page.slotPosition(i), item);
            }

            page.addClickHandler((event, stack, type) -> {
                event.screen().close(event.player());

                if (stack.getItemMeta() instanceof SkullMeta skullMeta) {
                    var player = skullMeta.getOwningPlayer();
                    if (player == null) return;

                    var name = player.isOnline() ? player.getPlayer().getDisplayName() : player.getName();

                    event.player().sendMessage("§aYou selected §3" + name);
                }
            });
        }, PageType.chest(6), count / perPage);

        var player = Bukkit.getPlayer("IkeVoodoo");

        assert player != null;
        screen.open(player);
    }

}
