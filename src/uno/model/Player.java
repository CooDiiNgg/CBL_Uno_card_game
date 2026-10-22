package uno.model;

public class Player {

    private String name;
    private Hand hand;
    private boolean isBot;

    public Player(String name, Hand hand) {
        this(name, hand, false);
    }

    public Player(String name, Hand hand, boolean isBot) {
        this.name = name;
        this.hand = hand;
        this.isBot = isBot;
    }

    public String getName() {
        return name;
    }

    public Hand getHand() {
        return hand;
    }

    public boolean isBot() {
        return isBot;
    }

    public void drawCard(Card card) {
        hand.addCard(card);
    }
}
