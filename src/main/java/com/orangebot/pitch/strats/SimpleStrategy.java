package com.orangebot.pitch.strats;

import com.orangebot.pitch.CardGame.Card;
import com.orangebot.pitch.PitchGame.PlayedCard;
import com.orangebot.pitch.PitchGame.Player;
import com.orangebot.pitch.PitchGame.PlayerStrategy;

public class SimpleStrategy implements PlayerStrategy {

    @Override
    public Card playCard(Player p) {

        if (p.isLead()) {
            return p.getHighCard(true, true, true);
        }

        PlayedCard highCard = p.getHighestPlayedCard();
        if (highCard.getPlayerId().getTeam() == p.getId().getTeam()) {
            // High card is from my partner
            // Try to find a point card
            Card card = p.getLowCard(true, false, true);
            if (card != null) {
                return card;
            }

        } else {
            // High card is not from my partner
            // Try to find a non-point card
            Card card = p.getLowCard(false, true, false);
            if (card != null) {
                return card;
            }

            // Try to avoid the three
            card = p.getLowCard(true, true, false);
            if (card != null) {
                return card;
            }
        }

        // Return the lowest card we have
        return p.getLowCard(true, true, true);
    }

}
