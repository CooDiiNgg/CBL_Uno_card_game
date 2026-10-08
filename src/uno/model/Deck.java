package uno.model;
import uno.util.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uno.util.Value;

public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        for (Color color : Color.getAllColors()) {
            for (Value value : Value.getAllValues()) {
                cards.add(new Card(color, value.getValue()));
            }
        }
        for (Value specialValue : Value.getSpecialValues()) {
            for (int i = 0; i < 4; i++) {
                cards.add(new Card(Color.SPECIAL, specialValue.getValue()));
            }
        }
    }

    private void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty deck");
        }
        return cards.remove(0);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public void mergeDeck(Deck otherDeck) {
        if (otherDeck == null) {
            throw new IllegalArgumentException("Other deck cannot be null");
        }
        cards.addAll(otherDeck.cards);
        shuffle();
    }

    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        cards.add(card);
    }

    public Card peekTopCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot peek at an empty deck");
        }
        return cards.get(0);
    }
}
