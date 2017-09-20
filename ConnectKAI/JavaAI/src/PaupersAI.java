import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;

public class PaupersAI extends CKPlayer {

	public Point adj[] = new Point[]{new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(-1, 1)};
	//create adjacent spots arrays here to avoid unnecessarily recreating the array every time heuristic is called
	private int currdepth;
	private byte enemy;
	private HashMap<Integer, Point> bpmap; //map BoardModel's hashcode to the best point to place piece

	public PaupersAI (byte player, BoardModel state) {
		super(player, state);
		enemy = alternate(player);
		teamName = "Paupers";
		this.currdepth = 0;
		bpmap = new HashMap<Integer, Point>();
	}

	@Override
	public Point getMove(BoardModel state) {
		return minimax(state, 3, System.currentTimeMillis(), 5000);
	}
	
	private byte alternate(byte player){ 
		//alternates players for minimax and maxValue (this AI) and for minValue (opponent)
		if (player == 1){
			return 2;
		}
		return 1;
	}
	
	private Point minimax(BoardModel state, int dlimit, long startTime, int timeLimit){
		Point best = new Point();
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int v = Integer.MIN_VALUE;
		BoardModel copy = state.clone(); //create separate copy of original board

		if (bpmap.containsKey(state.hashCode()) && state.getSpace(bpmap.get(state.hashCode())) == 0){
			copy = state.placePiece(bpmap.get(state.hashCode()), this.player);
			this.currdepth++;
			v = Integer.max(v, minValue(copy, dlimit, alpha, beta, startTime, timeLimit));
			this.currdepth--;
			alpha = v;
			best.setLocation(bpmap.get(state.hashCode()));
			if (alpha >= beta){
				return best;
			}
		}
		for (int row = 0; row < state.getWidth(); row++){
			for (int col = 0; col < state.getHeight(); col++){
				if (System.currentTimeMillis() - startTime > timeLimit){
					return best;
				}
				if (state.getSpace(row, col) == 0){ //don't want to put piece in an occupied spot
					copy = state.placePiece(new Point(row, col), this.player);
					//create separate copy of board with a new piece added
					this.currdepth++;
					v = Integer.max(v, minValue(copy, dlimit, alpha, beta, startTime, timeLimit));
					this.currdepth--; 
					if (v > alpha){
						alpha = v;
						best.setLocation(row, col);
						bpmap.put(state.hashCode(), new Point(row, col));
						if (alpha >= beta){
							return best;
						}
					}
					if (state.gravityEnabled()){
						break;
					}
				}
			}
		}
		return best;
	}
	
	private int maxValue(BoardModel state, int dlimit, int alph, int bet, long startTime, int timeLimit){
		byte p = state.winner();
//		if (p == player){
//			return Integer.MAX_VALUE;
//		}
		if (p == enemy){ //alternate(player)){
			return Integer.MIN_VALUE;
		}
		if (currdepth == dlimit || !state.hasMovesLeft()){
			return heuristic(state);//, player);
		}
		BoardModel copy = state.clone();
		int alpha = alph;
		int beta = bet;
		int v = Integer.MIN_VALUE;
		
		if (bpmap.containsKey(state.hashCode()) && state.getSpace(bpmap.get(state.hashCode())) == 0){
			copy = state.placePiece(bpmap.get(state.hashCode()), this.player);
			this.currdepth++;
			v = Integer.max(v, minValue(copy, dlimit, alpha, beta, startTime, timeLimit));
			this.currdepth--;
			alpha = v;
			if (alpha >= beta){
				return v;
			}
		}
		for (int row = 0; row < state.getWidth(); row++){
			for (int col = 0; col < state.getHeight(); col++){ 
				//loop through board looking for all possible moves
				if (System.currentTimeMillis() - startTime > timeLimit){
					return v;
				}
				if (state.getSpace(row, col) == 0){ //don't want to put piece in an occupied spot
					copy = state.placePiece(new Point(row, col), player);
					//create separate copy of board with a new piece added
					this.currdepth++;
					v = Integer.max(v, minValue(copy, dlimit, alpha, beta, startTime, timeLimit));
					this.currdepth--; 
					if (v > alpha){
						alpha = v;
						bpmap.put(state.hashCode(), new Point(row, col));
						if (alpha >= beta){
							return v;
						}
					}
					if(state.gravityEnabled()){
						break;
					}
				}
			}
		}
		return v;
	}
	
	private int minValue(BoardModel state, int dlimit, int alph, int bet, long startTime, int timeLimit){
		byte p = state.winner();
//		if (p == enemy){
//			return Integer.MIN_VALUE;
//		}
		if (p == this.player){//alternate(player)){
			return Integer.MAX_VALUE;
		}
		if (currdepth == dlimit || !state.hasMovesLeft()){
			return heuristic(state);//, this.player);
		}
		BoardModel copy = state.clone();
		int alpha = alph;
		int beta = bet;
		int v = Integer.MAX_VALUE;
		
		if (bpmap.containsKey(state.hashCode()) && state.getSpace(bpmap.get(state.hashCode())) == 0){
			copy = state.placePiece(bpmap.get(state.hashCode()), enemy);
			this.currdepth++;
			v = Integer.min(v, maxValue(copy, dlimit, alpha, beta, startTime, timeLimit));
			this.currdepth--;
			beta = v;
			if (alpha >= beta){
				return v;
			}
		}
		for (int row = 0; row < state.getWidth(); row++){
			for (int col = 0; col < state.getHeight(); col++){
				if (System.currentTimeMillis() - startTime > timeLimit){
					return v;
				}
				if (state.getSpace(row, col) == 0){ //don't want to put piece in an occupied spot
					copy = state.placePiece(new Point(row, col), enemy);
					//create separate copy of board with a new piece added
					this.currdepth++;
					v = Integer.min(v, maxValue(copy, dlimit, alpha, beta, startTime, timeLimit));
					this.currdepth--; //decrement here to avoid unnecessarily decrementing twice once we return from the last leaf
					if (v < beta){
						beta = v;
						bpmap.put(state.hashCode(), new Point(row, col));
						if (alpha >= beta){
							return v;
						}
					}
					if(state.gravityEnabled()){
						break;
					}
				}
			}
		}
		return v;
	}
	
	private int heuristic(BoardModel state){//,byte player){
		int win = 0;		
		for(int i=0; i<state.getWidth(); ++i){
			for(int j=0; j<state.getHeight(); ++j){					
				if(state.getSpace(i, j) != enemy){//alternate(player)){
					for(Point p: adj){
						LinkedList<Byte> kpieces = new LinkedList<Byte>(); 
						int in_a_row = 0;
						int check_width = i;
						int check_height = j;
						while(bound_check(check_width, check_height) && (state.getSpace(check_width, check_height) != enemy)){//alternate(player))){
							kpieces.add(state.getSpace(check_width, check_height));
							check_width += p.x;
							check_height += p.y;
							if(++in_a_row == state.getkLength()){
								int value = countk(kpieces, player);
								win += 1 << value; //, alternate(player));
								break;
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < state.getWidth(); i++){
			for (int j = 0; j < state.getHeight(); j++){
				if (state.getSpace(i, j) != player){
					for(Point p: adj){
						LinkedList<Byte> kpieces = new LinkedList<Byte>();
						int in_a_row = 0;
						int check_width = i;
						int check_height = j;
						while(bound_check(check_width, check_height) && (state.getSpace(check_width, check_height) != player)){
							kpieces.add(state.getSpace(check_width, check_height));
							check_width += p.x;
							check_height += p.y;
							if(++in_a_row == state.getkLength()){
								int value = countk(kpieces, enemy);//alternate(player));
								win -= 1 << value;
								break;
							}
						}
					}
				}
			}
		}
		return win;
	}
	
	private int countk(LinkedList<Byte> klist, byte player){//, byte enemy){
		int c = 0;
		for (int i = 0; i < klist.size(); i++){
			if(klist.get(i) == player){
				c++;//changing this affects how well ai plays
			}
		}
		return c; //return weighted value
	}
	
	private Boolean bound_check(int curr_x, int curr_y){
		return (curr_x >= 0) && (curr_y >= 0)
				&& (curr_x < this.startState.getWidth())
				&& (curr_y < this.startState.getHeight());
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		Point result = new Point(state.getWidth()/2,state.getHeight()/2);
		int i = 1;
		long begin = System.currentTimeMillis();
		int timeLimit = deadline - 50;
		while(System.currentTimeMillis()-begin < timeLimit){
			result = minimax(state, i, begin, timeLimit);
			++i;
		}
		return result;
//		return getMove(state);
	}
}
