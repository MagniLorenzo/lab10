package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import it.unibo.mvc.Configuration.Builder;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    /*
     * private static final int MIN = 0;
     * private static final int MAX = 100;
     * private static final int ATTEMPTS = 10;
     */

    private final DrawNumber model;
    private final List<DrawNumberView> views;
    private final Configuration configuration;
    public static final String SEP = File.separator;

    /**
     * @param views
     *              the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view : views) {
            view.setObserver(this);
            view.start();
        }
        this.configuration = getConfiguration();
        this.model = new DrawNumberImpl(configuration.getMin(), configuration.getMax(), configuration.getAttempts());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view : views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view : views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    private Configuration getConfiguration() {
        final String FILE_NAME = System.getProperty("user.dir") + SEP + "src" + SEP + "main" + SEP + "resources" + SEP
                + "config.yml";
        String str;
        final Builder builder = new Builder();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_NAME)))) {
            while ((str = in.readLine()) != null && !str.isEmpty()) {
                final StringTokenizer tokenizer = new StringTokenizer(str);
                switch (tokenizer.nextToken()) {
                    case "minimum:":
                        builder.setMin(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "maximum:":
                        builder.setMax(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "attempts:":
                        builder.setAttempts(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    default:
                        throw new IOException("Unexpected parameters in the configuration file");
                }
            }
        } catch (IOException e) {
            for (final DrawNumberView view : views) {
                view.displayError("Error reading the configuration file: " + e.getMessage());
            }
            quit();
        } catch (NoSuchElementException e1) {
            for (final DrawNumberView view : views) {
                view.displayError("Error in configuration file: " + e1.getMessage());
            }
            quit();
        } catch (NumberFormatException e2) {
            for (final DrawNumberView view : views) {
                view.displayError("Configuration file entries only accept integers as parameters: " + e2.getMessage());
            }
            quit();
        }
        return builder.build();
    }

    /**
     * @param args
     *             ignored
     * @throws FileNotFoundException
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(),
                new DrawNumberViewImpl(),
                new PrintStreamView(System.out),
                new PrintStreamView("output.log"));
    }

}
