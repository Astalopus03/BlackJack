public class Dealer {
    private Hand hand; // The dealer's hand of cards

    // Constructor to initialize the dealer with an empty hand
    public Dealer() {
        hand = new Hand();
    }

    // Method to add a card to the dealer's hand (called when dealing)
    public void receiveCard(Card card) {
        hand.addCard(card);
    }

    // Method to make the dealer's decisions (hit until the hand value is 17 or higher)
    public void play(Deck deck) {
        // Dealer hits if total value is less than 17, otherwise stands
        while (hand.getTotalValue() < 17) {
            System.out.println("Dealer hits.");
            receiveCard(deck.dealCard());
            showHand(); // Display dealer's current hand after each hit
        }
        System.out.println("Dealer stands.");
    }

    // Method to get the total value of the dealer's hand
    public int getHandValue() {
        return hand.getTotalValue();
    }

    // Method to clear the dealer's hand (used between rounds)
    public void resetHand() {
        hand.clear();
    }

    // Method to show the dealer's hand
    public void showHand() {
        System.out.println("Total value: " + hand.getTotalValue());
    }

    // Method to reveal the dealer's first card (useful for AI decision-making)
    public Card getFirstCard() {
        return hand.getNumberOfCards() > 0 ? hand.cards.get(0) : null; // Reveal the first card
    }
    public Card getVisibleCard() {
        if (hand.getCards().isEmpty()) {
            throw new IllegalStateException("Dealer has no cards");
        }
        return hand.getCards().get(0); // Returns the first card as the visible card
    }

    public void showInitialHand() {
        System.out.println("Dealer's Hand:");
        if (hand.getCards().size() > 0) {
            System.out.println(hand.getCards().get(0) + " and [hidden card]");
        } else {
            System.out.println("[No cards in hand]");
        }
    }
}
