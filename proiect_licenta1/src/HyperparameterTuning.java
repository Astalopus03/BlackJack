import java.io.*;
import java.util.*;

public class HyperparameterTuning {
    private static final int EPISODES_PER_COMBINATION = 20000; // Redus de la 100000
    private static final String RESULTS_FILE = "hyperparameter_results.csv";

    // Intervalele pentru parametri extinse
    private static final double[] EPSILON_VALUES = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
    private static final double[] LEARNING_RATE_VALUES = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
    private static final double[] DISCOUNT_FACTOR_VALUES = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

    // Adăugăm un contor pentru progres
    private int totalCombinations;
    private int currentCombination;

    private List<HyperparameterResult> results;

    public HyperparameterTuning() {
        this.results = new ArrayList<>();
        this.totalCombinations = EPSILON_VALUES.length *
                LEARNING_RATE_VALUES.length *
                DISCOUNT_FACTOR_VALUES.length;
        this.currentCombination = 0;
    }

    public void runTuning() {
        System.out.println("Starting hyperparameter tuning...");
        System.out.printf("Total combinations to test: %d\n", totalCombinations);
        System.out.printf("Episodes per combination: %d\n", EPISODES_PER_COMBINATION);
        System.out.printf("Total games to be played: %d\n", totalCombinations * EPISODES_PER_COMBINATION);

        // Deschidem fișierul CSV pentru rezultate
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULTS_FILE))) {
            // Scriem header-ul
            writer.println("Epsilon,LearningRate,DiscountFactor,WinRate,DealerWinRate,TieRate,TotalGames");

            // Testăm toate combinațiile posibile
            for (double epsilon : EPSILON_VALUES) {
                for (double learningRate : LEARNING_RATE_VALUES) {
                    for (double discountFactor : DISCOUNT_FACTOR_VALUES) {
                        currentCombination++;
                        System.out.printf("\nTesting combination %d/%d (%.1f%%)\n",
                                currentCombination, totalCombinations,
                                (double)currentCombination/totalCombinations * 100);
                        System.out.printf("Parameters: epsilon=%.2f, learningRate=%.2f, discountFactor=%.2f\n",
                                epsilon, learningRate, discountFactor);

                        // Creăm un nou joc cu parametrii actuali
                        Game game = new Game(epsilon, learningRate, discountFactor);
                        game.playMultipleGames(EPISODES_PER_COMBINATION);

                        // Obținem statisticile
                        double[] rates = game.getRecentRates();
                        HyperparameterResult result = new HyperparameterResult(
                                epsilon, learningRate, discountFactor,
                                rates[0], rates[1], rates[2], EPISODES_PER_COMBINATION
                        );
                        results.add(result);

                        // Scriem rezultatul în CSV
                        writer.printf("%.2f,%.2f,%.2f,%.4f,%.4f,%.4f,%d\n",
                                epsilon, learningRate, discountFactor,
                                rates[0], rates[1], rates[2], EPISODES_PER_COMBINATION);
                        writer.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to results file: " + e.getMessage());
        }

        // Afișăm cea mai bună combinație
        HyperparameterResult bestResult = findBestResult();
        System.out.println("\nBest hyperparameter combination found:");
        System.out.printf("Epsilon: %.2f\n", bestResult.epsilon);
        System.out.printf("Learning Rate: %.2f\n", bestResult.learningRate);
        System.out.printf("Discount Factor: %.2f\n", bestResult.discountFactor);
        System.out.printf("Win Rate: %.2f%%\n", bestResult.winRate * 100);
        System.out.printf("Dealer Win Rate: %.2f%%\n", bestResult.dealerWinRate * 100);
        System.out.printf("Tie Rate: %.2f%%\n", bestResult.tieRate * 100);
    }

    private HyperparameterResult findBestResult() {
        return results.stream()
                .max(Comparator.comparingDouble(r -> r.winRate))
                .orElseThrow(() -> new RuntimeException("No results found"));
    }

    private static class HyperparameterResult {
        double epsilon;
        double learningRate;
        double discountFactor;
        double winRate;
        double dealerWinRate;
        double tieRate;
        int totalGames;

        public HyperparameterResult(double epsilon, double learningRate, double discountFactor,
                                    double winRate, double dealerWinRate, double tieRate, int totalGames) {
            this.epsilon = epsilon;
            this.learningRate = learningRate;
            this.discountFactor = discountFactor;
            this.winRate = winRate;
            this.dealerWinRate = dealerWinRate;
            this.tieRate = tieRate;
            this.totalGames = totalGames;
        }
    }

    public static void main(String[] args) {
        HyperparameterTuning tuning = new HyperparameterTuning();
        tuning.runTuning();
    }
} 