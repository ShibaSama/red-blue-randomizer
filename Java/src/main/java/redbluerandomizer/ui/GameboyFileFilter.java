package redbluerandomizer.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class GameboyFileFilter extends FileFilter {

  private static final String DESCRIPTION = ".gb ROM files";
  private static final String REGEX = ".*\\.gb$";

  public boolean accept(File file) {

    // ensure selection isn't a directory
    if (file.isDirectory()) {
      return true;
    }

    // determine if filename is an acceptable .gb file
    return file.getName().toLowerCase().matches(REGEX);
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }
}
