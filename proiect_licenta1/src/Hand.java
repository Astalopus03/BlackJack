import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    public List<Card> cards; // Stores the cards in the player's hand

    // Constructor to initialize an empty hand
    public Hand() {
        cards = new ArrayList<>();
    }

    // Method to add a card to the hand
    public void addCard(Card card) {
        cards.add(card);
    }

    // Method to calculate the total value of the hand
    public int getTotalValue() {
        int totalValue = 0;
        int aceCount = 0;

        // Calculate the total value and count Aces
        for (Card card : cards) {
            int cardValue = card.getValue();
            totalValue += cardValue;

            // Keep track of how many Aces are in the hand
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        // Adjust for Aces: Count them as 11 if it doesn't cause a bust
        while (aceCount > 0 && totalValue + 10 <= 21) {
            totalValue += 10;
            aceCount--;
        }

        return totalValue;
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    // Method to clear the hand (useful for resetting)
    public void clear() {
        cards.clear();
    }

    // Method to return a string representation of the hand
    @Override
    public String toString() {
        StringBuilder handString = new StringBuilder();
        for (Card card : cards) {
            handString.append(card.toString()).append("\n");
        }
        return handString.toString();
    }

    // Method to get the number of cards in the hand
    public int getNumberOfCards() {
        return cards.size();
    }


}
