public class Game {
    private Deck deck;
    private AIPlayer aiPlayer;
    private Dealer dealer;
    private int aiWins;
    private int dealerWins;
    private int ties;


    private String lastState; // Track the last state before AI’s final action
    private boolean lastAction; // Track the last action (hit=true, stand=false)


    public Game() {
        deck = new Deck();
        aiPlayer = new AIPlayer();
        dealer = new Dealer();
        aiWins = 0;
        dealerWins = 0;
        ties = 0;
    }

    private void dealInitialCards() {
        aiPlayer.receiveCard(deck.dealCard());
        aiPlayer.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());

        // Display initial hands
        System.out.println("AI's Initial Hand:");
        aiPlayer.showHand();
        System.out.println("Dealer's Initial Hand (one card hidden):");
        dealer.showInitialHand();
    }

    private void aiTurn() {
        while (true) {
            int dealerVisibleCard = dealer.getVisibleCard().getValue();
            lastState = aiPlayer.getState(dealerVisibleCard); // Get current state
            lastAction = aiPlayer.decideToHit(dealerVisibleCard); // Choose action

            if (lastAction) {
                aiPlayer.receiveCard(deck.dealCard());
                if (aiPlayer.getHandValue() > 21) break; // AI busts
            } else {
                break; // AI stands
            }
        }
    }

    private void dealerTurn() {
        System.out.println("Dealer's Turn:");
        dealer.play(deck);
    }

    private void determineOutcome() {
        int aiValue = aiPlayer.getHandValue();
        int dealerValue = dealer.getHandValue();

        double reward = 0;

        System.out.println("Final Hands:");
        System.out.println("AI's Hand:");
        aiPlayer.showHand();
        System.out.println("Dealer's Hand:");
        dealer.showHand();

        // Determine the result
        if (aiValue > 21) {
            dealerWins++;
            reward = -1; // Penalty for busting
            System.out.println("Result: Dealer Wins");
        } else if (dealerValue > 21|| aiValue > dealerValue) {
            aiWins++;
            reward = 1; // Reward for winning
            System.out.println("Result: AI Wins");
        } else if (aiValue > dealerValue) {
            aiWins++;
            System.out.println("Result: AI Wins");
        } else if (dealerValue > aiValue) {
            dealerWins++;
            reward = -1; // Penalty for losing
            System.out.println("Result: Dealer Wins");
        } else {
            ties++;
            reward = 0; // Neutral reward for a tie
            System.out.println("Result: Tie");
        }
        // Update Q-table based on the final outcome
        String nextState = "terminal"; // No next state since the game round ended
        aiPlayer.updateQTable(lastState, lastAction, reward, nextState);
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
        for (int i = 1; i <= numberOfRounds; i++) {
            System.out.println("\n--- Round " + i + " ---");
            playRound();
        }
        showResultsSummary();
        calculateWinRate();
    }

    private void showResultsSummary() {
        System.out.println("\n--- Game Summary ---");
        System.out.println("AI Wins: " + aiWins);
        System.out.println("Dealer Wins: " + dealerWins);
        System.out.println("Ties: " + ties);
    }

    private void calculateWinRate() {
        int totalGames = aiWins + dealerWins + ties;
        double winRate = totalGames > 0 ? (double) aiWins / totalGames * 100 : 0;
        System.out.printf("AI Win Rate: %.2f%%\n", winRate);
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.playMultipleRounds(100000);
    }
}
