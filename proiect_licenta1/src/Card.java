public class Card implements Comparable<Card> {
    private Suit suit;
    private Rank rank;

    // Constructor
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    // Method to get the blackjack value of the card
    public int getValue() {
        switch (rank) {
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return 10;
            case ACE:
                return 1;
            default:
                throw new IllegalArgumentException("Unknown rank: " + rank);
        }
    }

    // Method to return a readable string for the card
    @Override
    public String toString() {
        String rankString;

        switch (rank) {
            case TWO: rankString = "2"; break;
            case THREE: rankString = "3"; break;
            case FOUR: rankString = "4"; break;
            case FIVE: rankString = "5"; break;
            case SIX: rankString = "6"; break;
            case SEVEN: rankString = "7"; break;
            case EIGHT: rankString = "8"; break;
            case NINE: rankString = "9"; break;
            case TEN: rankString = "10"; break;
            case JACK: rankString = "Jack"; break;
            case QUEEN: rankString = "Queen"; break;
            case KING: rankString = "King"; break;
            case ACE: rankString = "Ace"; break;
            default: throw new IllegalArgumentException("Unknown rank: " + rank);
        }

        return rankString + " of " + suit.toString().charAt(0) + suit.toString().substring(1).toLowerCase();
    }

    // Implementation of compareTo for sorting
    @Override
    public int compareTo(Card other) {
        // Compare by rank first
        int rankComparison = this.rank.ordinal() - other.rank.ordinal();
        if (rankComparison != 0) {
            return rankComparison;
        }

        // If ranks are the same, compare by suit
        return this.suit.ordinal() - other.suit.ordinal();
    }

    // Getters for the suit and rank fields (optional)
    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }
}
