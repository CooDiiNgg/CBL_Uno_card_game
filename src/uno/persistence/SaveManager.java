package uno.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uno.model.GameState;
import uno.util.Constants;

public class SaveManager {
    public static String save(GameState gameState, String saveName) throws SaveException {
        if (gameState == null) {
            throw new SaveException("There is no game to save");
        }
        File file = getSaveFile(saveName);
        byte[] serialized = GameSerializer.serialize(SaveState.fromGameState(gameState));
        byte[] compressed = Compressor.compress(serialized);
        try {
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), compressed);
        } catch (IOException e) {
            throw new SaveException("Could not write " + file.getPath(), e);
        }
        return file.getPath() + " (" + serialized.length + " bytes serialized, " + compressed.length + " bytes compressed)";
    }

    public static GameState load(String saveName) throws SaveException {
        File file = getSaveFile(saveName);
        if (!file.isFile()) {
            throw new SaveException("Save not found: " + saveName);
        }
        byte[] compressed;
        try {
            compressed = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new SaveException("Could not read " + file.getPath(), e);
        }
        byte[] serialized = Compressor.decompress(compressed);
        SaveState saveState = GameSerializer.deserialize(serialized);
        try {
            return saveState.toGameState();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new SaveException("Save data is invalid: " + e.getMessage(), e);
        }
    }

    public static String[] listSaves() {
        File[] files = new File(Constants.SAVE_DIRECTORY).listFiles();
        if (files == null) {
            return new String[0];
        }
        List<String> saveNames = new ArrayList<>();
        for (File file : files) {
            String fileName = file.getName();
            if (file.isFile() && fileName.endsWith(Constants.SAVE_EXTENSION)) {
                saveNames.add(fileName.substring(0, fileName.length() - Constants.SAVE_EXTENSION.length()));
            }
        }
        Collections.sort(saveNames);
        return saveNames.toArray(new String[0]);
    }

    private static File getSaveFile(String saveName) throws SaveException {
        if (saveName == null || !saveName.matches("[A-Za-z0-9_-]+")) {
            throw new SaveException("Save names can only contain letters, numbers, '-' and '_'");
        }
        return new File(Constants.SAVE_DIRECTORY, saveName + Constants.SAVE_EXTENSION);
    }
}
