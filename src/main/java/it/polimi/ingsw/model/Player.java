package it.polimi.ingsw.model;

import it.polimi.ingsw.messages.PlayerToGameMessages.DataMessages.PlayerInfo;

import java.util.Calendar;

/**
 * This class has all the information regarding the players
 */
public class Player{

    private final String nickname;
    private final Calendar birthday;
    private final Worker[] workers = new Worker[2];
    private Colour colour;
    private GodCard godCard;

    public Player(PlayerInfo playerInfo){

        this.nickname=playerInfo.getPlayerNickname();
        this.birthday=playerInfo.getBirthday();
    }

    public void setColour(Colour colour) {
        this.colour = colour;
        setWorkers();
    }

    private void setWorkers(){
        workers[0]=new Worker(this.colour,0);
        workers[1]=new Worker(this.colour,1);
    }

    public void setGodCard(GodCard godCard) {
        this.godCard = godCard;
    }

    public Worker getWorker(int i){return workers[i];}

    public Colour getColour(){return colour;}

    public String getNickname() {
        return nickname;
    }

    public Calendar getBirthday() {
        return birthday;
    }

    public GodCard getGodCard() {
        return godCard;
    }
}
