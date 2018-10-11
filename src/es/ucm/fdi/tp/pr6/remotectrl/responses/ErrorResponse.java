package es.ucm.fdi.tp.pr6.remotectrl.responses;

import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;

public class ErrorResponse implements Response{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String msg;
	
	public ErrorResponse(String msg) {
		this.msg = msg;
	}

	@Override
	public void run(GameObserver o) {
		o.onError(msg);
	}

}
