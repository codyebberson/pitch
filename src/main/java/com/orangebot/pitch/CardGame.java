package com.orangebot.pitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

/**
 * The CardGame class manages the fundamentals of a card game.
 *
 * The CardGame constructor takes an array of key objects.  Those keys
 * represent the various locations that a card may be (i.e., the deck,
 * the discard pile, a player's hand, etc).
 *
 * It is transactional, so that all cards can always be tracked
 * to a single location.
 */
public class CardGame {
    private final Map<Object, List<Card>> lists;

    /**
     * Creates a new card game.
     * @param keys Array of sublists by key names.
     */
    public CardGame(final Object... keys) {
        Validate.notNull(keys);
        Validate.notEmpty(keys);
        Validate.noNullElements(keys);
        lists = new HashMap<>();
        initLists(keys);
        initDeck(keys[0]);
    }

    /**
     * Initialize all of the sublists.
     * @param keys Array of sublists by key names.
     */
    private void initLists(final Object[] keys) {
        for (Object obj : keys) {
            lists.put(obj, new ArrayList<Card>());
        }
    }

    /**
     * Initializes the primary deck.
     * @param deckKey The deck key name.
     */
    private void initDeck(Object deckKey) {
        final List<Card> deck = lists.get(deckKey);

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                if (suit != Suit.JOKER && rank != Rank.LOW && rank != Rank.HIGH) {
                    deck.add(new Card(rank, suit));
                }
            }
        }

        deck.add(new Card(Rank.LOW, Suit.JOKER));
        deck.add(new Card(Rank.HIGH, Suit.JOKER));
    }

    /**
     * Returns a read-only list of cards in the sublist by key name.
     * @param key The list key name.
     * @return Read-only list of cards.
     */
    public List<Card> get(final Object key) {
        Validate.notNull(key);
        return Collections.unmodifiableList(new ArrayList<Card>(lists.get(key)));
    }

    /**
     * Moves a single card from one list to another list.
     * @param card The card to move.
     * @param from The source list.
     * @param to The destination list.
     */
    public void move(final Card card, final Object from, final Object to) {
        Validate.notNull(card);
        Validate.notNull(from);
        Validate.notNull(to);

        final List<Card> fromList = lists.get(from);
        final List<Card> toList = lists.get(to);
        final int index = fromList.indexOf(card);
        if (index >= 0) {
            toList.add(fromList.remove(index));
        }
    }

    /**
     * Moves the first card from one list to another list.
     * @param from The source list.
     * @param to The destination list.
     */
    public void move(final Object from, final Object to) {
        Validate.notNull(from);
        Validate.notNull(to);
        move(from, to, 1);
    }

    /**
     * Moves the first n cards from one list to another list.
     * @param from The source list.
     * @param to The destination list.
     * @param count The number of cards to move.
     */
    public void move(final Object from, final Object to, final int count) {
        Validate.notNull(from);
        Validate.notNull(to);
        Validate.inclusiveBetween(0, lists.get(from).size(), count);

        final List<Card> fromList = lists.get(from);
        final List<Card> toList = lists.get(to);

        for (int i = 0; i < count; i++) {
            toList.add(fromList.remove(0));
        }
    }

    /**
     * Moves all cards from one list to another list.
     * @param from The source list.
     * @param to The destination list.
     */
    public void moveAll(final Object from, final Object to) {
        Validate.notNull(from);
        Validate.notNull(to);

        final int count = lists.get(from).size();
        if (count > 0) {
            move(from, to, count);
        }
    }

    /**
     * Moves all cards from all lists to one list.
     * @param to The destination list.
     */
    public void moveAll(final Object to) {
        Validate.notNull(to);

        for (Object obj : lists.keySet()) {
            if (!obj.equals(to)) {
                moveAll(obj, to);
            }
        }
    }

    /**
     * Shuffles a list.
     * @param key The list key name.
     */
    public void shuffle(final Object key) {
        Validate.notNull(key);
        Collections.shuffle(lists.get(key));
    }

    /**
     * Sorts a list.
     * @param key The list key name.
     * @param c The list comparator.
     */
    public void sort(final Object key, final Comparator<Card> c) {
        Validate.notNull(key);
        Collections.sort(lists.get(key), c);
    }

    /**
     * The card class represents a single card.
     */
    public static final class Card {
        private final Rank rank;
        private final Suit suit;

        /**
         * Creates a new card.
         * @param rank The rank (ace, king, ten, etc).
         * @param suit The suit (hearts, clubs, etc).
         */
        private Card(Rank rank, Suit suit) {
            this.rank = rank;
            this.suit = suit;
        }

        /**
         * Returns the card rank.
         * @return The card rank.
         */
        public Rank rank() {
            return this.rank;
        }

        /**
         * Returns the card suit.
         * @return The card suit.
         */
        public Suit suit() {
            return this.suit;
        }

        /**
         * Returns a semi unique hash code.
         * @return The hash code.
         */
        @Override
        public int hashCode() {
            return rank.hashCode() + 31 * suit.hashCode();
        }

        /**
         * Returns whether the object is the same as this card.
         * @return True if equal.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Card)) {
                return false;
            }

            Card other = (Card) obj;
            return this.rank == other.rank && this.suit == other.suit;
        }

        /**
         * Returns a string name of this card.
         * @return The card name.
         */
        @Override
        public String toString() {
            return rank + " of " + suit;
        }
    }

    public static enum Rank {
        DEUCE(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JACK(11),
        QUEEN(12),
        KING(13),
        ACE(14),
        LOW(15),
        HIGH(16);

        private final int value;

        private Rank(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum Suit {
        CLUBS(2),
        DIAMONDS(3),
        HEARTS(4),
        SPADES(1),
        JOKER(5);

        private final int value;

        private Suit(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
