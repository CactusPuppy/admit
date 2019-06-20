package usa.cactuspuppy.admit.utils;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class ConfigTest {
    private String input;
    private File testDir;
    private File testConfig;

    @Before
    public void setup() {
        testDir = new File("test");
        if (!testDir.isDirectory()) assumeTrue(testDir.mkdirs());
        input = "label:\n  label1:  asdf";
        testConfig = new File("test", "config.yml");
        if (!testConfig.isFile()) {
            try {
                assertTrue(testConfig.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void cleanup() {
        try {
            FileUtils.deleteDirectory(testDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readValuesSimple() throws InvalidConfigurationException {
        Config testReader = new Config(testConfig);
        testReader.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("asdf", testReader.get("label.label1"));
        System.out.println(testReader.toString());
    }

    @Test
    public void readValuesBackIndent() throws InvalidConfigurationException {
        input = "label:\n" +
                "  label1:  asdf\n" +
                "label2: true";
        Config testReader = new Config(testConfig);
        testReader.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("true", testReader.get("label2"));
        System.out.println(testReader.toString());
    }

    @Test
    public void readValuesComplicated() throws InvalidConfigurationException {
        input = "label:\n" +
                "  label1:  asdf\n" +
                "  label1-1: false\n" +
                "    oceanman:   unit\n" +
                "   yml: uiop\n" +
                "label2: true";
        Config testReader = new Config(testConfig);
        testReader.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("asdf", testReader.get("label.label1"));
        assertEquals("false", testReader.get("label.label1-1"));
        assertEquals("unit", testReader.get("label.label1-1.oceanman"));
        assertEquals("uiop", testReader.get("label.label1-1.yml"));
        assertEquals("true", testReader.get("label2"));
        System.out.println(testReader.toString());
    }

    @Test
    public void readValuesAndComments() throws InvalidConfigurationException {
        input = "### IMPORTANT: DO NOT CHANGE THE FOLLOWING LINE UNLESS YOU KNOW WHAT YOU ARE DOING ###\n" +
                "version-hash: D84BDB34D4EEEF4034D77E5403F850E35BC4A51B1143E3A83510E1AAAD839748\n" +
                "\n" +
                "game-defaults: # asdf\n" +
                "  nether: true\n" +
                "  end: true\n" +
                "  asdf # uip\n" +
                "\n" +
                "countdown:\n" +
                "  default: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,30,60,300,600,900,1200,1800,3600\n" +
                "  lobby: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,15,30,45,60,120,180,240,300,600\n" +
                "  chunk: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10" +
                "";
        Config testReader = new Config(testConfig);
        testReader.load(new ByteArrayInputStream(input.getBytes()));
        System.out.println(testReader.toString());
        Map<Integer, String> nonKeyLines = new LinkedHashMap<>(testReader.getNonKeyLocs());
        for (int i : nonKeyLines.keySet()) {
            System.out.println("[Line " + i + "] " + nonKeyLines.get(i));
        }
    }

    @Test
    public void testSaveConfig() throws InvalidConfigurationException {
        input = "### IMPORTANT: DO NOT CHANGE THE FOLLOWING LINE UNLESS YOU KNOW WHAT YOU ARE DOING ###\n" +
                "version-hash: D84BDB34D4EEEF4034D77E5403F850E35BC4A51B1143E3A83510E1AAAD839748\n" +
                "\n" +
                "game-defaults: # asdf\n" +
                "  nether: true\n" +
                "  end: true\n" +
                "  asdf # uip\n" +
                "\n" +
                "countdown:\n" +
                "  default: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,30,60,300,600,900,1200,1800,3600\n" +
                "  lobby: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,15,30,45,60,120,180,240,300,600\n" +
                "  chunk: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10\n" +
                "tier1:\n" +
                "  tier2:\n" +
                "    tier3: qwerty";
        Config testReader = new Config(testConfig);
        testReader.load(new ByteArrayInputStream(input.getBytes()));
        assertTrue(testReader.saveConfig());
        try {
            Scanner scan = new Scanner(testConfig);
            StringBuilder output = new StringBuilder();
            while (scan.hasNext()) {
                output.append(scan.nextLine());
                if (scan.hasNext()) output.append("\n");
            }
            scan.close();
            assertEquals(input, output.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNonexistNewValue() {
        input = "### IMPORTANT: DO NOT CHANGE THE FOLLOWING LINE UNLESS YOU KNOW WHAT YOU ARE DOING ###\n" +
                "version-hash: D84BDB34D4EEEF4034D77E5403F850E35BC4A51B1143E3A83510E1AAAD839748\n" +
                "\n" +
                "game-defaults: # asdf\n" +
                "  nether: true\n" +
                "  end: true\n" +
                "  asdf # uip\n" +
                "\n" +
                "countdown:\n" +
                "  default: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,30,60,300,600,900,1200,1800,3600\n" +
                "  lobby: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,15,30,45,60,120,180,240,300,600\n" +
                "  chunk: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10\n" +
                "tier1:\n" +
                "  tier2:\n" +
                "    tier3: qwerty";
        Config testReader = new Config(testConfig);
        testReader.readValues(new ByteArrayInputStream(input.getBytes()));
        assertFalse(testReader.set("not-exist", "beeblebrox"));
        assertNull(testReader.get("not-exist"));
    }

    @Test
    public void testSetNewValue() {
        input = "### IMPORTANT: DO NOT CHANGE THE FOLLOWING LINE UNLESS YOU KNOW WHAT YOU ARE DOING ###\n" +
                "version-hash: D84BDB34D4EEEF4034D77E5403F850E35BC4A51B1143E3A83510E1AAAD839748\n" +
                "\n" +
                "game-defaults: # asdf\n" +
                "  nether: true\n" +
                "  end: true\n" +
                "  asdf # uip\n" +
                "\n" +
                "countdown:\n" +
                "  default: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,30,60,300,600,900,1200,1800,3600\n" +
                "  lobby: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,15,30,45,60,120,180,240,300,600\n" +
                "  chunk: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10\n" +
                "tier1:\n" +
                "  tier2:\n" +
                "    tier3: qwerty";
        Config testReader = new Config(testConfig);
        testReader.readValues(new ByteArrayInputStream(input.getBytes()));
        assertTrue(testReader.set("tier1.tier2", "hello"));
        assertEquals("hello", testReader.get("tier1.tier2"));
    }

    @Test
    public void testSaveConfigAfterNewValue() {
        input = "### IMPORTANT: DO NOT CHANGE THE FOLLOWING LINE UNLESS YOU KNOW WHAT YOU ARE DOING ###\n" +
                "version-hash: D84BDB34D4EEEF4034D77E5403F850E35BC4A51B1143E3A83510E1AAAD839748\n" +
                "\n" +
                "game-defaults: # asdf\n" +
                "  nether: true\n" +
                "  end: true\n" +
                "  asdf # uip\n" +
                "\n" +
                "countdown:\n" +
                "  default: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,30,60,300,600,900,1200,1800,3600\n" +
                "  lobby: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,15,30,45,60,120,180,240,300,600\n" +
                "  chunk: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10\n" +
                "tier1:\n" +
                "  tier2:\n" +
                "    tier3: qwerty";
        String expected_output =
                "### IMPORTANT: DO NOT CHANGE THE FOLLOWING LINE UNLESS YOU KNOW WHAT YOU ARE DOING ###\n" +
                "version-hash: D84BDB34D4EEEF4034D77E5403F850E35BC4A51B1143E3A83510E1AAAD839748\n" +
                "\n" +
                "game-defaults: # asdf\n" +
                "  nether: true\n" +
                "  end: true\n" +
                "  asdf # uip\n" +
                "\n" +
                "countdown:\n" +
                "  default: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,30,60,300,600,900,1200,1800,3600\n" +
                "  lobby: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10,15,30,45,60,120,180,240,300,600\n" +
                "  chunk: +1,+2,+3,+4,+5,+6,+7,+8,+9,+10\n" +
                "tier1:\n" +
                "  tier2: hello\n" +
                "    tier3: qwerty";
        Config testReader = new Config(testConfig);
        testReader.readValues(new ByteArrayInputStream(input.getBytes()));
        assertTrue(testReader.set("tier1.tier2", "hello"));
        assertEquals("hello", testReader.get("tier1.tier2"));
        assertTrue(testReader.saveConfig());
        try {
            Scanner scan = new Scanner(testConfig);
            StringBuilder output = new StringBuilder();
            while (scan.hasNext()) {
                output.append(scan.nextLine());
                if (scan.hasNext()) output.append("\n");
            }
            scan.close();
            assertEquals(expected_output, output.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}