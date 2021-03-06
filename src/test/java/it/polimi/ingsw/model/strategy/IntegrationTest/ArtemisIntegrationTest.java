package it.polimi.ingsw.model.strategy.IntegrationTest;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.messages.PlayerToGameMessages.CompleteMessages.PlayerBuildChoice;
import it.polimi.ingsw.messages.PlayerToGameMessages.CompleteMessages.PlayerEndOfTurnChoice;
import it.polimi.ingsw.messages.PlayerToGameMessages.CompleteMessages.PlayerMessage;
import it.polimi.ingsw.messages.PlayerToGameMessages.CompleteMessages.PlayerMovementChoice;
import it.polimi.ingsw.messages.PlayerToGameMessages.DataMessages.BuildData;
import it.polimi.ingsw.messages.PlayerToGameMessages.DataMessages.MoveData;
import it.polimi.ingsw.messages.PlayerToGameMessages.DataMessages.PlayerInfo;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.piece.Dome;
import it.polimi.ingsw.model.piece.Level3Block;
import it.polimi.ingsw.server.Server;
import it.polimi.ingsw.server.ServerSideConnection;
import it.polimi.ingsw.supportClasses.FakeConnection;
import it.polimi.ingsw.supportClasses.TestSupportFunctions;
import it.polimi.ingsw.view.VirtualView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

public class ArtemisIntegrationTest {

    Server server;
    ServerSideConnection c1;
    ServerSideConnection c2;
    ServerSideConnection c3;

    Player player;
    Player enemy1Player;
    Player enemy2Player;
    PlayerInfo playerInfo;
    PlayerInfo enemy1Info;
    PlayerInfo enemy2Info;
    GodCard artemisCard;

    Controller controller;
    Model model;
    ArrayList<VirtualView> virtualViews;
    GameBoard gameBoard;
    TurnInfo turnInfo;

    TestSupportFunctions testMethods=new TestSupportFunctions();

    @BeforeEach
    void init() {

        try {
            server = new Server();
            c1 = new FakeConnection(new Socket(),server,"c1");
            c2 = new FakeConnection(new Socket(),server,"c2");
            c3 = new FakeConnection(new Socket(),server,"c3");
        } catch (IOException e) {
            e.printStackTrace();
        }

        playerInfo  =new PlayerInfo("xXoliTheQueenXx",new GregorianCalendar(1998, Calendar.SEPTEMBER, 9),3);
        player = new Player(playerInfo);

        enemy1Info  =new PlayerInfo("enemy1",new GregorianCalendar(1999, Calendar.NOVEMBER, 30),3);
        enemy1Player = new Player(enemy1Info);

        enemy2Info  =new PlayerInfo("enemy2",new GregorianCalendar(2000, Calendar.DECEMBER, 7),3);
        enemy2Player = new Player(enemy2Info);

        //Instancing testPlayer's godcard
        String godDataString[] = {"Artemis","God Of ...", "Simple", "true", "Your worker may move another time..."};
        artemisCard = new GodCard(godDataString);
        player.setGodCard(artemisCard);

        ArrayList<Player> players=new ArrayList<>();
        players.add(player);
        players.add(enemy1Player);
        players.add(enemy2Player);
        ArrayList<ServerSideConnection> connections=new ArrayList<>();
        connections.add(c1);
        connections.add(c2);
        connections.add(c3);

        controller=new Controller(players,connections);

        virtualViews=controller.getVirtualViews();
        model=controller.getModel();
        turnInfo=model.getTurnInfo();
        gameBoard=model.getGameBoard();
        
        player.getWorker(0).setStartingPosition(3,0);
        player.getWorker(1).setStartingPosition(0,1);
        
        enemy1Player.getWorker(0).setStartingPosition(4,1);
        enemy1Player.getWorker(1).setStartingPosition(3,2);
        
        enemy2Player.getWorker(0).setStartingPosition(1,4);
        enemy2Player.getWorker(1).setStartingPosition(4,4);

        //"xXoliTheQueenXx" is the oldest player. but the one i want to test
        model.setColour(Colour.PURPLE);

        //GAMEBOARD GENERATION
        int[][] towers=
                {
                        {0,0,2,2,2},
                        {0,0,0,0,3},
                        {0,1,0,3,4},
                        {0,0,1,2,3},
                        {0,0,1,0,0}
                };

        gameBoard.generateBoard(towers);

        //POSITIONING OTHER DOMES

        gameBoard.getTowerCell(1,2).getFirstNotPieceLevel().setPiece(new Dome());
        gameBoard.getTowerCell(1,2).increaseTowerHeight();
        gameBoard.getTowerCell(1,2).checkCompletion();

        gameBoard.getTowerCell(1,3).getFirstNotPieceLevel().setPiece(new Dome());
        gameBoard.getTowerCell(1,3).increaseTowerHeight();
        gameBoard.getTowerCell(1,3).checkCompletion();

        //POSITIONING TEST WORKERS
        gameBoard.getTowerCell(2,0).getFirstNotPieceLevel().setWorker(player.getWorker(0));
        player.getWorker(0).movedToPosition(2,0,2);

        gameBoard.getTowerCell(0,2).getFirstNotPieceLevel().setWorker(player.getWorker(1));
        player.getWorker(1).movedToPosition(0,2,0);

        //POSITIONING ENEMY WORKERS

        gameBoard.getTowerCell(4,0).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
        enemy1Player.getWorker(0).movedToPosition(4,0,2);


        gameBoard.getTowerCell(4,3).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
        enemy1Player.getWorker(1).movedToPosition(4,3,3);


        gameBoard.getTowerCell(2,4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
        enemy2Player.getWorker(0).movedToPosition(2,4,1);

        gameBoard.getTowerCell(3,4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
        enemy2Player.getWorker(1).movedToPosition(3,4,0);

        //TURN RESET
        turnInfo.turnInfoReset();

    }

    @AfterEach
    void end(){
        //closing serverSocket
        try {
            server.closeServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////COMPLETE TURNS//////////////////////////////////////////////////////////
    @Test
    void CompleteTurnTesting(){

        //testPlayer's worker0 starting position is (2,0,2)

        //Setting the right turn manually
        model.setColour(player.getColour());

        //////////////////////////////////////////MOVING FOR THE FIRST TIME/////////////////////////////

        //creating message that should trigger the controller object (in this case, triggering will be "manual")
        PlayerMovementChoice moveMessage = new PlayerMovementChoice(virtualViews.get(0),player, new MoveData(0,3,0));
        controller.update(moveMessage);

        //Artemis has moved not using his power. Did he move correctly?
        assertEquals((new Position(3, 0, 2)), player.getWorker(0).getCurrentPosition());
        assertEquals((new Position(2, 0, 2)), player.getWorker(0).getPreviousPosition());
        assertEquals(player.getWorker(0),gameBoard.getTowerCell(3,0).getFirstNotPieceLevel().getWorker());
        assertNull(gameBoard.getTowerCell(2,0).getFirstNotPieceLevel().getWorker());

        assertTrue(turnInfo.getHasAlreadyMoved());
        assertEquals(1,turnInfo.getNumberOfMoves());
        assertEquals(0,turnInfo.getChosenWorker());

        ////////////////////////////////////TRYING TO MOVE ANOTHER TIME BUT IN THE FIRST POSITION/////////////////////////////////

        moveMessage = new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(0,2,0));
        controller.update(moveMessage);

        //should not move
        assertEquals((new Position(3, 0, 2)), player.getWorker(0).getCurrentPosition());
        assertEquals((new Position(2, 0, 2)), player.getWorker(0).getPreviousPosition());
        assertEquals(player.getWorker(0),gameBoard.getTowerCell(3,0).getFirstNotPieceLevel().getWorker());
        assertNull(gameBoard.getTowerCell(2,0).getFirstNotPieceLevel().getWorker());

        assertTrue(turnInfo.getHasAlreadyMoved());
        assertEquals(1,turnInfo.getNumberOfMoves());
        assertEquals(0,turnInfo.getChosenWorker());

        ////////////////////////////////////TRYING TO MOVE ANOTHER TIME/////////////////////////////////

        moveMessage = new PlayerMovementChoice(virtualViews.get(0),player, new MoveData(0,3,1));
        controller.update(moveMessage);

        //should move
        assertEquals((new Position(3, 1, 0)), player.getWorker(0).getCurrentPosition());
        assertEquals((new Position(3, 0, 2)), player.getWorker(0).getPreviousPosition());
        assertEquals(player.getWorker(0),gameBoard.getTowerCell(3,1).getFirstNotPieceLevel().getWorker());
        assertNull(gameBoard.getTowerCell(3,0).getFirstNotPieceLevel().getWorker());

        assertTrue(turnInfo.getHasAlreadyMoved());
        assertEquals(2,turnInfo.getNumberOfMoves());
        assertEquals(0,turnInfo.getChosenWorker());

        //////////////////////////////////////BUILDING FOR THE FIRST TIME////////////////////////////////

        //creating build message
        PlayerBuildChoice buildMessage = new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(0,3,0,"Block"));
        controller.update(buildMessage);

        //Artemis has built a level1block with his basicBuild strategy

        //checking that tower's height increased
        assertEquals(3, gameBoard.getTowerCell(3, 0).getTowerHeight());
        //checking that the piece is right
        assertTrue(gameBoard.getTowerCell(3,0).getLevel(2).getPiece() instanceof Level3Block);
        //checking that tower is not completed
        assertFalse(gameBoard.getTowerCell(3, 0).isTowerCompleted());
        //checking that hasBuilt is true
        assertTrue(turnInfo.getHasAlreadyBuilt());

        //////////////////////////////////TRYING TO BUILD AGAIN//////////////////////////////////////////

        //creating build message
        buildMessage = new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(0,2,0,"Block"));
        controller.update(buildMessage);


        //checking that tower's height didn't increase
        assertEquals(3, gameBoard.getTowerCell(3, 0).getTowerHeight());
        //checking that the piece is right
        assertTrue(gameBoard.getTowerCell(3,0).getLevel(2).getPiece() instanceof Level3Block);

        //checking that hasAlreadyBuilt is still true
        assertTrue(turnInfo.getHasAlreadyBuilt());
        //checking that hasAlreadyMoved is still true
        assertTrue(turnInfo.getHasAlreadyMoved());

    }

    //////////////////////////////////////////FIRST CHOICE//////////////////////////////////////////////////////////////
    @Test
    void EndBeforeEverything(){

        PlayerMessage message=new PlayerEndOfTurnChoice(virtualViews.get(0),player);
        controller.update(message);
        //method returns immediately

        //turnInfo must still have all his initial values
        testMethods.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);

    }

    @Test
    void BuildBeforeEverything(){

        PlayerMessage message=new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(1,1,1,"Block"));
        controller.update(message);

        //turnInfo must still have all his initial values
        testMethods.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);

    }

    @Test
    void WrongMoveBeforeEverything(){
        PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(0,2,2));
        controller.update(message);
        //invalid move, denied

        //turnInfo must still have all his initial values
        testMethods.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);

    }

    @Test
    void WrongMoveBeforeEverything2(){
        PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(-1,1,0));
        controller.update(message);
        //invalid move, denied, invalid worker

        //turnInfo must still have all his initial values
        testMethods.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);
    }

    @Test //ok
    void CorrectMoveBeforeEverything(){
        PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(1,0,3));
        controller.update(message);

        //turnInfo must have been modified
        testMethods.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);

    }

    @Test //ok
    void CorrectMoveAndWinBasic(){
        //repositioning worker
        gameBoard.getTowerCell(2,0).getFirstNotPieceLevel().workerMoved();
        gameBoard.getTowerCell(3,0).getFirstNotPieceLevel().setWorker(player.getWorker(0));
        player.getWorker(0).movedToPosition(3,0,2);

        PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(0,4,1));
        controller.update(message);
        //should return victory message

        //turnInfo must have been modified
        testMethods.baseTurnInfoChecker(turnInfo,true,1,false,0,0,false,false);

    }

    /////////////////////////////////////////SECOND CHOICE//////////////////////////////////////////////////////////////
    @Nested
    class SecondChoice {

        @BeforeEach
        void init() {

            //SETS INITIAL STATE
            turnInfo.setChosenWorker(0);
            turnInfo.setHasMoved();
            turnInfo.addMove();

            //player's worker0 is in (2,0,2)

        }

        //move done, only build should be performed

        @Test
        void EndAftertMove() {

            PlayerMessage message=new PlayerEndOfTurnChoice(virtualViews.get(0),player);
            controller.update(message);
            //method returns immediately

            //turnInfo must still have all his initial values
            testMethods.baseTurnInfoChecker(turnInfo,true,1,false,0,0,false,false);

        }

        @Test
        void MoveAfterMove() {

            PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(0,1,0));
            controller.update(message);
            //method returns because the player has already moved

            //should have moved
            assertEquals((new Position(1, 0, 0)), player.getWorker(0).getCurrentPosition());
            assertEquals((new Position(2, 0, 2)), player.getWorker(0).getPreviousPosition());
            assertEquals(player.getWorker(0),gameBoard.getTowerCell(1,0).getFirstNotPieceLevel().getWorker());
            assertNull(gameBoard.getTowerCell(2,0).getFirstNotPieceLevel().getWorker());

            testMethods.baseTurnInfoChecker(turnInfo,true,2,false,0,0,false,false);

        }

        @Test
        void WrongBuildAfterMove() {
            PlayerMessage message=new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(0,3,0,"Dome"));
            controller.update(message);
            //every parameter is wrong, should give error for the wrong block

            //turnInfo must not have been modified since this class's BeforeEach
            testMethods.baseTurnInfoChecker(turnInfo,true,1,false,0,0,false,false);

        }

        @Test
        void WrongBuildAfterMove2() {
            PlayerMessage message=new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(1,3,0,"Block"));
            controller.update(message);
            //wrong worker

            //turnInfo must not have been modified since this class's BeforeEach
            testMethods.baseTurnInfoChecker(turnInfo,true,1,false,0,0,false,false);

        }

        @Test //ok
        void BuildAfterMove() {
            PlayerMessage message=new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(0,3,0,"Block"));
            controller.update(message);
            //should work

            //turnInfo must have been updated
            testMethods.baseTurnInfoChecker(turnInfo,true,1,true,1,0,true,true);

        }
    }

    /////////////////////////////////////////THIRD CHOICE///////////////////////////////////////////////////////////////
    @Nested
    class ThirdChoice {

        @BeforeEach
        void init() {

            //SETS INITIAL STATE
            turnInfo.setChosenWorker(1);
            turnInfo.setHasMoved();
            turnInfo.addMove();
            turnInfo.setHasBuilt();
            turnInfo.addBuild();
            turnInfo.setTurnCanEnd();
            turnInfo.setTurnHasEnded();
            player.getWorker(1).setStartingPosition(2,0);
            player.getWorker(1).movedToPosition(3,0,2);


        }

        //turn finishes here, only end should be performed

        @Test
        void MoveAfterFinish() {
            PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(1,0,3));
            controller.update(message);
            //can't execute because turn has ended

            //turnInfo Must be the initial one
            testMethods.baseTurnInfoChecker(turnInfo,true,1,true,1,1,true,true);
        }

        @Test
        void BuildAfterFinish() {
            PlayerMessage message=new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(1,0,3,"Block"));
            controller.update(message);
            //can't execute because turn has ended

            //turnInfo Must be the initial one
            testMethods.baseTurnInfoChecker(turnInfo,true,1,true,1,1,true,true);

        }

        @Test
        void EndAfterFinish() {
            PlayerMessage message=new PlayerEndOfTurnChoice(virtualViews.get(0),player);
            controller.update(message);
            //correct end of turn

            //turnInfo must have been reset
            testMethods.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);

        }

    }

    /////////////////////////////////////////THIRD CHOICE-MOVING A SECOND TIME///////////////////////////////////////////////////////////////
    @Nested
    class ThirdChoice2 {

        @BeforeEach
        void init() {

            //SETS INITIAL STATE
            turnInfo.setChosenWorker(1);
            turnInfo.setHasMoved();
            turnInfo.addMove();

            player.getWorker(1).setStartingPosition(2,0);
            player.getWorker(1).movedToPosition(3,0,2);


        }


        @Test
        void MovingSecondTime() {

            PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(1,2,0));
            controller.update(message);
            //cannot

            //turnInfo must be same
            testMethods.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);

        }
    }

    /////////////////////////////////////////FOURTH CHOICE (Artemis has moved twice)///////////////////////////////////////////////////////
    @Nested
    class FourthChoice {

        @BeforeEach
        void init() {

            //SETS INITIAL STATE
            turnInfo.setChosenWorker(1);
            turnInfo.setHasMoved();
            turnInfo.addMove();
            turnInfo.addMove();
            turnInfo.setHasBuilt();
            turnInfo.addBuild();
            turnInfo.setTurnCanEnd();
            turnInfo.setTurnHasEnded();

            //player's worker1 is in (0,2,0)

        }

        //turn finishes here, only end should be performed

        @Test
        void MoveAfterFinish() {
            PlayerMessage message=new PlayerMovementChoice(virtualViews.get(0),player,new MoveData(1,0,3));
            controller.update(message);
            //can't execute because turn has ended

            //turnInfo Must be the initial one
            testMethods.baseTurnInfoChecker(turnInfo,true,2,true,1,1,true,true);
        }

        @Test
        void BuildAfterFinish() {
            PlayerMessage message=new PlayerBuildChoice(virtualViews.get(0),player,new BuildData(1,0,3,"Block"));
            controller.update(message);
            //can't execute because turn has ended

            //turnInfo Must be the initial one
            testMethods.baseTurnInfoChecker(turnInfo,true,2,true,1,1,true,true);

        }

        @Test
        void EndAfterFinish() {
            PlayerMessage message=new PlayerEndOfTurnChoice(virtualViews.get(0),player);
            controller.update(message);
            //correct end of turn

            //turnInfo must have been reset
            testMethods.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);

        }
    }
}
