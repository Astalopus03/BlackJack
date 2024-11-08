public class Main {
    public static void main(String[] args) {
        Deck deck = new Deck();
        Dealer dealer = new Dealer();

        // Deal two cards to the dealer
        dealer.receiveCard(deck.dealCard());
        dealer.receiveCard(deck.dealCard());

        // Show the dealer's initial hand (before their turn)
        dealer.showHand();

        // Dealer plays (hits until 17 or more)
        dealer.play(deck);

        // Final hand
        System.out.println("Dealer's final hand:");
        dealer.showHand();
    }
}
