package usa.cactuspuppy.admit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.PlayerList;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Bukkit.class
})
public class MainTest {
    private static Player bypasser;
    private static Player pleb;
    private static InetAddress mockAddress = Mockito.mock(InetAddress.class);
    private ServerMock server;
    private Main main;


    @BeforeClass
    public static void setupClass() {
        PowerMockito.mockStatic(Bukkit.class);
        bypasser = Mockito.mock(Player.class);
        pleb = Mockito.mock(Player.class);

        Mockito.when(bypasser.hasPermission("admit.bypass")).thenReturn(true);
        Mockito.when(pleb.hasPermission("admit.bypass")).thenReturn(false);
    }

    @Before
    public void setup() {
        server = MockBukkit.mock();
        main = MockBukkit.load(Main.class);
    }

    @Test
    public void normalLoginUnderOverride() {
        Main.setBypassMode(BypassMode.OVERRIDE);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.ALLOWED, "", mockAddress);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void nonFullKicksUnderOverride() {
        Main.setBypassMode(BypassMode.OVERRIDE);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_BANNED, "", mockAddress);
        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.KICK_BANNED, event.getResult());

        event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_WHITELIST, "", mockAddress);
        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.KICK_WHITELIST, event.getResult());

        event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_OTHER, "", mockAddress);
        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.KICK_OTHER, event.getResult());
    }

    @Test
    public void plebLoginUnderNoCount() {
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.setPlayers(7);
        server.setMaxPlayers(10);


        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void normalLoginUnderNoCount() {
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.ALLOWED, "", mockAddress);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }


}