package redbluerandomizer;

import static redbluerandomizer.Constants.BYTE_MASK;
import static redbluerandomizer.Constants.OFFSET_PLAYER_STARTERS;
import static redbluerandomizer.Constants.OFFSET_TITLE_SCREEN_PKMN;
import static redbluerandomizer.Constants.OFFSET_TRAINER_PKMN_END;
import static redbluerandomizer.Constants.OFFSET_TRAINER_PKMN_START;
import static redbluerandomizer.Constants.OFFSET_WILD_AREAS;

import java.util.Map;

public class RomPrinter {

  private byte[] rom;
  private Map<Integer, String> pokemonNameMap;

  private RedBlueRandomizer r;

  public RomPrinter(RedBlueRandomizer randomizer) {
    this.r = randomizer;
    this.rom = r.getRom();
    pokemonNameMap = r.getPokemonNameMap();
  }

  /** Prints the relevant data in the ROM. */
  public void printROM() {
    int offset;

    // intro pokemon
    System.out.println("\nTitle Screen Pokemon**********************************\n");
    for (int i = 0; i < OFFSET_TITLE_SCREEN_PKMN.length; i++) {
      offset = OFFSET_TITLE_SCREEN_PKMN[i];
      printPokemon(offset, rom[offset]);
    }

    // player starters
    System.out.println("\nPlayer Starter ***************************************\n");
    for (int i = 0; i < OFFSET_PLAYER_STARTERS.length; i++) {
      offset = OFFSET_PLAYER_STARTERS[i];
      printPokemon(offset, rom[offset]);
    }

    // wild pokemon areas
    System.out.println("\nWild Pokemon Areas ***********************************");
    for (int i = 0; i < OFFSET_WILD_AREAS.length; i++) {
      System.out.println(
          "\nWild Pokemon Area " + i + "\t" + intToHexStr(OFFSET_WILD_AREAS[i]) + "\n");
      for (int j = 0; j < 20; j += 2) {
        offset = OFFSET_WILD_AREAS[i];
        printPokemon((offset + j + 1), rom[offset + j], rom[offset + j + 1]);
      }
    }

    // trainer pokemon
    System.out.println("\nTrainer Pokemon **************************************\n");
    int a = OFFSET_TRAINER_PKMN_START;
    while (a < OFFSET_TRAINER_PKMN_END) {
      System.out.println("\nOffset:" + intToHexStr(a));
      if (byteToInt(rom[a]) == 0x0 && byteToInt(rom[a + 1]) != 0xFF) {
        a = printRegularTrainer(a);
      } else {
        a = printSpecialTrainer(a);
      }
    }
  }

	/**
	 * Prints a pokemon's ROM offset location and its name.
	 *
	 * @param offset Location of the pokemon in the ROM.
	 * @param pokemonIndex The pokemon's id.
	 */
  private void printPokemon(int offset, byte pokemonIndex) {
    String output = intToHexStr(offset) + "\t";
    output += this.getPokemonName(pokemonIndex);
    System.out.println(output);
  }

	/**
	 * Print's a pokemon's ROM offset location, its level, and its name.
	 *
	 * @param offset Location of the pokemon in the ROM.
	 * @param level The pokemon's level.
	 * @param pokemonIndex The pokemon's id.
	 */
  private void printPokemon(int offset, int level, byte pokemonIndex) {
    String output = intToHexStr(offset) + "\t";
    if (("Level " + level).length() == 7) {
      output += ("Level " + level + "    ");
    } else {
      output += ("Level " + level) + "   ";
    }
    output += this.getPokemonName(pokemonIndex);
    System.out.println(output);
  }

	/**
	 * Prints the pokemon belonging to a regular trainer.
	 *
	 * @param offset Cursor location where the regular trainer's pokemon data begins.
	 * @return Cursor location after parsing the regular trainer's pokemon data.
	 */
  private int printRegularTrainer(int offset) {
    offset++;
    System.out.println("Level: " + this.byteToInt(rom[offset]));
    offset++;
    boolean loop = true;
    while (loop) {
      if (rom[offset] == 0x0) {
        loop = false;
        break;
      } else {
        printPokemon(offset, rom[offset]);
        offset++;
      }
    }
    return offset;
  }

	/**
	 * Prints the pokemon belonging to a special trainer.
	 *
	 * @param offset Cursor location where the special trainer's pokemon data begins.
	 * @return Cursor location after parsing the special trainer's pokemon data.
	 */
  private int printSpecialTrainer(int offset) {
    offset += 2;
    boolean loop = true;
    while (loop) {
      if (rom[offset] == 0x0) {
        loop = false;
        break;
      } else {
        printPokemon(offset + 1, byteToInt(rom[offset]), rom[offset + 1]);
        offset += 2;
      }
    }
    return offset;
  }

	/**
	 * Returns a pokemon's name based on its id.
	 *
	 * @param pokemonIndex A pokemon id.
	 * @return The name belonging to the pokemon id.
	 */
  public String getPokemonName(int pokemonIndex) {
    return (String) pokemonNameMap.get(pokemonIndex);
  }

	/**
	 * Returns a pokemon's name based on its id.
	 *
	 * @param pokemonIndex A pokemon id.
	 * @return The name belonging to the pokemon id.
	 */
  public String getPokemonName(byte pokemonIndex) {
    return getPokemonName(byteToInt(pokemonIndex));
  }

	/**
	 * Converts an int to a hex string.
	 *
	 * @param i An integer.
	 * @return The hex string value of the provided integer.
	 */
  private String intToHexStr(int i) {
    return String.format("0x%02X", i);
  }

	/**
	 * Converts a byte to an int.
	 *
	 * @param b A byte.
	 * @return The integer value of the byte.
	 */
  private int byteToInt(byte b) {
    return b & BYTE_MASK;
  }
}
