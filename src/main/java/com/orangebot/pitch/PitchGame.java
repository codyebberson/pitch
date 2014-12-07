package com.orangebot.pitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.orangebot.pitch.CardGame.Card;
import com.orangebot.pitch.CardGame.Rank;
import com.orangebot.pitch.CardGame.Suit;
import com.orangebot.pitch.strats.SimpleStrategy;

public class PitchGame {
    public static final String DECK = "deck";
    public static final String DISCARD = "discard";
    public static final String CENTER = "center";
    public static final PlayerId P1 = new PlayerId(0);
    public static final PlayerId P2 = new PlayerId(1);
    public static final PlayerId P3 = new PlayerId(2);
    public static final PlayerId P4 = new PlayerId(3);
    public static final Object[] LISTS = { DECK, DISCARD, CENTER, P1, P2, P3, P4 };

    private final CardGame cards;
    private final Player[] players;
    private final List<PlayedCard> played;
    private final Comparator<Card> cardComparator;
    private final Comparator<PlayedCard> playedCardComparator;
    private final int[] score;
    private Suit trump;
    private Player bidder;
    private Player lead;
    private String bidToken;

    public PitchGame() {
        this.cards = new CardGame(DECK, DISCARD, CENTER, P1, P2, P3, P4);
        this.score = new int[2];
        this.players = new Player[] {
                new Player(P1, P3),
                new Player(P2, P4),
                new Player(P3, P1),
                new Player(P4, P2),
        };
        this.played = new ArrayList<PlayedCard>();

        this.cardComparator = new Comparator<Card>(){
            @Override
            public int compare(Card c1, Card c2) {
                return -Integer.compare(getSortValue(c1), getSortValue(c2));
            }};

        this.playedCardComparator = new Comparator<PlayedCard>(){
            @Override
            public int compare(PlayedCard c1, PlayedCard c2) {
                return -Integer.compare(getSortValue(c1.getCard()), getSortValue(c2.getCard()));
            }};
    }

    /**
     * Plays a single round.
     */
    public void playRound() {
        dealRound();
        bid();
        discard();
        redealRound();

        for (int i = 0; i < 6; i++) {
            System.out.println("Hand " + (i + 1));
            playHand();
            System.out.println();
        }

        System.out.println("Score: " + score[0] + ", " + score[1]);
    }

    /**
     * Deals the round.
     */
    public void dealRound() {
        cards.moveAll(DECK);
        cards.shuffle(DECK);

        for (Player p : players) {
            cards.move(DECK, p.getId(), 9);
            p.setOut(false);
        }
    }

    /**
     * Auctions the bid.
     */
    public void bid() {
        trump = Suit.HEARTS;
        bidder = players[0];
        lead = bidder;
    }

    /**
     * Discards non-trump to the discard pile.
     */
    public void discard() {
        for (Player p : players) {
            discardPlayer(p);
        }

        // Remainder goes to the bidder
        cards.sort(bidder.getId(), cardComparator);
        bidToken = bidder.getHandString();
        System.out.println("Bid hand: " + bidToken);
    }

    /**
     * Discards non-trump to the discard pile for one player.
     * @param p The player.
     */
    public void discardPlayer(Player p) {
        List<Card> hand = cards.get(p.getId());
        for (Card card : hand) {
            if (!isTrump(card)) {
                cards.move(card, p.getId(), DISCARD);
            }
        }
    }

    /**
     * Redeals the round.
     */
    public void redealRound() {
        for (Player p : players) {
            redealPlayer(p);
        }

        // Remainder goes to the bidder
        cards.moveAll(DECK, bidder.getId());
        discardPlayer(bidder);
        cards.sort(bidder.getId(), cardComparator);
        System.out.println("Play hand: " + bidder.getHandString());
    }

    /**
     * Redeals for one player.
     * @param p The player.
     */
    public void redealPlayer(Player p) {
        final int count = cards.get(p.getId()).size();
        final int need = 6 - count;
        if (need > 0) {
            cards.move(DECK, p.getId(), need);
            discardPlayer(p);
        }
    }

    /**
     * Plays a single hand.
     */
    public void playHand() {
        // Clear the table
        cards.moveAll(CENTER, DISCARD);
        played.clear();

        SimpleStrategy strat = new SimpleStrategy();

        for (int i = 0; i < 4; i++) {
            Player p = players[(lead.getId().getIndex() + i) % 4];
            cards.sort(p.getId(), cardComparator);
            if (p.hasTrump()) {
                System.out.print(p + " plays ");
                Card card = strat.playCard(p);
                cards.move(card, p.getId(), CENTER);
                played.add(new PlayedCard(p.getId(), card));
                System.out.println(card);
            } else {
                p.setOut(true);
                System.out.println(p + " is out");
            }
        }

        if (!played.isEmpty()) {
            cards.sort(CENTER, cardComparator);
            played.sort(playedCardComparator);

            PlayedCard highCard = played.get(0);
            System.out.println("High card = " + highCard);

            int highCardTeam = highCard.getPlayerId().getTeam();
            int[] scoreDelta = new int[2];
            for (PlayedCard c : played) {
                int cardTeam = c.getPlayerId().getTeam();
                int team = c.getCard().rank() == Rank.DEUCE ? cardTeam : highCardTeam;
                scoreDelta[team] += getPointValue(c.getCard());
            }
            System.out.println("Score delta: " + scoreDelta[0] + ", " + scoreDelta[1]);
            score[0] += scoreDelta[0];
            score[1] += scoreDelta[1];
            System.out.println("Score: " + score[0] + ", " + score[1]);

            lead = players[highCard.getPlayerId().getIndex()];
        }
    }

    /**
     * Returns the left jack suit for a trump suit.
     *
     * @param s The trump suit.
     * @return The left jack suit.
     */
    public static Suit getLeftJackSuit(Suit s) {
        switch (s) {
        case CLUBS: return Suit.SPADES;
        case SPADES: return Suit.CLUBS;
        case HEARTS: return Suit.DIAMONDS;
        case DIAMONDS: return Suit.HEARTS;
        default: return s;
        }
    }

    /**
     * Returns true if a card is a trump card.
     * @param c The card.
     * @return True if trump; false otherwise.
     */
    public boolean isTrump(Card c) {
        return c.suit() == trump || c.suit() == Suit.JOKER || isLeftJack(c);
    }

    /**
     * Returns true if a card is the left jack.
     * @param c The card.
     * @return True if the left jack; false otherwise.
     */
    public boolean isLeftJack(Card c) {
        return c.rank() == Rank.JACK && c.suit() == getLeftJackSuit(trump);
    }

    /**
     * Returns the sort value of a card.
     * @param c The card.
     * @return The sort value.
     */
    public int getSortValue(Card c) {
        if (!isTrump(c)) {
            return 0;
        }

        if (c.rank().getValue() <= Rank.TEN.getValue()) {
            return c.rank().getValue();
        }

        if (c.suit() == Suit.JOKER) {
            return c.rank() == Rank.LOW ? 11 : 12;
        }

        if (isLeftJack(c)) {
            return 13;
        }

        return 14 + (c.rank().getValue() - Rank.JACK.getValue());
    }

    /**
     * Returns the point value of a card.
     * @param c The card.
     * @return The point value.
     */
    public int getPointValue(Card c) {
        if (!isTrump(c)) {
            return 0;
        }

        switch (c.rank()) {
        case THREE:
            return 3;
        case ACE: case JACK: case HIGH: case LOW: case TEN: case DEUCE:
            return 1;
        default:
            return 0;
        }
    }

    /**
     * Returns the short (1-2 chars) display name for a trump card.
     * @param c The card.
     * @return The short name.
     */
    public String getShortName(Card c) {
        switch (c.rank()) {
        case ACE: return "A";
        case KING: return "K";
        case QUEEN: return "Q";
        case JACK: return isLeftJack(c) ? "LJ" : "J";
        case HIGH: return "JH";
        case LOW: return "JL";
        default: return Integer.toString(c.rank().getValue());
        }
    }


    /**
     * Prints a debug display to the console.
     */
    public void print(String title) {
        System.out.println("================ START " + title + " =====================");
        for (Object key : LISTS) {
            System.out.println(key + ":");
            List<Card> list = cards.get(key);
            for (Card c : list) {
                System.out.println ("  " + c + " (" + getSortValue(c) + "," + getPointValue(c) + ")");
            }
        }
        System.out.println("================= END " + title + " ======================");
    }

    /**
     * The PlayerId class represents a player identifier.
     * It is a representation of a player that can be exposed to the
     * strategy implementations.
     */
    public final static class PlayerId {
        private final int index;

        private PlayerId(final int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public int getTeam() {
            return index % 2;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof PlayerId)) {
                return false;
            }

            PlayerId other = (PlayerId) obj;
            return this.index == other.index;
        }

        @Override
        public String toString() {
            return "P" + index;
        }
    }


    /**
     * The Player class represents a single player.
     *
     * This class is designed to be shared with the strategy implementations.
     */
    public final class Player {
        private final PlayerId id;
        private final PlayerId partnerId;
        private boolean out;

        private Player(final PlayerId id, final PlayerId partnerId) {
            this.id = id;
            this.partnerId = partnerId;
        }

        public PlayerId getId() {
            return id;
        }

        public PlayerId getPartnerId() {
            return partnerId;
        }

        public List<Card> getHand() {
            return cards.get(id);
        }

        public List<PlayedCard> getPlayedCards() {
            return Collections.unmodifiableList(played);
        }

        public boolean isLead() {
            return played.isEmpty();
        }

        public boolean isOut() {
            return out;
        }

        private void setOut(boolean out) {
            this.out = out;
        }

        public boolean isOut(PlayerId playerId) {
            return players[playerId.getIndex()].isOut();
        }

        public boolean isTrump(Card c) {
            return PitchGame.this.isTrump(c);
        }

        public int getSortValue(Card c) {
            return PitchGame.this.getSortValue(c);
        }

        public int getPointValue(Card c) {
            return PitchGame.this.getPointValue(c);
        }

        public boolean hasTrump() {
            for (Card c : getHand()) {
                if (isTrump(c)) {
                    return true;
                }
            }
            return false;
        }

        public Card getHighCard(boolean includePoints, boolean includeNonPoints, boolean includeThree) {
            Card bestCard = null;
            int bestValue = 0;

            for (Card c : getHand()) {
                if (!matches(c, includePoints, includeNonPoints, includeThree)) {
                    continue;
                }

                if (getSortValue(c) > bestValue) {
                    bestCard = c;
                    bestValue = getSortValue(c);
                }
            }

            return bestCard;
        }

        public Card getLowCard(boolean includePoints, boolean includeNonPoints, boolean includeThree) {
            Card bestCard = null;
            int bestValue = Integer.MAX_VALUE;

            for (Card c : getHand()) {
                if (!matches(c, includePoints, includeNonPoints, includeThree)) {
                    continue;
                }

                if (getSortValue(c) < bestValue) {
                    bestCard = c;
                    bestValue = getSortValue(c);
                }
            }

            return bestCard;
        }

        private boolean matches(Card c, boolean includePoints, boolean includeNonPoints, boolean includeThree) {
            if (!isTrump(c)) {
                return false;
            }

            if (!includePoints && getPointValue(c) > 0) {
                return false;
            }

            if (!includeNonPoints && getPointValue(c) == 0) {
                return false;
            }

            if (!includeThree && c.rank() == Rank.THREE) {
                return false;
            }

            return true;
        }

        public PlayedCard getHighestPlayedCard() {
            PlayedCard bestCard = null;
            int bestRank = 0;

            for (PlayedCard card : played) {
                if (getSortValue(card.getCard()) > bestRank) {
                    bestCard = card;
                    bestRank = getSortValue(card.getCard());
                }
            }

            return bestCard;
        }

        public String getHandString() {
            StringBuilder b = new StringBuilder();
            for (Card c : getHand()) {
                b.append(getShortName(c));
                b.append(" ");
            }
            return b.toString();
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("[Player id=");
            b.append(id);
            b.append(", hand=[");
            b.append(getHandString().trim());
            b.append("]]");
            return b.toString();
        }
    }

    public static class PlayedCard {
        private final PlayerId playerId;
        private final Card card;

        public PlayedCard(PlayerId playerId, Card card) {
            this.playerId = playerId;
            this.card = card;
        }

        public PlayerId getPlayerId() {
            return playerId;
        }

        public Card getCard() {
            return card;
        }

        @Override
        public String toString() {
            return "[PlayedCard playerId=" + playerId + ", card=" + card + "]";
        }
    }

    public static interface PlayerStrategy {
        public Card playCard(Player p);
    }
}
