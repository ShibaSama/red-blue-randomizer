package redbluerandomizer;

public class Test {

  /** @param args */
  public static String inputFile = "";

  public static String outputFile = "";

  /** @param args */
  public static void main(String[] args) {
    RedBlueRandomizer br = new RedBlueRandomizer();
    br.readRom(inputFile);
    br.setPlayerStartersToggle(true);
    br.setTitleScreenToggle(true);
    br.setTrainersToggle(true);
    br.setWildAreasToggle(true);
    br.randomize();

    RomPrinter printer = new RomPrinter(br);
    printer.printROM();
  }
}
