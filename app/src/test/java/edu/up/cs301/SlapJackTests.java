package edu.up.cs301;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import edu.up.cs301.game.GameFramework.actionMessage.MyNameIsAction;
import edu.up.cs301.game.GameFramework.actionMessage.ReadyAction;
import edu.up.cs301.game.GameFramework.players.GamePlayer;
import edu.up.cs301.game.R;
import edu.up.cs301.slapjack.Deck;
import edu.up.cs301.slapjack.SJLocalGame;
import edu.up.cs301.slapjack.SJMainActivity;
import edu.up.cs301.slapjack.card.Rank;
import edu.up.cs301.slapjack.infoMessage.SJState;
import edu.up.cs301.slapjack.sjActionMessage.SJPlayAction;
import edu.up.cs301.slapjack.sjActionMessage.SJSlapAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/* @author Eric Imperio
 * @version 2020
 * Use this as a template to make your own tests
 * These are go tests to use
 * Additional tests are good as well
 * NOTE: Avoid tests that simply check one action.
 *    Example: You know that the following will set the expected value.
 *        a = b + 2;
 */
@RunWith(RobolectricTestRunner.class)
public class SlapJackTests {

    public SJMainActivity activity;

    @Before
    public void setup() throws Exception {
        activity = Robolectric.buildActivity(SJMainActivity.class).create().resume().get();
    }

    //This does a full game to verify it works
    // Notice that it includes invalid moves
    // You can do it this way or have multiple unit tests that do this
    // Sometimes easier to just have one since this is turn-based
    @Test
    public void test_checkGamePlay() {
        //TODO: Modify the following for your game
        //Starting the game
        View view = activity.findViewById(R.id.playGameButton);
        activity.onClick(view);
        //Getting the created game
        SJLocalGame sjLocalGame = (SJLocalGame) activity.getGame();
        //Getting the players
        GamePlayer[] gamePlayers = sjLocalGame.getPlayers();
        //Sending the names of the players to the game
        for (GamePlayer gp : gamePlayers) {
            sjLocalGame.sendAction(new MyNameIsAction(gp, gp.getClass().toString()));
        }
        //Telling the game everyone is ready
        for (GamePlayer gp : gamePlayers) {
            sjLocalGame.sendAction(new ReadyAction(gp));
        }
        //TODO: Start Making moves here
        ((SJState)sjLocalGame.getGameState()).setToPlay(0);
        SJState match = new SJState((SJState) sjLocalGame.getGameState());
        GamePlayer player1 = gamePlayers[0];
        GamePlayer player2 = gamePlayers[1];
        Deck p1 = match.getDeck(0);
        Deck p2 = match.getDeck(1);
        Deck middle = match.getDeck(2);
        //Can I make two moves in a row?
        sjLocalGame.sendAction(new SJPlayAction(player1));
        sjLocalGame.sendAction(new SJPlayAction(player1));
        //Setting the expected outcome of the two lines following setToPlay
        p1.moveTopCardTo(middle);
        match.setToPlay(1);
        //Testing that I couldn't make two moves in a row
        helper_assert_match(p1,p2,middle,sjLocalGame, match);
        //Make sure turns do in fact work
        sjLocalGame.sendAction(new SJPlayAction(player2));
        sjLocalGame.sendAction(new SJPlayAction(player1));
        //Expected changes from two lines above
        p2.moveTopCardTo(middle);
        p1.moveTopCardTo(middle);
        //Make sure those changes happened
        helper_assert_match(p1,p2,middle,sjLocalGame, match);
        //Get to a finished game
        while(p2.size() > 0 || middle.size() > 0) {
            if (middle.size() > 0 && middle.peekAtTopCard().getRank() == Rank.JACK) {
                sjLocalGame.sendAction(new SJSlapAction(player1));
                middle.moveAllCardsTo(p1);
                assertEquals("Middle Deck not empty", 0, ((SJState) sjLocalGame.getGameState()).getDeck(2).size());
                assertEquals("Player One Deck not the same size", p1.size(), ((SJState) sjLocalGame.getGameState()).getDeck(0).size());
                match.setDeck(0,new Deck(((SJState) sjLocalGame.getGameState()).getDeck(0)));
                p1 = match.getDeck(0);
                helper_assert_match(p1,p2,middle,sjLocalGame, match);
            }
            if(p2.size() > 0) {
                sjLocalGame.sendAction(new SJPlayAction(player2));
                //Expected Changes from the line above
                p2.moveTopCardTo(middle);
                match.setToPlay(0);
            }
            //Check those worked
            helper_assert_match(p1,p2,middle,sjLocalGame, match);
            if (middle.size() > 0 && middle.peekAtTopCard().getRank() == Rank.JACK) {
                sjLocalGame.sendAction(new SJSlapAction(player1));
                middle.moveAllCardsTo(p1);
                assertEquals("Middle Deck not empty", 0, ((SJState) sjLocalGame.getGameState()).getDeck(2).size());
                assertEquals("Player One Deck not the same size", p1.size(), ((SJState) sjLocalGame.getGameState()).getDeck(0).size());
                match.setDeck(0,new Deck(((SJState) sjLocalGame.getGameState()).getDeck(0)));
                p1 = match.getDeck(0);
                helper_assert_match(p1,p2,middle,sjLocalGame, match);
            }
            sjLocalGame.sendAction(new SJPlayAction(player1));
            //Expected Changes from the line above
            if(!(middle.size() == 0 && p2.size() == 0)) {
                p1.moveTopCardTo(middle);
                if (p2.size() > 0) match.setToPlay(1);
            }
            //Check those worked
            helper_assert_match(p1,p2,middle,sjLocalGame, match);
        }
        //Make sure player 1 won
        if(middle.size() != 0) {
            if (middle.peekAtTopCard().getRank() == Rank.JACK) {
                sjLocalGame.sendAction(new SJSlapAction(player1));
                middle.moveAllCardsTo(p1);
                assertEquals("Middle Deck not empty", 0, ((SJState) sjLocalGame.getGameState()).getDeck(2).size());
                assertEquals("Player One Deck not the same size", p1.size(), ((SJState) sjLocalGame.getGameState()).getDeck(0).size());
                match.setDeck(0,new Deck(((SJState) sjLocalGame.getGameState()).getDeck(0)));
                p1 = match.getDeck(0);
                assertEquals("Player 1 did not win", 0, sjLocalGame.whoWon());
            } else {
                assertEquals("It was not a draw", -1, sjLocalGame.whoWon());
            }
        } else {
            assertEquals("Player 1 did not win", 0, sjLocalGame.whoWon());
        }
        //Check if you can move after game over
        sjLocalGame.sendAction(new SJPlayAction(player1));
        helper_assert_match(p1,p2,middle,sjLocalGame, match);
    }

    private void helper_assert_match(Deck p1, Deck p2, Deck middle, SJLocalGame sjLocalGame, SJState match){
        assertTrue("Game States were not equal"+
                        "\n    Deck 1: (match)"+p1.toString()+"  (reg)"+((SJState) sjLocalGame.getGameState()).getDeck(0)+
                        "\n    Deck 2: (match)"+p2.toString()+"  (reg)"+((SJState) sjLocalGame.getGameState()).getDeck(1)+
                        "\n    Middle: (match)"+middle.toString()+"  (reg)"+((SJState) sjLocalGame.getGameState()).getDeck(2)+
                        "\n    Turn  : (match)"+match.toPlay()+"  (reg)"+((SJState) sjLocalGame.getGameState()).toPlay()
                , ((SJState) sjLocalGame.getGameState()).equals(match));
    }

    //Tests focused on the state: copy constructors and equals
    //copy cons:  empty default state, in progress state, full board state
    //This tests the copy constructor when nothing is set
    @Test
    public void test_CopyConstructorOfState_Empty(){
        SJState sjState = new SJState();
        SJState copyState = new SJState(sjState);
        assertTrue("Copy Constructor did not produce equal States", sjState.equals(copyState));
    }

    //Make state that looks like a game that'd be in progress
    @Test
    public void test_CopyConstructorOfState_InProgress(){
        SJState sjState = new SJState();
        for(int i = 0; i < 10; i++) {
            sjState.getDeck(0).moveTopCardTo(sjState.getDeck(2));
        }
        sjState.getDeck(1).shuffle();
        SJState copyState = new SJState(sjState);
        assertTrue("Copy Constructor did not produce equal States", sjState.equals(copyState));
    }

    // Make a state that has all values set to something (preferably not default)
    @Test
    public void test_CopyConstructorOfState_Full(){
        SJState sjState = new SJState();
        sjState.setToPlay(1);
        sjState.getDeck(0).moveAllCardsTo(sjState.getDeck(2));
        sjState.getDeck(2).shuffle();
        sjState.getDeck(1).moveAllCardsTo(sjState.getDeck(2));
        sjState.getDeck(2).shuffle();
        SJState copyState = new SJState(sjState);
        assertTrue("Copy Constructor did not produce equal States", sjState.equals(copyState));
    }

    //These follow the same structure as copy but they test your equals method
    // Copy might fail because your equals is wrong
    // DO NOT make equals use copy while copy is using equals. You won't know which is broken easily.
    //Equals
    @Test
    public void test_Equals_State_Empty(){
        SJState sjState = new SJState();
        sjState.setToPlay(0);
        SJState otherState = new SJState();
        otherState.setToPlay(0);
        otherState.setDeck(0,sjState.getDeck(0));
        otherState.setDeck(1,sjState.getDeck(1));
        otherState.setDeck(2,sjState.getDeck(2));
        assertTrue("Equals method did not agree the States where equal", sjState.equals(otherState));
    }

    @Test
    public void test_Equals_State_InProgress(){
        SJState sjState = new SJState();
        sjState.setToPlay(0);
        for(int i = 0; i < 10; i++) {
            sjState.getDeck(0).moveTopCardTo(sjState.getDeck(2));
        }
        SJState otherState = new SJState();
        otherState.setToPlay(0);
        otherState.setDeck(0,sjState.getDeck(0));
        otherState.setDeck(1,sjState.getDeck(1));
        otherState.setDeck(2,sjState.getDeck(2));
        assertTrue("Equals method did not agree the States where equal", sjState.equals(otherState));
    }

    @Test
    public void test_Equals_State_Full(){
        SJState sjState = new SJState();
        sjState.setToPlay(0);
        sjState.getDeck(0).moveAllCardsTo(sjState.getDeck(2));
        sjState.getDeck(1).moveAllCardsTo(sjState.getDeck(2));
        SJState otherState = new SJState();
        otherState.setToPlay(0);
        otherState.setDeck(0,sjState.getDeck(0));
        otherState.setDeck(1,sjState.getDeck(1));
        otherState.setDeck(2,sjState.getDeck(2));
        assertTrue("Equals method did not agree the States where equal", sjState.equals(otherState));
    }
}
