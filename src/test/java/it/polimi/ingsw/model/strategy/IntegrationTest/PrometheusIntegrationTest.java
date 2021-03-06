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
import it.polimi.ingsw.model.piece.Level1Block;
import it.polimi.ingsw.model.piece.Level2Block;
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

public class PrometheusIntegrationTest {

    Server server;
    ServerSideConnection c1;
    ServerSideConnection c2;
    ServerSideConnection c3;

    Player testPlayer;
    Player enemy1Player;
    Player enemy2Player;
    PlayerInfo testPlayerInfo;
    PlayerInfo enemy1Info;
    PlayerInfo enemy2Info;
    GodCard prometheus;

    Controller controller;
    Model model;
    ArrayList<VirtualView> virtualViews;
    GameBoard gameBoard;
    TurnInfo turnInfo;

    TestSupportFunctions testSupportFunctions=new TestSupportFunctions();

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

        testPlayerInfo = new PlayerInfo("xXoliTheQueenXx", new GregorianCalendar(1998, Calendar.SEPTEMBER, 9),3);
        testPlayer = new Player(testPlayerInfo);

        enemy1Info = new PlayerInfo("enemy1", new GregorianCalendar(1990, Calendar.NOVEMBER, 30),3);
        enemy1Player = new Player(enemy1Info);

        enemy2Info = new PlayerInfo("enemy2", new GregorianCalendar(1995, Calendar.DECEMBER, 7),3);
        enemy2Player = new Player(enemy2Info);

        //Instancing testPlayer's godcard
        String godDataString[] = {"Prometheus", "Titan Benefactor of Mankind", "Simple", "true", "If your Worker does not move up, it may build both before and after moving."};
        prometheus = new GodCard(godDataString);
        testPlayer.setGodCard(prometheus);

        ArrayList<Player> players=new ArrayList<>();
        players.add(testPlayer);
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

        testPlayer.getWorker(0).setStartingPosition(0, 0);
        testPlayer.getWorker(1).setStartingPosition(1, 0);

        enemy1Player.getWorker(0).setStartingPosition(0, 1);
        enemy1Player.getWorker(1).setStartingPosition(0, 2);

        enemy2Player.getWorker(0).setStartingPosition(0, 3);
        enemy2Player.getWorker(1).setStartingPosition(0, 4);
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

    @Nested
    class FirstChoice {

        @BeforeEach
        void init() {

            //GAMEBOARD GENERATION
            int[][] towers =
                    {
                            {2, 4, 1, 2, 2},
                            {3, 1, 2, 1, 4},
                            {4, 1, 0, 3, 4},
                            {0, 2, 2, 4, 4},
                            {3, 2, 1, 4, 0}
                    };

            gameBoard.generateBoard(towers);


            //POSITIONING TEST WORKERS
            gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
            testPlayer.getWorker(0).movedToPosition(1, 1, 1);

            gameBoard.getTowerCell(2, 3).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(2, 3, 2);

            //POSITIONING OPPONENT WORKERS

            gameBoard.getTowerCell(0, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
            enemy1Player.getWorker(0).movedToPosition(0, 1, 3);

            gameBoard.getTowerCell(2, 2).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
            enemy1Player.getWorker(1).movedToPosition(2, 2, 0);

            gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
            enemy2Player.getWorker(0).movedToPosition(4, 4, 0);

            gameBoard.getTowerCell(2, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
            enemy2Player.getWorker(1).movedToPosition(2, 4, 1);
        }

        //turn starts here, only move or build should be performed

        @Test
        void EndBeforeEverything() {

            PlayerMessage message = new PlayerEndOfTurnChoice(virtualViews.get(0), testPlayer);
            controller.update(message);
            //method returns immediately
            
            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);
        }

        @Test
        void WrongBuildBeforeEverything() {

            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(-2, 1, 2, "Block"));
            controller.update(message);
            //invalid chosenworker, should execute and give error back

            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);
        }

        @Test
        void CorrectBuildBeforeEverything() {

            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 1, 2, "Block"));
            controller.update(message);
            //invalid chosenworker, should execute and give error back

            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,true,1,1,false,false);

            assertEquals(2,gameBoard.getTowerCell(1,2).getTowerHeight());
            assertTrue(gameBoard.getTowerCell(1,2).getLevel(1).getPiece() instanceof Level2Block);
        }

        @Test
        void WrongMoveBeforeEverything() {
            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 2, 4));
            controller.update(message);
            //invalid move, denied, occupied position

            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);
        }

        @Test
        void MoveBeforeEverythingNoWin() {
            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 1, 4));
            controller.update(message);
            //valid move, no win

            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);

            assertNull(gameBoard.getTowerCell(2,3).getFirstNotPieceLevel().getWorker());
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(1,4).getFirstNotPieceLevel().getWorker());
        }

        @Test
        void MoveBeforeEverythingAndWin() {
            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 3, 2));
            controller.update(message);
            //valid move, with win

            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);

            assertNull(gameBoard.getTowerCell(2,3).getFirstNotPieceLevel().getWorker());
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(3,2).getFirstNotPieceLevel().getWorker());
        }

        @Test
        void MoveBeforeEverythingAndNoPanWin() {
            gameBoard.getTowerCell(2,3).getFirstNotPieceLevel().workerMoved();
            gameBoard.getTowerCell(0,4).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(0,4,3);

            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 0, 3));
            controller.update(message);
            //valid move, no Pan win

            //turnInfo must still have all his initial values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);

            assertNull(gameBoard.getTowerCell(0,4).getFirstNotPieceLevel().getWorker());
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(0,3).getFirstNotPieceLevel().getWorker());
        }
    }

    @Nested
    //built, should only move
    class withInitialBuild{

        @BeforeEach
        void init() {

            turnInfo.setHasBuilt();
            turnInfo.addBuild();
            turnInfo.setChosenWorker(1);

            //GAMEBOARD GENERATION
            int[][] towers =
                    {
                            {2, 4, 1, 2, 2},
                            {3, 1, 2, 1, 4},
                            {4, 2, 0, 3, 4},
                            {0, 2, 2, 4, 4},
                            {3, 2, 1, 4, 0}
                    };

            gameBoard.generateBoard(towers);


            //POSITIONING TEST WORKERS
            gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
            testPlayer.getWorker(0).movedToPosition(1, 1, 1);

            gameBoard.getTowerCell(2, 3).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(2, 3, 2);

            //POSITIONING OPPONENT WORKERS

            gameBoard.getTowerCell(0, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
            enemy1Player.getWorker(0).movedToPosition(0, 1, 3);

            gameBoard.getTowerCell(2, 2).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
            enemy1Player.getWorker(1).movedToPosition(2, 2, 0);

            gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
            enemy2Player.getWorker(0).movedToPosition(4, 4, 0);

            gameBoard.getTowerCell(2, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
            enemy2Player.getWorker(1).movedToPosition(2, 4, 1);
        }

        @Test
        void EndAfterFirstBuild(){

            PlayerMessage message = new PlayerEndOfTurnChoice(virtualViews.get(0), testPlayer);
            controller.update(message);
            //method returns immediately, turn incomplete

            //turnInfo must still have all the values it had after the fist build
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,true,1,1,false,false);

        }

        @Test
        void BuildAfterFirstBuild(){

            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 3, 2, "Block"));
            controller.update(message);
            //can't build again before moving

            //turnInfo must still have all the values it had after the fist build
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,true,1,1,false,false);

            assertEquals(3,gameBoard.getTowerCell(3,2).getTowerHeight());
        }

        @Test
        void ImpossibleMoveAfterFistBuildBecauseOfOwnPower(){
            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 3, 2));
            controller.update(message);
            //invalid move, can't move up because of his own power

            //turnInfo must still have all his initial values after the original build
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,true,1,1,false,false);

            //still in his position
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(2,3).getFirstNotPieceLevel().getWorker());
        }

        @Test
        void MoveAfterFistBuild(){

            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 1, 2));
            controller.update(message);
            //valid move, no win

            //turnInfo must have been updated considering the move
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,1,1,false,false);

            assertNull(gameBoard.getTowerCell(2,3).getFirstNotPieceLevel().getWorker());
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(1,2).getFirstNotPieceLevel().getWorker());

            assertAll(
                    ()->assertEquals(2,testPlayer.getWorker(1).getPreviousPosition().getX()),
                    ()->assertEquals(3,testPlayer.getWorker(1).getPreviousPosition().getY()),
                    ()->assertEquals(2,testPlayer.getWorker(1).getPreviousPosition().getZ()),
                    ()->assertEquals(1,testPlayer.getWorker(1).getCurrentPosition().getX()),
                    ()->assertEquals(2,testPlayer.getWorker(1).getCurrentPosition().getY()),
                    ()->assertEquals(2,testPlayer.getWorker(1).getCurrentPosition().getZ())
            );
        }

    }

    @Nested
    //build and move, should only build
    class secondBuildWithInitialBuild{

        @BeforeEach
        void init() {

            turnInfo.setHasBuilt();
            turnInfo.addBuild();
            turnInfo.setChosenWorker(1);
            turnInfo.addMove();
            turnInfo.setHasMoved();

            //GAMEBOARD GENERATION
            int[][] towers =
                    {
                            {2, 4, 1, 2, 2},
                            {3, 1, 2, 1, 4},
                            {4, 2, 0, 3, 4},
                            {0, 2, 2, 4, 4},
                            {3, 2, 1, 4, 0}
                    };

            gameBoard.generateBoard(towers);

            //POSITIONING TEST WORKERS
            gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
            testPlayer.getWorker(0).movedToPosition(1, 1, 1);

            gameBoard.getTowerCell(1, 2).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(1, 2, 2);

            //POSITIONING OPPONENT WORKERS

            gameBoard.getTowerCell(0, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
            enemy1Player.getWorker(0).movedToPosition(0, 1, 3);

            gameBoard.getTowerCell(2, 2).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
            enemy1Player.getWorker(1).movedToPosition(2, 2, 0);

            gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
            enemy2Player.getWorker(0).movedToPosition(4, 4, 0);

            gameBoard.getTowerCell(2, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
            enemy2Player.getWorker(1).movedToPosition(2, 4, 1);
        }

        @Test
        void EndAfterBuildAndMove(){

            PlayerMessage message = new PlayerEndOfTurnChoice(virtualViews.get(0), testPlayer);
            controller.update(message);
            //method returns immediately, turn incomplete

            //turnInfo must still have all the values it had after the build and the move
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,1,1,false,false);
        }

        @Test
        void MoveAfterBuildAndMove(){

            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 0, 3));
            controller.update(message);
            //can't move again

            //turnInfo must not change from the original
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,1,1,false,false);

            //not moved
            assertNull(gameBoard.getTowerCell(0,3).getFirstNotPieceLevel().getPiece());
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(1,2).getFirstNotPieceLevel().getWorker());
        }

        @Test
        void WrongSecondBuild(){

            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 0, 2, "Dome"));
            controller.update(message);
            //can build, but tower complete and type of piece is wrong, should give error regarding the complete tower

            //turnInfo must not change from the original
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,1,1,false,false);

            assertEquals(4,gameBoard.getTowerCell(0,2).getTowerHeight());
        }

        @Test
        void CorrectSecondBuild(){
            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 0, 3, "Block"));
            controller.update(message);
            //should build

            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,2,1,true,true);

            assertEquals(1,gameBoard.getTowerCell(0,3).getTowerHeight());
            assertTrue(gameBoard.getTowerCell(0,3).getLevel(0).getPiece() instanceof Level1Block);
        }

    }

    @Nested
    //built twice and moved, should only end
    class possibility1End{

        @BeforeEach
        void init() {

            turnInfo.setHasBuilt();
            turnInfo.addBuild();
            turnInfo.setChosenWorker(1);
            turnInfo.addMove();
            turnInfo.setHasMoved();
            turnInfo.setTurnCanEnd();
            turnInfo.setTurnHasEnded();

            //second build ok
            turnInfo.addBuild();

            //GAMEBOARD GENERATION
            int[][] towers =
                    {
                            {2, 4, 1, 2, 2},
                            {3, 1, 2, 1, 4},
                            {4, 2, 0, 3, 4},
                            {1, 2, 2, 4, 4},
                            {3, 2, 1, 4, 0}
                    };

            gameBoard.generateBoard(towers);


            //POSITIONING TEST WORKERS
            gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
            testPlayer.getWorker(0).movedToPosition(1, 1, 1);

            gameBoard.getTowerCell(1, 2).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(1, 2, 2);

            //POSITIONING OPPONENT WORKERS

            gameBoard.getTowerCell(0, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
            enemy1Player.getWorker(0).movedToPosition(0, 1, 3);

            gameBoard.getTowerCell(2, 2).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
            enemy1Player.getWorker(1).movedToPosition(2, 2, 0);

            gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
            enemy2Player.getWorker(0).movedToPosition(4, 4, 0);

            gameBoard.getTowerCell(2, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
            enemy2Player.getWorker(1).movedToPosition(2, 4, 1);
        }

        @Test
        void EndLongTurn(){

            PlayerMessage message = new PlayerEndOfTurnChoice(virtualViews.get(0), testPlayer);
            controller.update(message);
            //turn has ended

            //turn to be reset
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);

            assertEquals(Colour.BLUE,model.getTurn());
        }

        @Test
        void noMoreMoves(){

            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 0, 3));
            controller.update(message);
            //can't move after turn end

            //turnInfo is the same as the one after the second build
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,2,1,true,true);

            //not moved
            assertEquals(testPlayer.getWorker(1),gameBoard.getTowerCell(1,2).getFirstNotPieceLevel().getWorker());
            assertNull(gameBoard.getTowerCell(0,3).getFirstNotPieceLevel().getWorker());
        }

        @Test
        void noMoreBuilds(){
            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 0, 3, "Block"));
            controller.update(message);
            //can't build after turn end

            //turnInfo is the same as the one after the second build
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,true,2,1,true,true);

            assertEquals(1,gameBoard.getTowerCell(0,3).getTowerHeight());
            assertNull(gameBoard.getTowerCell(0,3).getFirstNotPieceLevel().getPiece());
        }
    }

    @Nested
    //moved, should only build
    class withoutInitialBuild{

        @BeforeEach
        void init() {

            turnInfo.setHasMoved();
            turnInfo.addMove();
            turnInfo.setChosenWorker(1);

            //GAMEBOARD GENERATION
            int[][] towers =
                    {
                            {2, 4, 1, 2, 2},
                            {3, 1, 2, 1, 4},
                            {4, 1, 0, 3, 4},
                            {0, 2, 2, 4, 4},
                            {3, 2, 1, 4, 0}
                    };

            gameBoard.generateBoard(towers);


            //POSITIONING TEST WORKERS
            gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
            testPlayer.getWorker(0).movedToPosition(1, 1, 1);

            gameBoard.getTowerCell(1, 4).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(1, 4, 2);

            //POSITIONING OPPONENT WORKERS

            gameBoard.getTowerCell(0, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
            enemy1Player.getWorker(0).movedToPosition(0, 1, 3);

            gameBoard.getTowerCell(2, 2).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
            enemy1Player.getWorker(1).movedToPosition(2, 2, 0);

            gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
            enemy2Player.getWorker(0).movedToPosition(4, 4, 0);

            gameBoard.getTowerCell(2, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
            enemy2Player.getWorker(1).movedToPosition(2, 4, 1);
        }

        @Test
        void endAfterMove(){
            PlayerMessage message = new PlayerEndOfTurnChoice(virtualViews.get(0), testPlayer);
            controller.update(message);
            //method returns immediately because the turn cannot end

            //turnInfo must keep his values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);
        }

        @Test
        void moveAfterMove(){
            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 1, 4));
            controller.update(message);
            //player can't move again because he has already moved, moreover he wants to move to his own position

            //turnInfo must keep his values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);
        }

        @Test
        void WrongBuildAfterMove(){
            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 2, 5, "Block"));
            controller.update(message);
            //invalid space on gameboard

            //turnInfo must keep his values
            testSupportFunctions.baseTurnInfoChecker(turnInfo,true,1,false,0,1,false,false);

        }

        @Test
        void CorrectBuildAfterMove() {
            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 0, 4, "Dome"));
            controller.update(message);
            //build ok

            //turnInfo must keep his values
            testSupportFunctions.baseTurnInfoChecker(turnInfo, true, 1, true, 1, 1, true, true);

            assertEquals(4, gameBoard.getTowerCell(0, 4).getTowerHeight());
            assertTrue(gameBoard.getTowerCell(0, 4).getLevel(3).getPiece() instanceof Dome);
        }


    }

    @Nested
    //move and then built, should only end
    class possibility2End{

        @BeforeEach
        void init() {

            turnInfo.setHasMoved();
            turnInfo.addMove();
            turnInfo.setChosenWorker(1);
            turnInfo.setHasBuilt();
            turnInfo.addBuild();
            turnInfo.setTurnCanEnd();
            turnInfo.setTurnHasEnded();

            //GAMEBOARD GENERATION
            int[][] towers =
                    {
                            {2, 4, 1, 2, 2},
                            {3, 1, 2, 1, 4},
                            {4, 1, 0, 3, 4},
                            {0, 2, 2, 4, 4},
                            {4, 2, 1, 4, 0}
                    };

            gameBoard.generateBoard(towers);


            //POSITIONING TEST WORKERS
            gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
            testPlayer.getWorker(0).movedToPosition(1, 1, 1);

            gameBoard.getTowerCell(1, 4).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
            testPlayer.getWorker(1).movedToPosition(1, 4, 2);

            //POSITIONING OPPONENT WORKERS

            gameBoard.getTowerCell(0, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
            enemy1Player.getWorker(0).movedToPosition(0, 1, 3);

            gameBoard.getTowerCell(2, 2).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
            enemy1Player.getWorker(1).movedToPosition(2, 2, 0);

            gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
            enemy2Player.getWorker(0).movedToPosition(4, 4, 0);

            gameBoard.getTowerCell(2, 4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
            enemy2Player.getWorker(1).movedToPosition(2, 4, 1);
        }

        @Test
        void endAfterMoveAndBuild(){
            PlayerMessage message = new PlayerEndOfTurnChoice(virtualViews.get(0), testPlayer);
            controller.update(message);
            //ok, turn must end

            //turnInfo reset
            testSupportFunctions.baseTurnInfoChecker(turnInfo,false,0,false,0,-1,false,false);
            assertEquals(Colour.BLUE,model.getTurn());

        }

        @Test
        void moveAfterMoveAndBuild(){

            PlayerMessage message = new PlayerMovementChoice(virtualViews.get(0), testPlayer, new MoveData(1, 1, 4));
            controller.update(message);
            //player can't move again because the turn has ended, moreover he wants to move to his own position
            testSupportFunctions.baseTurnInfoChecker(turnInfo, true, 1, true, 1, 1, true, true);
        }

        @Test
        void buildAfterMoveAndBuild(){
            PlayerMessage message = new PlayerBuildChoice(virtualViews.get(0), testPlayer, new BuildData(1, 0, 4, "Dome"));
            controller.update(message);
            //can't move after turn end, and can't build on top of dome (should not arrive here)

            //turnInfo must keep his values
            testSupportFunctions.baseTurnInfoChecker(turnInfo, true, 1, true, 1, 1, true, true);
        }

    }

    @Test
    void winnerForOtherLoss(){

        //GAMEBOARD GENERATION
        int[][] towers =
                {
                        {2, 4, 1, 2, 2},
                        {4, 3, 2, 1, 4},
                        {4, 1, 0, 3, 4},
                        {0, 2, 2, 1, 4},
                        {3, 2, 1, 4, 0}
                };

        gameBoard.generateBoard(towers);

        turnInfo.activateAthenaPower();
        model.setEliminated(enemy2Player);

        //POSITIONING TEST WORKERS
        gameBoard.getTowerCell(0, 0).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(0));
        testPlayer.getWorker(0).movedToPosition(0, 0, 2);

        gameBoard.getTowerCell(4, 4).getFirstNotPieceLevel().setWorker(testPlayer.getWorker(1));
        testPlayer.getWorker(1).movedToPosition(4, 4, 0);

        //POSITIONING OPPONENT WORKERS

        gameBoard.getTowerCell(1, 1).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
        enemy1Player.getWorker(0).movedToPosition(1, 1, 3);

        gameBoard.getTowerCell(2, 0).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
        enemy1Player.getWorker(1).movedToPosition(2, 0, 1);

        PlayerMessage move=new PlayerMovementChoice(virtualViews.get(0),testPlayer,new MoveData(1,3,3));
        controller.update(move);
        //testPlayer should lose, and be removed from the gameboard
        assertNull(gameBoard.getTowerCell(0,0).getFirstNotPieceLevel().getWorker());
        assertNull(gameBoard.getTowerCell(4,4).getFirstNotPieceLevel().getWorker());
        assertTrue(model.isEliminated(testPlayer));
        assertEquals(1,model.getPlayersLeft());

        //test player should be declared beaten and enemy1 should win

        assertFalse(virtualViews.get(0).isObservingModel());
        assertFalse(virtualViews.get(1).isObservingModel());
        assertFalse(virtualViews.get(2).isObservingModel());
    }



}
