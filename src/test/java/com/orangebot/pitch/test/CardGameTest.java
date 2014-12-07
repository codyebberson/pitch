package com.orangebot.pitch.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.orangebot.pitch.CardGame;
import com.orangebot.pitch.CardGame.Card;

public class CardGameTest {

    @Test
    public void testEmptyConstructor() {
        try {
            new CardGame();
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testNullConstructor() {
        try {
            new CardGame(null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testSingleDeck() {
        CardGame cg = new CardGame("deck");
        Assert.assertNotNull(cg);

        List<Card> cards = cg.get("deck");
        Assert.assertNotNull(cards);
        Assert.assertEquals(54, cards.size());
    }

    @Test
    public void testLists() {
        CardGame cg = new CardGame("d1", "d2");
        Assert.assertNotNull(cg);
        Assert.assertNotNull(cg.get("d1"));
        Assert.assertNotNull(cg.get("d2"));
        Assert.assertEquals(54, cg.get("d1").size());
        Assert.assertEquals(0, cg.get("d2").size());

        // Move 10 cards
        cg.move("d1", "d2", 10);
        Assert.assertEquals(44, cg.get("d1").size());
        Assert.assertEquals(10, cg.get("d2").size());

        // Move zero cards
        cg.move("d1", "d2", 0);
        Assert.assertEquals(44, cg.get("d1").size());
        Assert.assertEquals(10, cg.get("d2").size());
    }

    @Test
    public void testShuffle() {
        CardGame cg = new CardGame("deck");
        Card first = cg.get("deck").get(0);
        cg.shuffle("deck");
        Assert.assertNotEquals(first, cg.get("deck").get(0));
    }
}
