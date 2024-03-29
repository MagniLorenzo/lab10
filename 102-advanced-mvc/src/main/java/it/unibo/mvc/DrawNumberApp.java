package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import it.unibo.mvc.Configuration.Builder;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int ATTEMPTS = 10;

    private final DrawNumber model;
    private final List<DrawNumberView> views;

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
        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
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
        final String FILE_NAME = "/Users/lorenzomagni/Università/2023-2024/Programmazione ad oggetti/Laboratorio/Esercizi/Lab 10/lab10/102-advanced-mvc/src/main/resources/config.yml";
        Optional<Integer> min = Optional.empty();
        Optional<Integer> max = Optional.empty();
        Optional<Integer> attempts = Optional.empty();
        String str;
        final Builder builder = new Builder();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_NAME)))) {
            while ((str = in.readLine()) != null) {
                final StringTokenizer tokenizer = new StringTokenizer(str);
                switch (tokenizer.nextToken()) {
                    case "minimum":
                        min = Optional.of(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "maximum":
                        max = Optional.of(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    case "attempts":
                        attempts = Optional.of(Integer.parseInt(tokenizer.nextToken()));
                        break;
                    default:
                        throw new IOException("Error reading the configuration file");
                }
            }
            if (min.isPresent()) {
                builder.setMin(min.get());
            }
            if (max.isPresent()) {
                builder.setMax(max.get());
            }
            if (attempts.isPresent()) {
                builder.setAttempts(attempts.get());
            }

        } catch (Exception e) {
            for (final DrawNumberView view : views) {
                view.displayError(e.getMessage());
            }
        }
        return builder.build();
    }

    /**
     * @param args
     *             ignored
     * @throws FileNotFoundException
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
