package edu.umn.amicus.config;

import edu.umn.amicus.Amicus;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Generic way to load configurations for individual classes.
 * Useful for custom Transformers/Aligners/Distillers that might have resource requirements.
 * Will load using yaml for .yml extensions, or just the whole file for .txt.
 *
 * Created by gpfinley on 10/24/16.
 */
public class ClassConfigurationLoader {

    private static Logger LOGGER = Logger.getLogger(ClassConfigurationLoader.class.getName());
    private static final Yaml yaml = new Yaml();

    public static Object load(Class clazz) throws FileNotFoundException {
        File file = new File(Amicus.CLASS_CONFIG_DIR.resolve(clazz.getName()) + ".yml");
        if (file.exists()) {
            return yaml.load(new FileInputStream(file));
        } else {
            file = new File(Amicus.CLASS_CONFIG_DIR.resolve(clazz.getName()) + ".txt");
            if (!file.exists()) {
                LOGGER.severe("No configuration file with .yml or .txt extension found for class " + clazz.getName());
                throw new FileNotFoundException();
            }
            return new Scanner(file).useDelimiter("\\Z").next();
        }
    }

}
