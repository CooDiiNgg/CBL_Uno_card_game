package uno.model;

public class GameState {
    
    private Deck deck;
    private Deck discardPile;
    private Player[] players;

    public GameState(Deck deck, Deck discardPile, Player[] players) {
        this.deck = deck;
        this.discardPile = discardPile;
        this.players = players;
    }

    public Deck getDeck() {
        return deck;
    }

    public Deck getDiscardPile() {
        return discardPile;
    }

    public Player[] getPlayers() {
        return players;
    }
}
