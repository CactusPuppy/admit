package usa.cactuspuppy.admit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class MainTest {
    private static Player bypasser;
    private static Player pleb;
    private static InetAddress mockAddress = Mockito.mock(InetAddress.class);
    private static ServerMock server;
    private static Main main;


    @BeforeClass
    public static void setupClass() {
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

    @After
    public void tearDown() {
        MockBukkit.unload();
    }

    @Test
    public void normalLoginUnderOverride() {
        System.out.println("Testing normal login with Override mode...");
        Main.setBypassMode(BypassMode.OVERRIDE);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.ALLOWED, "", mockAddress);
        server.setPlayers(7);
        server.setMaxPlayers(10);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void normalLoginUnderNoCount() {
        System.out.println("Testing normal login with No Count mode...");
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.ALLOWED, "", mockAddress);
        server.setPlayers(7);
        server.setMaxPlayers(10);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void nonFullKicksUnderOverride() {
        System.out.println("Testing all non-interesting kicks...");
        Main.setBypassMode(BypassMode.OVERRIDE);
        server.setPlayers(7);
        server.setMaxPlayers(10);
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
        System.out.println("Testing allowed no count mode...");
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.setMaxPlayers(2);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());

        server.setPlayers(0);
        event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(3);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void plebKickUnderNoCount() {
        System.out.println("Testing kicking under no count mode...");
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(2);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.KICK_FULL, event.getResult());
    }

    @Test
    public void bypassJoinUnderNoCountNotFull() {
        System.out.println("Testing bypasser joining under no count with non-full server...");
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(bypasser, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(10);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());

        server.setPlayers(0);
        server.setPlayers(0);
        event = new PlayerLoginEvent(bypasser, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb3", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb4", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb5", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(10);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void bypassJoinUnderNoCountFull() {
        System.out.println("Testing bypasser joining under no count mode with full server...");
        Main.setBypassMode(BypassMode.NO_COUNT);
        PlayerLoginEvent event = new PlayerLoginEvent(bypasser, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(2);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());

        server.setPlayers(0);
        event = new PlayerLoginEvent(bypasser, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb3", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb4", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb5", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(5);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Test
    public void plebLoginThenDenyOverride() {
        System.out.println("Testing pleb join then deny under override...");
        Main.setBypassMode(BypassMode.OVERRIDE);
        PlayerLoginEvent event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.ALLOWED, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(6);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());

        server.setPlayers(0);
        event = new PlayerLoginEvent(pleb, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(5);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.KICK_FULL, event.getResult());
    }

    @Test
    public void testBypasserOverride() {
        System.out.println("Test bypasser override...");
        Main.setBypassMode(BypassMode.OVERRIDE);
        PlayerLoginEvent event = new PlayerLoginEvent(bypasser, "", mockAddress,
                PlayerLoginEvent.Result.ALLOWED, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(6);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());

        server.setPlayers(0);
        event = new PlayerLoginEvent(bypasser, "", mockAddress,
                PlayerLoginEvent.Result.KICK_FULL, "", mockAddress);
        server.addPlayer(new PermissiblePlayerMock(server, "pleb", false));
        server.addPlayer(new PermissiblePlayerMock(server, "pleb2", false));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser2", true));
        server.addPlayer(new PermissiblePlayerMock(server, "bypasser3", true));
        server.setMaxPlayers(5);

        main.onPlayerLogin(event);
        assertEquals(PlayerLoginEvent.Result.ALLOWED, event.getResult());
    }

    @Ignore
    public static class PermissiblePlayerMock extends PlayerMock {
        private boolean isBypasser;

        public PermissiblePlayerMock(ServerMock server, String name, boolean isBypasser) {
            super(server, name);
            this.isBypasser = isBypasser;
        }

        @Override
        public boolean hasPermission(String name) {
            if (!name.equals("admit.bypass")) {
                return false;
            }
            return isBypasser;
        }
    }
}