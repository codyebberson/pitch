package com.orangebot.pitch.strats;

import com.orangebot.pitch.CardGame.Card;
import com.orangebot.pitch.CardGame.Rank;
import com.orangebot.pitch.PitchGame.PlayedCard;
import com.orangebot.pitch.PitchGame.Player;
import com.orangebot.pitch.PitchGame.PlayerStrategy;

public class SimpleStrategy implements PlayerStrategy {

    @Override
    public Card playCard(Player p) {

        Card myHighCard = p.getMyHighestCard(true, true, true);

        if (p.isLead()) {
            // If I have high card, play high card
            if (p.isHighCard(myHighCard)) {
                return myHighCard;
            }

            // Try to find a non-point card
            Card card = p.getMyLowestCard(false, true, false);
            if (card != null) {
                return card;
            }

            // Try to avoid the three
            card = p.getMyLowestCard(true, true, false);
            if (card != null) {
                return card;
            }
        }

        if (p.isHighCard(myHighCard)) {
            return myHighCard;
        }

        PlayedCard highCard = p.getHighestPlayedCard();
        if (highCard.getPlayerId().getTeam() == p.getId().getTeam() &&
                p.isHighCard(highCard.getCard())) {
            // High card is from my partner
            // Try to play the three
            if (p.hasCard(Rank.THREE)) {
                return p.getCard(Rank.THREE);
            }

            // Try to find a point card
            Card card = p.getMyLowestCard(true, false, true);
            if (card != null) {
                return card;
            }

        } else {
            // High card is not from my partner
            // Try to find a non-point card
            Card card = p.getMyLowestCard(false, true, false);
            if (card != null) {
                return card;
            }

            // Try to avoid the three
            card = p.getMyLowestCard(true, true, false);
            if (card != null) {
                return card;
            }
        }

        // Return the lowest card we have
        return p.getMyLowestCard(true, true, true);
    }

}
