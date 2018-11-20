package redbluerandomizer;

import static redbluerandomizer.Constants.LEGENDARY_INDEXES;
import static redbluerandomizer.Constants.OFFSET_PLAYER_STARTERS;
import static redbluerandomizer.Constants.OFFSET_ROM_NAME_END;
import static redbluerandomizer.Constants.OFFSET_ROM_NAME_START;
import static redbluerandomizer.Constants.OFFSET_TITLE_SCREEN_PKMN;
import static redbluerandomizer.Constants.OFFSET_TRAINER_PKMN_END;
import static redbluerandomizer.Constants.OFFSET_TRAINER_PKMN_START;
import static redbluerandomizer.Constants.OFFSET_WILD_AREAS;
import static redbluerandomizer.Constants.PKMN_INDEXES;
import static redbluerandomizer.Constants.PKMN_NAMES;
import static redbluerandomizer.Constants.ROM_NAME_BLUE;
import static redbluerandomizer.Constants.ROM_NAME_RED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public class RedBlueRandomizer {

  // options
  private boolean titleScreenToggle = false;
  private boolean playerStartersToggle = false;
  private boolean wildAreasToggle = false;
  private boolean trainersToggle = false;
  private boolean oneToOneToggle = false;
  private boolean noLegendariesToggle = false;

  private Map<Integer, Integer> swapMap;
  private RandomGenerator rand = new MersenneTwister(ZonedDateTime.now().toEpochSecond());
  private byte[] rom;

  /** **************************************** */
  // Randomize
  /** **************************************** */

  // performs the randomization (duh...)
  public void randomize() {
    // setup
    swapMap = getOneToOneMap();

    int offset;
    // intro pokemon
    if (titleScreenToggle) {
      for (int i = 0; i < OFFSET_TITLE_SCREEN_PKMN.length; i++) {
        offset = OFFSET_TITLE_SCREEN_PKMN[i];
        if (oneToOneToggle) {
          rom[offset] = getReplacement(rom[offset]);
        } else {
          rom[offset] = getRandomPokemonIndex();
        }
      }
    }
    // player starters
    if (playerStartersToggle) {
      for (int i = 0; i < OFFSET_PLAYER_STARTERS.length; i++) {
        offset = OFFSET_PLAYER_STARTERS[i];
        if (oneToOneToggle) {
          rom[offset] = getReplacement(rom[offset]);
        } else {
          rom[offset] = getRandomPokemonIndex();
        }
      }
    }
    // wild pokemon areas
    if (wildAreasToggle) {
      for (int i = 0; i < OFFSET_WILD_AREAS.length; i++) {
        for (int j = 0; j < 20; j += 2) {
          offset = OFFSET_WILD_AREAS[i];
          if (oneToOneToggle) {
            rom[offset + j + 1] = getReplacement(rom[offset + j + 1]);
          } else {
            rom[offset + j + 1] = getRandomPokemonIndex();
          }
        }
      }
    }
    // pokemon trainers
    if (trainersToggle) {
      int i = OFFSET_TRAINER_PKMN_START;
      while (i < OFFSET_TRAINER_PKMN_END) {
        if (byteToInt(rom[i]) == 0x0 && byteToInt(rom[i + 1]) != 0xFF) {
          i = randomizeRegularTrainer(i);
        } else {
          i = randomizeSpecialTrainer(i);
        }
      }
    }
  }

  /** **************************************** */
  // Randomize Support Methods
  /** **************************************** */

  // randomizes a regular trainer
  private int randomizeRegularTrainer(int offset) {
    offset += 2;
    boolean loop = true;
    while (loop) {
      if (rom[offset] == 0x0) {
        loop = false;
        break;
      } else {
        if (oneToOneToggle) {
          rom[offset] = getReplacement(rom[offset]);
        } else {
          rom[offset] = getRandomPokemonIndex();
        }
        offset++;
      }
    }
    return offset;
  }

  // randomizer a special trainer
  private int randomizeSpecialTrainer(int offset) {
    offset += 2;
    boolean loop = true;
    while (loop) {
      if (rom[offset] == 0x0) {
        loop = false;
        break;
      } else {
        if (oneToOneToggle) {
          rom[offset + 1] = getReplacement(rom[offset + 1]);
        } else {
          rom[offset + 1] = getRandomPokemonIndex();
        }
        offset += 2;
      }
    }
    return offset;
  }

  // returns a random pokemon index
  private byte getRandomPokemonIndex() {
    shuffle();
    if (noLegendariesToggle) {
      int randomIndex;
      int randomPokemon;
      while (true) {
        randomIndex = rand.nextInt(PKMN_INDEXES.length);
        randomPokemon = PKMN_INDEXES[randomIndex];
        if (!isLegendaryPokemon(randomPokemon)) {
          return (byte) randomPokemon;
        }
      }
    } else {
      int randomIndex = rand.nextInt(PKMN_INDEXES.length);
      return (byte) PKMN_INDEXES[randomIndex];
    }
  }

  // progresses the RNG a random number of times to add to the randomness
  private void shuffle() {
    int loop = rand.nextInt(10);
    for (int i = 0; i < loop; i++) {
      rand.nextInt(PKMN_INDEXES.length);
    }
  }

  /** **************************************** */
  // Lookup Methods
  /** **************************************** */

  // creates a one-to-one randomization of the pokemon list
  public HashMap<Integer, Integer> getOneToOneMap() {
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    List<Integer> temp = getPokemonIndexes();
    if (noLegendariesToggle) {
      for (Integer legendaryIndex : LEGENDARY_INDEXES) {
        int i = temp.indexOf(legendaryIndex);
        temp.set(i, byteToInt(getRandomPokemonIndex()));
      }
    }

    Integer newIndex;
    for (Integer oldIndex : PKMN_INDEXES) {
      newIndex = temp.get(rand.nextInt(temp.size()));
      map.put(oldIndex, newIndex);
      temp.remove(newIndex);
    }
    return map;
  }

  // gets the replacement for a pokemon using the swap map generated
  public byte getReplacement(byte oldIndex) {
    return (byte) swapMap.get(byteToInt(oldIndex)).intValue();
  }

  // determines if a given pokemon index belongs to a legendary
  public boolean isLegendaryPokemon(int index) {
    for (int legendaryIndex : LEGENDARY_INDEXES) {
      if (index == legendaryIndex) {
        return true;
      }
    }
    return false;
  }

  // returns a list of all the pokemon's indexes
  public ArrayList<Integer> getPokemonIndexes() {
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    for (int index : PKMN_INDEXES) {
      indexes.add(index);
    }
    return indexes;
  }

  // returns a Map of all pokemon indexes and names
  public HashMap<Integer, String> getPokemonNameMap() {
    HashMap<Integer, String> nameMap = new HashMap<Integer, String>();
    for (int i = 0; i < PKMN_INDEXES.length; i++) {
      nameMap.put(PKMN_INDEXES[i], PKMN_NAMES[i]);
    }
    return nameMap;
  }

  /** **************************************** */
  // File I/O
  /** **************************************** */

  // reads in the ROM given a filepath
  public void readRom(String filePath) {
    try {
      FileInputStream stream = new FileInputStream(filePath);
      rom = new byte[stream.available()];
      stream.read(rom, 0, stream.available());
      stream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // saves the ROM to a specified filepath
  public void saveRom(String filePath) {
    try {
      FileOutputStream stream = new FileOutputStream(new File(filePath));
      stream.write(rom);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // checks the ROM's name
  public boolean isPokemonRedBlue() {
    try {
      String romName = "";
      for (int i = OFFSET_ROM_NAME_START; i < OFFSET_ROM_NAME_END; i++) {
        romName += (char) byteToInt(rom[i]);
      }
      romName = romName.trim();
      if (romName.equals(ROM_NAME_RED) || romName.equals(ROM_NAME_BLUE)) {
        return true;
      }
      return false;
    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /** **************************************** */
  // Misc.
  /** **************************************** */

  // converts a byte to an int
  private int byteToInt(byte b) {
    return b & 0xFF;
  }

  /** **************************************** */
  // Setters/Getters
  /** **************************************** */

  // toggle setters
  public void setTitleScreenToggle(boolean toggle) {
    this.titleScreenToggle = toggle;
  }

  public void setPlayerStartersToggle(boolean toggle) {
    this.playerStartersToggle = toggle;
  }

  public void setwildAreasToggle(boolean toggle) {
    this.wildAreasToggle = toggle;
  }

  public void setTrainersToggle(boolean toggle) {
    this.trainersToggle = toggle;
  }

  public void setOneToOneToggle(boolean toggle) {
    this.oneToOneToggle = toggle;
  }

  public void setNoLegendariesToggle(boolean toggle) {
    this.noLegendariesToggle = toggle;
  }

  public byte[] getRom() {
    return rom;
  }
}
