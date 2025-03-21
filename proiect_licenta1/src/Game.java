import java.io.FileWriter;
import java.io.IOException;

public class Game {
    private Deck deck;
    private AIPlayer aiPlayer;
    private Dealer dealer;
    private int aiWins;
    private int dealerWins;
    private int ties;
    private static final int TRACKING_SIZE = 10000; // Numărul de runde pentru care calculăm winrate-ul
    private int[] recentResults; // 1 pentru victorie AI, 0 pentru egalitate, -1 pentru victorie dealer
    private int currentIndex;

    private String lastState; // Track the last state before AI's final action
    private boolean lastAction; // Track the last action (hit=true, stand=false)

    // Adăugăm o structură pentru a păstra istoricul detaliat
    private class GameRecord {
        int initialHandValue;
        boolean hasAce;
        int numCards;
        int finalHandValue;
        int dealerVisibleCard;
        int dealerFinalValue;
        int numHits;
        boolean finalAction; // true pentru hit, false pentru stand
        int result; // 1 pentru victorie, -1 pentru înfrângere, 0 pentru egalitate
        String resultType; // "normal", "blackjack", "bust"
    }

    private GameRecord[] gameHistory;
    private int historyIndex;

    public Game() {
        deck = new Deck();
        aiPlayer = new AIPlayer();
        dealer = new Dealer();
        aiWins = 0;
        dealerWins = 0;
        ties = 0;
        recentResults = new int[TRACKING_SIZE];
        gameHistory = new GameRecord[TRACKING_SIZE];
        currentIndex = 0;
        historyIndex = 0;
    }

    private void dealInitialCards() {
        System.out.println("\n=== Starting New Round ===");
        aiPlayer.receiveCard(deck.dealCard());
        aiPlayer.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());

        System.out.println("\nInitial Hands:");
        System.out.println("AI's Hand:");
        aiPlayer.showHand();
        System.out.println("\nDealer's Hand:");
        dealer.showInitialHand();
        System.out.println("------------------------");
    }

    private void aiTurn() {
        System.out.println("\nAI's Turn:");
        while (true) {
            int dealerVisibleCard = dealer.getVisibleCard().getValue();
            lastState = aiPlayer.getState(dealerVisibleCard);
            lastAction = aiPlayer.decideToHit(dealerVisibleCard);

            if (lastAction) {
                Card newCard = deck.dealCard();
                aiPlayer.receiveCard(newCard);
                System.out.println("\nAI hits and receives: " + newCard);
                aiPlayer.showHand();
                System.out.println("------------------------");

                // Calculate intermediate reward for this action
                int newValue = aiPlayer.getHandValue();
                String nextState = aiPlayer.getState(dealerVisibleCard);
                double intermediateReward = aiPlayer.calculateIntermediateReward(true, newValue);
                aiPlayer.updateQTable(lastState, lastAction, intermediateReward, nextState);

                if (newValue > 21) {
                    System.out.println("AI Busts!");
                    break;
                }
                lastState = nextState;
            } else {
                System.out.println("\nAI Stands.");
                System.out.println("AI's Final Hand:");
                aiPlayer.showHand();
                System.out.println("------------------------");
                break;
            }
        }
    }

    private void dealerTurn() {
        System.out.println("\nDealer's Turn:");
        System.out.println("Revealing hidden card: " + dealer.getHand().getCards().get(1));
        dealer.showHand();

        while (dealer.getHandValue() < 17) {
            Card newCard = deck.dealCard();
            dealer.receiveCard(newCard);
            System.out.println("\nDealer hits and receives: " + newCard);
            dealer.showHand();
            System.out.println("------------------------");
        }

        if (dealer.getHandValue() > 21) {
            System.out.println("Dealer Busts!");
        } else {
            System.out.println("Dealer Stands.");
        }
    }

    private void updateRecentResults(int result) {
        recentResults[currentIndex] = result;
        currentIndex = (currentIndex + 1) % TRACKING_SIZE;
    }

    private void recordGame() {
        GameRecord record = new GameRecord();
        Hand aiHand = aiPlayer.getHand();
        Hand dealerHand = dealer.getHand();

        // Calculăm valorile inițiale
        record.initialHandValue = aiHand.getCards().get(0).getValue() + aiHand.getCards().get(1).getValue();
        record.hasAce = aiHand.getCards().stream().anyMatch(card -> card.getRank() == Rank.ACE);
        record.numCards = aiHand.getCards().size();
        record.finalHandValue = aiHand.getTotalValue();
        record.dealerVisibleCard = dealerHand.getCards().get(0).getValue();
        record.dealerFinalValue = dealerHand.getTotalValue();

        // Calculăm numărul de hit-uri
        record.numHits = record.numCards - 2; // Scădem cărțile inițiale
        record.finalAction = lastAction;

        // Determinăm rezultatul și tipul
        int aiValue = aiHand.getTotalValue();
        int dealerValue = dealerHand.getTotalValue();

        // Verificăm blackjack (As + carte de 10)
        boolean aiHasBlackjack = aiValue == 21 && record.numCards == 2 &&
                record.hasAce && aiHand.getCards().stream().anyMatch(card -> card.getValue() == 10);
        boolean dealerHasBlackjack = dealerValue == 21 && dealerHand.getCards().size() == 2 &&
                dealerHand.getCards().stream().anyMatch(card -> card.getRank() == Rank.ACE) &&
                dealerHand.getCards().stream().anyMatch(card -> card.getValue() == 10);

        if (aiValue > 21) {
            record.result = -1;
            record.resultType = "bust";
        } else if (dealerValue > 21) {
            record.result = 1;
            record.resultType = "normal";
        } else if (aiHasBlackjack) {
            record.result = 1;
            record.resultType = "blackjack";
        } else if (dealerHasBlackjack) {
            record.result = -1;
            record.resultType = "normal";
        } else if (aiValue > dealerValue) {
            record.result = 1;
            record.resultType = "normal";
        } else if (dealerValue > aiValue) {
            record.result = -1;
            record.resultType = "normal";
        } else {
            record.result = 0;
            record.resultType = "tie";
        }

        gameHistory[historyIndex] = record;
        historyIndex = (historyIndex + 1) % TRACKING_SIZE;
    }

    private void exportToCSV() {
        try (FileWriter writer = new FileWriter("blackjack_stats.csv")) {
            // Scriem header-ul
            writer.write("Initial Hand Value,Has Ace,Number of Cards,Final Hand Value," +
                    "Dealer Visible Card,Dealer Final Value,Number of Hits,Final Action," +
                    "Result,Result Type\n");

            // Scriem datele
            for (int i = 0; i < TRACKING_SIZE; i++) {
                GameRecord record = gameHistory[i];
                if (record != null) {
                    writer.write(String.format("%d,%b,%d,%d,%d,%d,%d,%b,%d,%s\n",
                            record.initialHandValue,
                            record.hasAce,
                            record.numCards,
                            record.finalHandValue,
                            record.dealerVisibleCard,
                            record.dealerFinalValue,
                            record.numHits,
                            record.finalAction,
                            record.result,
                            record.resultType));
                }
            }
            System.out.println("Statistics exported to blackjack_stats.csv");
        } catch (IOException e) {
            System.out.println("Error exporting statistics: " + e.getMessage());
        }
    }

    private void determineOutcome() {
        System.out.println("\n=== Round Results ===");
        System.out.println("AI's Final Hand:");
        aiPlayer.showHand();
        System.out.println("\nDealer's Final Hand:");
        dealer.showHand();
        System.out.println("------------------------");

        int aiValue = aiPlayer.getHandValue();
        int dealerValue = dealer.getHandValue();
        double reward = 0;
        int result = 0;

        // Determine the result
        if (aiValue > 21) {
            dealerWins++;
            reward = -1.0;
            result = -1;
            System.out.println("Result: AI Busts - Dealer Wins");
        } else if (dealerValue > 21) {
            aiWins++;
            reward = 1.0;
            result = 1;
            System.out.println("Result: Dealer Busts - AI Wins");
        } else if (aiValue == 21 && aiPlayer.getHand().getCards().size() == 2) {
            aiWins++;
            reward = 1.5;
            result = 1;
            System.out.println("Result: AI Wins with Blackjack!");
        } else if (dealerValue == 21 && dealer.getHand().getCards().size() == 2) {
            dealerWins++;
            reward = -1.0;
            result = -1;
            System.out.println("Result: Dealer Wins with Blackjack!");
        } else if (aiValue > dealerValue) {
            aiWins++;
            reward = 1.0;
            result = 1;
            System.out.println("Result: AI Wins");
        } else if (dealerValue > aiValue) {
            dealerWins++;
            reward = -1.0;
            result = -1;
            System.out.println("Result: Dealer Wins");
        } else {
            ties++;
            reward = 0.0;
            result = 0;
            System.out.println("Result: Tie");
        }
        System.out.println("========================\n");

        updateRecentResults(result);
        String nextState = "terminal";
        aiPlayer.updateQTable(lastState, lastAction, reward, nextState);

        // Adăugăm înregistrarea meciului
        recordGame();
    }

    private void playRound() {
        aiPlayer.resetHand();
        dealer.resetHand();
        deck.reset();
        dealInitialCards();
        aiTurn();
        dealerTurn();
        determineOutcome();
    }

    public void playMultipleRounds(int numberOfRounds) {
        System.out.println("Starting " + numberOfRounds + " rounds of Blackjack...\n");
        for (int i = 1; i <= numberOfRounds; i++) {
            System.out.println("=== Round " + i + " of " + numberOfRounds + " ===");
            playRound();

            // Afișăm winrate-ul la fiecare 1000 de runde
            if (i % 1000 == 0) {
                calculateWinRate();
            }
        }
        showResultsSummary();
        calculateWinRate();
        exportToCSV(); // Exportăm statisticile la final
    }

    private void showResultsSummary() {
        System.out.println("\n=== Final Game Summary ===");
        System.out.println("Total Rounds Played: " + (aiWins + dealerWins + ties));
        System.out.println("AI Wins: " + aiWins);
        System.out.println("Dealer Wins: " + dealerWins);
        System.out.println("Ties: " + ties);
        System.out.println("------------------------");

        // Calculăm statisticile finale
        int totalRounds = aiWins + dealerWins + ties;
        double finalWinRate = (double) aiWins / totalRounds * 100;
        double finalDealerWinRate = (double) dealerWins / totalRounds * 100;
        double finalTieRate = (double) ties / totalRounds * 100;

        System.out.printf("Final Win Rate: %.2f%%\n", finalWinRate);
        System.out.printf("Final Dealer Win Rate: %.2f%%\n", finalDealerWinRate);
        System.out.printf("Final Tie Rate: %.2f%%\n", finalTieRate);
        System.out.println("========================\n");
    }

    private void calculateWinRate() {
        int aiWinsInRecent = 0;
        int dealerWinsInRecent = 0;
        int tiesInRecent = 0;

        // Calculăm statisticile pentru ultimele 10.000 de runde
        for (int i = 0; i < TRACKING_SIZE; i++) {
            if (recentResults[i] == 1) {
                aiWinsInRecent++;
            } else if (recentResults[i] == -1) {
                dealerWinsInRecent++;
            } else {
                tiesInRecent++;
            }
        }

        double winRate = (double) aiWinsInRecent / TRACKING_SIZE * 100;
        double dealerWinRate = (double) dealerWinsInRecent / TRACKING_SIZE * 100;
        double tieRate = (double) tiesInRecent / TRACKING_SIZE * 100;

        System.out.println("\n=== Recent Statistics (last " + TRACKING_SIZE + " rounds) ===");
        System.out.printf("Recent Win Rate: %.2f%%\n", winRate);
        System.out.printf("Recent Dealer Win Rate: %.2f%%\n", dealerWinRate);
        System.out.printf("Recent Tie Rate: %.2f%%\n", tieRate);
        System.out.println("------------------------");
        System.out.println("Recent Total Rounds: " + TRACKING_SIZE);
        System.out.println("Recent AI Wins: " + aiWinsInRecent);
        System.out.println("Recent Dealer Wins: " + dealerWinsInRecent);
        System.out.println("Recent Ties: " + tiesInRecent);
        System.out.println("========================\n");
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.playMultipleRounds(1000000);
    }
}
