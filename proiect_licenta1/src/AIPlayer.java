import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AIPlayer {
    private Hand hand;
    private Map<String, Double[]> qTable;
    private double epsilon = 0.1; // Exploration rate
    private double learningRate = 0.1;//influences how much new information overwrites old information in the Q-table.;
    private double discountFactor = 0.9; //determines how much future rewards are valued compared to immediate rewards.
    private Random random;

    public AIPlayer() {
        hand = new Hand();
        qTable = new HashMap<>();
        random = new Random();
    }

    public void receiveCard(Card card) {
        hand.addCard(card);
    }

    // Calculate state based on hand value, dealer's visible card, and ace presence
    public String getState(int dealerVisibleCard) {
        int value = getHandValue();
        boolean hasAce = hand.getCards().stream().anyMatch(card -> card.getRank() == Rank.ACE);
        int numCards = hand.getCards().size();
        boolean hasPair = hand.getCards().size() == 2 &&
                hand.getCards().get(0).getRank() == hand.getCards().get(1).getRank();
        boolean dealerHasAce = dealerVisibleCard == 1;

        return String.format("%d-%d-%b-%d-%b-%b",
                value, dealerVisibleCard, hasAce, numCards, hasPair, dealerHasAce);
    }

    // Choose to hit or stand based on Q-values and epsilon-greedy strategy
    public boolean decideToHit(int dealerVisibleCard) {
        String state = getState(dealerVisibleCard);
        qTable.putIfAbsent(state, new Double[]{0.0, 0.0}); // Initialize state if absent

        boolean decision;
        if (random.nextDouble() < epsilon) {
            // Explore: Random choice
            decision = random.nextBoolean();
            System.out.println("AI is exploring and decides to " + (decision ? "hit." : "stand."));
            System.out.println(hand);
        } else {
            // Exploit: Choose action with max Q-value
            decision = qTable.get(state)[0] > qTable.get(state)[1];
            System.out.println("AI is exploiting and decides to " + (decision ? "hit." : "stand."));
            System.out.println(hand);
        }

        return decision;
    }


    // Update Q-table based on action taken and reward received
    public void updateQTable(String state, boolean hit, double reward, String nextState) {
        int actionIndex = hit ? 0 : 1;
        qTable.putIfAbsent(nextState, new Double[]{0.0, 0.0});

        double oldQValue = qTable.get(state)[actionIndex];
        double nextMaxQ = Math.max(qTable.get(nextState)[0], qTable.get(nextState)[1]);
        double updatedQValue = oldQValue + learningRate * (reward + discountFactor * nextMaxQ - oldQValue);

        qTable.get(state)[actionIndex] = updatedQValue;
    }

    public int getHandValue() {
        return hand.getTotalValue();
    }

    public void resetHand() {
        hand.clear();
    }
    public void showHand() {
        System.out.println(hand);
        System.out.println("Total value: " + hand.getTotalValue());
    }

    public double calculateIntermediateReward(boolean hit, int newValue) {
        if (newValue == 21) return 0.5;  // Bonus pentru blackjack
        if (newValue > 21) return -0.5;  // Penalizare pentru bust
        if (newValue >= 17 && newValue <= 20) return 0.3;  // Bonus pentru mână puternică
        return 0;
    }

    public Hand getHand() {
        return hand;
    }
}
