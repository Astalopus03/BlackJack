import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    // Constructor to initialize and shuffle the deck
    public Deck() {
        reset(); // Populate and shuffle the deck on initialization
    }

    // Reset the deck to a full 52-card set and shuffle
    public void reset() {
        cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        shuffle();
    }

    // Shuffle the deck
    public void shuffle() {
        Collections.shuffle(cards);
    }

    // Deal a card from the top of the deck
    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("The deck is empty. Cannot deal any more cards.");
        }
        return cards.remove(cards.size() - 1);
    }
}
