package es.ucm.fdi.tp.distributedgame;


public class MyNumber implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private int number;
	private boolean winner;
	
	public MyNumber(int number, boolean winner){
		this.number = number;
		this.winner = winner;
	}
	
	public int getNumber(){
		return number;
	}
	
	public boolean isWinner(){
		return winner;
	}
	
	public void setWinner(boolean b){
		this.winner = b;
	}
	
}
