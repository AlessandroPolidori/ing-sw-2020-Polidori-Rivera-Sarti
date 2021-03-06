package it.polimi.ingsw.model;

/**
 * This class contains all the information regarding the current turn
 */

public class TurnInfo {

    //da usare per verificare che il non possano essere fatte altre mosse nel turno
    private boolean turnHasEnded;

    //da usare per verificare che il turno può finire se viene mandato un messaggio di fine turno
    private boolean turnCanEnd;

    //da usare per check attivazione potere di Atena
    private boolean athenaPowerActive;

    //da usare con move base
    private boolean hasAlreadyMoved;

    //da usare con build base
    private boolean hasAlreadyBuilt;

    //da usare con move multiple
    private int numberOfMoves;

    private int numberOfBuilds;

    //da usare per verificare che move e build siano fatte con lo stesso worker
    private int chosenWorker;

    //da usare con Demetra, check seconda costruzione
    private final int[] lastBuildCoordinates;

    public TurnInfo(){

        turnHasEnded=false;
        turnCanEnd=false;
        athenaPowerActive=false;
        hasAlreadyMoved=false;
        hasAlreadyBuilt=false;
        numberOfMoves=0;
        numberOfBuilds=0;

        chosenWorker=-1;
        lastBuildCoordinates = new int[]{-1, -1};
    }


    /**
     * Resets all attributes excluding AthenaPowerActive
     */
    public void turnInfoReset(){

        turnHasEnded=false;
        turnCanEnd=false;
        hasAlreadyMoved=false;
        hasAlreadyBuilt=false;
        numberOfMoves=0;
        numberOfBuilds=0;

        chosenWorker=-1;
        lastBuildCoordinates[0]=-1;
        lastBuildCoordinates[1]=-1;
    }

    public boolean getTurnHasEnded(){return turnHasEnded;}
    public void setTurnHasEnded(){turnHasEnded=true;}

    public boolean getTurnCanEnd(){return turnCanEnd;}
    public void setTurnCanEnd(){turnCanEnd=true;}

    public boolean getAthenaPowerActive(){return athenaPowerActive;}
    public void activateAthenaPower(){athenaPowerActive=true;}
    public void deactivateAthenaPower(){athenaPowerActive=false;}


    public boolean getHasAlreadyMoved(){return hasAlreadyMoved;}
    public void setHasMoved(){hasAlreadyMoved=true;}

    public boolean getHasAlreadyBuilt(){return hasAlreadyBuilt;}
    public void setHasBuilt(){hasAlreadyBuilt=true;}

    public int getNumberOfMoves(){return numberOfMoves;}
    public void addMove(){numberOfMoves++;}

    public int getNumberOfBuilds(){return numberOfBuilds;}
    public void addBuild(){numberOfBuilds++;}

    public int getChosenWorker(){return chosenWorker;}
    public void setChosenWorker(int n){chosenWorker=n;}

    public int[] getLastBuildCoordinates(){return lastBuildCoordinates;}
    public void setLastBuildCoordinates(int x, int y){
        lastBuildCoordinates[0]=x;
        lastBuildCoordinates[1]=y;
    }





}
