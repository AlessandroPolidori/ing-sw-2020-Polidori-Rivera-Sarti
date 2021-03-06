package it.polimi.ingsw.model.strategy.movestrategy;

import it.polimi.ingsw.messages.GameToPlayerMessages.Others.GameMessage;
import it.polimi.ingsw.messages.PlayerToGameMessages.DataMessages.PlayerInfo;
import it.polimi.ingsw.model.GameBoard;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Position;
import it.polimi.ingsw.model.TurnInfo;
import it.polimi.ingsw.supportClasses.TestSupportFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

class ApolloMoveTest {

    ApolloMove apolloMove = new ApolloMove();
    TurnInfo turnInfo;
    GameBoard gameBoard;
    int[] movingTo = new int[2];

    TestSupportFunctions testMethods = new TestSupportFunctions();
    PlayerInfo playerInfo  = new PlayerInfo("xXoliTheQueenXx",new GregorianCalendar(1998, Calendar.SEPTEMBER, 9),3);
    Player player = new Player(playerInfo);
    PlayerInfo enemy1Info  =new PlayerInfo("enemy1",new GregorianCalendar(2000, Calendar.NOVEMBER, 30),3);
    Player enemy1Player = new Player(playerInfo);
    PlayerInfo enemy2Info  =new PlayerInfo("enemy2",new GregorianCalendar(1999, Calendar.DECEMBER, 7),3);
    Player enemy2Player = new Player(playerInfo);



    @BeforeEach
    void init(){

        gameBoard = new GameBoard();

        turnInfo = new TurnInfo();

        testMethods.drawnGameBoardInit(playerInfo,enemy1Info,enemy2Info,player,enemy1Player,enemy2Player,gameBoard,turnInfo);
        //POSITIONING WORKERS

        gameBoard.getTowerCell(4,0).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(0));
        enemy1Player.getWorker(0).movedToPosition(4,0,2);


        gameBoard.getTowerCell(4,3).getFirstNotPieceLevel().setWorker(enemy1Player.getWorker(1));
        enemy1Player.getWorker(1).movedToPosition(4,3,3);


        gameBoard.getTowerCell(2,4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(0));
        enemy2Player.getWorker(0).movedToPosition(2,4,1);

        gameBoard.getTowerCell(3,4).getFirstNotPieceLevel().setWorker(enemy2Player.getWorker(1));
        enemy2Player.getWorker(1).movedToPosition(3,4,0);
    }

    @Test
    void everyCheck_checkMove_Testing(){

        //alreadyMoved must be false
        movingTo[0]=1;
        movingTo[1]=1;
        turnInfo.setHasMoved();
        assertEquals(GameMessage.alreadyMoved, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));
        turnInfo.turnInfoReset();

        //x and y must be inside the board
        movingTo[0]=28;
        movingTo[1]=1;
        assertEquals(GameMessage.notInGameBoard, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //workerPosition must not be the destination position
        movingTo[0]=player.getWorker(0).getCurrentPosition().getX();
        movingTo[1]=player.getWorker(0).getCurrentPosition().getY();
        assertEquals(GameMessage.notOwnPosition, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //workerPosition must be adjacent to destination position
        movingTo[0]=2;
        movingTo[1]=0;
        assertEquals(GameMessage.notInSurroundings, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //towerCell must not be completed by a dome
        player.getWorker(0).movedToPosition(0,3,0);
        movingTo[0]=1;
        movingTo[1]=3;
        assertEquals(GameMessage.noMoveToCompleteTower, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //towercell height must be <= (worker height +1)
        player.getWorker(0).movedToPosition(3,1,0);
        movingTo[0]=3;
        movingTo[1]=2;
        assertEquals(GameMessage.noHighJump, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //towercell can contain an enemy worker
        player.getWorker(0).movedToPosition(3,0,2);
        movingTo[0]=4;
        movingTo[1]=0;
        assertEquals(GameMessage.moveOK, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //towercell cannot contain your other worker
        player.getWorker(0).movedToPosition(3,0,2);
        player.getWorker(1).movedToPosition(4,0,2);
        gameBoard.getTowerCell(4,0).getFirstNotPieceLevel().setWorker(player.getWorker(1));
        movingTo[0]=4;
        movingTo[1]=0;
        assertEquals(GameMessage.noMovedToOccupiedTowerApollo, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

        //if Athena's power is active, worker can not move up
        turnInfo.activateAthenaPower();
        player.getWorker(0).movedToPosition(2,2,0);
        movingTo[0]=2;
        movingTo[1]=3;
        assertEquals(GameMessage.athenaNoMoveUp, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));
        turnInfo.deactivateAthenaPower();

        //move ok
        assertEquals(GameMessage.moveOK, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));

    }

    @Test
    void differentScenarios_move_Testing(){

        //Apollo using his power. From a no-block towercell to a level1block occupied by enemy2 worker0

        turnInfo.turnInfoReset();
        player.getWorker(0).movedToPosition(1,4,0);
        movingTo[0]=2;
        movingTo[1]=4;
        assertEquals(GameMessage.moveOK, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));
        assertEquals(GameMessage.buildRequest, apolloMove.move(turnInfo, gameBoard,player,0,movingTo));

        assertEquals((new Position(2, 4, 1)), player.getWorker(0).getCurrentPosition());
        assertEquals((new Position(1, 4, 0)), enemy2Player.getWorker(0).getCurrentPosition());
        assertEquals((new Position(2, 4, 1)), enemy2Player.getWorker(0).getPreviousPosition());
        assertEquals((new Position(1, 4, 0)), player.getWorker(0).getPreviousPosition());
        assertEquals(enemy2Player.getWorker(0),gameBoard.getTowerCell(1,4).getFirstNotPieceLevel().getWorker());
        assertEquals(player.getWorker(0),gameBoard.getTowerCell(2,4).getFirstNotPieceLevel().getWorker());

        assertTrue(turnInfo.getHasAlreadyMoved());
        assertEquals(1,turnInfo.getNumberOfMoves());
        assertEquals(0,turnInfo.getChosenWorker());

        //Apollo not using his power. From a level3block to a level2block

        turnInfo.turnInfoReset();
        player.getWorker(0).movedToPosition(3,2,3);
        movingTo[0]=3;
        movingTo[1]=3;
        assertEquals(GameMessage.moveOK, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));
        assertEquals(GameMessage.buildRequest, apolloMove.move(turnInfo, gameBoard,player,0,movingTo));
        assertTrue((new Position(3,3,2)).equals(player.getWorker(0).getCurrentPosition()));
        assertNull(gameBoard.getTowerCell(3,2).getFirstNotPieceLevel().getWorker());
        assertEquals(player.getWorker(0),gameBoard.getTowerCell(3,3).getFirstNotPieceLevel().getWorker());

        assertTrue(turnInfo.getHasAlreadyMoved());
        assertEquals(1,turnInfo.getNumberOfMoves());
        assertEquals(0,turnInfo.getChosenWorker());

        //Apollo using his power. From a level2block to a levelzero towercell occupied by enemy2 worker1

        turnInfo.turnInfoReset();
        player.getWorker(0).movedToPosition(3,3,2);
        movingTo[0]=3;
        movingTo[1]=4;
        assertEquals(GameMessage.moveOK, apolloMove.checkMove(turnInfo, gameBoard,player,0,movingTo));
        assertEquals(GameMessage.buildRequest, apolloMove.move(turnInfo, gameBoard,player,0,movingTo));
        assertTrue((new Position(3,4,0)).equals(player.getWorker(0).getCurrentPosition()));
        assertTrue((new Position(3,3,2)).equals(player.getWorker(0).getPreviousPosition()));
        assertTrue((new Position(3,3,2)).equals(enemy2Player.getWorker(1).getCurrentPosition()));
        assertTrue((new Position(3,4,0)).equals(enemy2Player.getWorker(1).getPreviousPosition()));
        assertEquals(enemy2Player.getWorker(1),gameBoard.getTowerCell(3,3).getFirstNotPieceLevel().getWorker());
        assertEquals(player.getWorker(0),gameBoard.getTowerCell(3,4).getFirstNotPieceLevel().getWorker());

        assertTrue(turnInfo.getHasAlreadyMoved());
        assertEquals(1,turnInfo.getNumberOfMoves());
        assertEquals(0,turnInfo.getChosenWorker());

    }

}