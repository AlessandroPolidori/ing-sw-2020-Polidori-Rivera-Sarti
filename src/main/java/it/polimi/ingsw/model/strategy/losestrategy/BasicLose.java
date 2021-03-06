package it.polimi.ingsw.model.strategy.losestrategy;

import it.polimi.ingsw.model.GameBoard;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.TurnInfo;

/**
 * basicLose is based on the idea that a player has to move before he can build, therefore it can't
 * be applied to Gods such as prometheus
 */
public class BasicLose implements LoseStrategy {

    /**
     * The player loses if none of his worker can move
     * @param turnInfo containing information about current turn
     * @param gameBoard object representing the gameboard
     * @param player player whose losing condition has to be checked
     * @param chosenWorker is not used
     * @return a boolean which says if the player lost or not
     */
    @Override
    public boolean movementLoss(TurnInfo turnInfo, GameBoard gameBoard, Player player, int chosenWorker) {

        //lose condition checked only before first movement
        if(turnInfo.getHasAlreadyMoved()){
            return false;
        }

        int possibility = 16;
        int x;
        int y;
        int z;

        for(int w=0;w<2;w++){
            x = player.getWorker(w).getCurrentPosition().getX();
            y = player.getWorker(w).getCurrentPosition().getY();
            z = player.getWorker(w).getCurrentPosition().getZ();

            for(int j=y-1;j<=y+1;j++){
                for(int i=x-1;i<=x+1;i++){

                    if(!(j==y && i==x)) {
                        if (j < 0 || j > 4 || i < 0 || i > 4 ||
                                gameBoard.getTowerCell(i,j).isTowerCompleted() ||
                                gameBoard.getTowerCell(i,j).hasWorkerOnTop() ||
                                gameBoard.getTowerCell(i,j).getTowerHeight() > z + 1 ||
                                (turnInfo.getAthenaPowerActive() && gameBoard.getTowerCell(i,j).getTowerHeight() > z)) {

                            possibility--;
                        }
                    }
                }
            }
        }

        return possibility==0;
    }

    /**
     * The player loses if he cannot complete the build phase because not allowed to build
     * @param turnInfo containing information about current turn
     * @param gameBoard object representing the gameboard
     * @param player player whose losing condition has to be checked
     * @param chosenWorker is the number of the worker to check
     * @return a boolean which says if the player lost or not
     */
    @Override
    public boolean buildingLoss(TurnInfo turnInfo, GameBoard gameBoard, Player player, int chosenWorker) {

        //lose condition checked only after a movement, and only on first build. chosenworker cant't be unsetted if the player moved
        if(!turnInfo.getHasAlreadyMoved() || turnInfo.getHasAlreadyBuilt()){
            return false;
        }

        int possibility = 8;
        int x;
        int y;

        //only the worker that moved must be checked
        x = player.getWorker(turnInfo.getChosenWorker()).getCurrentPosition().getX();
        y = player.getWorker(turnInfo.getChosenWorker()).getCurrentPosition().getY();

        for(int j=y-1;j<=y+1;j++){
            for(int i=x-1; i<=x+1;i++){

                if(!(j==y && i==x)){

                    if( j<0 || j>4 || i<0 || i>4 ||
                        gameBoard.getTowerCell(i,j).isTowerCompleted() ||
                        gameBoard.getTowerCell(i,j).hasWorkerOnTop()){

                        possibility--;
                    }
                }
            }
        }
        return possibility == 0;
    }
}
